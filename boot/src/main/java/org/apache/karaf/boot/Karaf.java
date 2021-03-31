/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.karaf.boot;

import lombok.extern.java.Log;
import org.apache.karaf.boot.config.KarafConfig;
import org.apache.karaf.boot.service.KarafLifeCycleService;
import org.apache.karaf.boot.service.ServiceRegistry;
import org.apache.karaf.boot.spi.Service;

import java.io.File;
import java.io.FileInputStream;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.SortedMap;

@Log
public class Karaf {

    private String base;
    private KarafConfig config;
    private ServiceRegistry serviceRegistry;

    private long startTime;

    private Karaf(KarafConfig config) {
        this.config = config;
    }

    /**
     * Create a Karaf instance.
     *
     * @return the Karaf instance.
     * @throws Exception in case of build failure.
     */
    public static Karaf build() throws Exception {
        // trying to load KarafConfig from KARAF_CONFIG environment variable
        KarafConfig config = null;
        if (System.getenv("KARAF_CONFIG") != null) {
            File file = new File(System.getenv("KARAF_CONFIG"));
            if (file.exists()) {
                log.info("Read KARAF_CONFIG " + System.getenv("KARAF_CONFIG"));
                config = KarafConfig.read(new FileInputStream(file));
            } else {
                log.warning(System.getenv("KARAF_CONFIG") + " doesn't exist");
            }
        }
        // trying to load KarafConfig from karaf.config system property
        if (config == null && System.getProperty("karaf.config") != null) {
            File file = new File(System.getProperty("karaf.config"));
            if (file.exists()) {
                log.info("Read karaf.config " + System.getProperty("karaf.config"));
                config = KarafConfig.read(new FileInputStream(file));
            } else {
                log.warning(System.getProperty("karaf.config") + " doesn't exist");
            }
        }
        // trying to load KarafConfig from classpath
        if (Karaf.class.getResourceAsStream("META-INF/karaf.json") != null) {
            config = KarafConfig.read(Karaf.class.getResourceAsStream("META-INF/karaf.json"));
        }
        if (Karaf.class.getResourceAsStream("karaf.json") != null) {
            config = KarafConfig.read(Karaf.class.getResourceAsStream("karaf.json"));
        }
        // loading default configuration
        if (config == null) {
            config = new KarafConfig();
        }
        return new Karaf(config);
    }

    /**
     * Create a Karaf instance with a given configuration.
     *
     * @param config the Karaf configuration.
     * @return the Karaf instance.
     */
    public static Karaf build(KarafConfig config) {
        return new Karaf(config);
    }

    /**
     * Init Karaf runtime, ready to serve.
     * @throws Exception in case of init failure.
     */
    public void init() throws Exception {
        startTime = System.currentTimeMillis();

        // log format
        if (System.getProperty("java.util.logging.config.file") == null) {
            if (System.getenv("KARAF_LOG_FORMAT") != null) {
                System.setProperty("java.util.logging.SimpleFormatter.format", System.getenv("KARAF_LOG_FORMAT"));
            }
            if (System.getProperty("java.util.logging.SimpleFormatter.format") == null) {
                System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tF %1$tT.%1$tN %4$s [ %2$s ] : %5$s%6$s%n");
            }
        }

        // banner
        if (System.getenv("KARAF_BANNER") != null) {
            log.info(System.getenv("KARAF_BANNER"));
        } else {
            log.info("\n" +
                    "        __ __                  ____      \n" +
                    "       / //_/____ __________ _/ __/      \n" +
                    "      / ,<  / __ `/ ___/ __ `/ /_        \n" +
                    "     / /| |/ /_/ / /  / /_/ / __/        \n" +
                    "    /_/ |_|\\__,_/_/   \\__,_/_/         \n" +
                    "\n" +
                    "  Apache Karaf (5.0-SNAPSHOT)\n");
        }

        if (System.getenv("KARAF_BASE") != null) {
            base = System.getenv("KARAF_BASE");
        } else if (System.getProperty("karaf.base") != null) {
            base = System.getProperty("karaf.base");
        } else {
            base = ".";
        }

        log.info("Base directory: " + this.base);

        log.info("Starting service registry");
        serviceRegistry = new ServiceRegistry();

        log.info("Loading services ...");
        ServiceLoader<Service> serviceLoader = ServiceLoader.load(Service.class);
        Map<Service, Long> servicePriority = new HashMap<>();
        serviceLoader.forEach(service -> {
            if ((service.name() != null) && (config.getProperties().get(service.name() + ".priority") != null)) {
                servicePriority.put(service, (Long) config.getProperties().get(service.name() + ".priority"));
            } else {
                servicePriority.put(service, service.defaultPriority());
            }
        });
        servicePriority.entrySet().stream().sorted(Map.Entry.comparingByValue()).forEach(entry -> {
           try {
               log.info("Registering " + entry.getKey().name());
               entry.getKey().onRegister(config, serviceRegistry);
           } catch (Exception e) {
               log.warning("Can't load service " + entry.getKey().name() + ": " + e);
           }
        });

        log.info("Starting services ...");
        KarafLifeCycleService karafLifeCycleService = serviceRegistry.get(KarafLifeCycleService.class);
        if (karafLifeCycleService == null) {
            throw new RuntimeException("Karaf lifecycle service is not registered");
        }
        karafLifeCycleService.start();

    }

    public void start() {
        long now = System.currentTimeMillis();
        log.info(getStartedMessage(startTime, now));
    }

    private String getStartedMessage(long start, long now) {
        StringBuilder message = new StringBuilder();
        message.append("Started in ");
        message.append((now - start) / 1000.0);
        message.append(" seconds");
        try {
            double uptime = ManagementFactory.getRuntimeMXBean().getUptime() / 1000.0;
            message.append(" (JVM running for ").append(uptime).append(")");
        } catch (Throwable e) {
            // no-op
        }
        return message.toString();
    }

    public KarafConfig getConfig() {
        return config;
    }

}
