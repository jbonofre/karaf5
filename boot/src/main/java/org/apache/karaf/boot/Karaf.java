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
import org.apache.felix.framework.FrameworkFactory;
import org.apache.felix.framework.cache.BundleCache;
import org.apache.felix.framework.util.FelixConstants;
import org.apache.karaf.boot.config.KarafConfig;
import org.apache.karaf.boot.module.ModuleManager;
import org.apache.karaf.boot.specs.SpecsListener;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.startlevel.FrameworkStartLevel;

import java.io.File;
import java.io.FileInputStream;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

@Log
public class Karaf {

    private KarafConfig config;
    private Framework framework = null;
    private ModuleManager moduleManager;

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
        // TODO load from classpath
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

        // TODO URLs resolver registration

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
                    "  Apache Karaf (5.0.0-SNAPSHOT)\n");
        }

        log.info("Home directory: " + this.config.getHome());
        log.info("Cache directory: " + this.config.getCache());

        // TODO create libraries
        log.fine("Creating libraries");

        log.fine("Creating framework configuration");
        Map<String, Object> frameworkConfig = new HashMap<>();
        // cache
        frameworkConfig.put(Constants.FRAMEWORK_STORAGE, this.config.getCache());
        // clear cache
        if (this.config.isClearCache()) {
            frameworkConfig.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
        }
        // start level
        frameworkConfig.put(Constants.FRAMEWORK_BEGINNING_STARTLEVEL, this.config.getStartLevel());
        // framework log level
        frameworkConfig.put(FelixConstants.LOG_LEVEL_PROP, this.config.getFrameworkLogLevel());
        // bundle cache dir
        frameworkConfig.put(BundleCache.CACHE_ROOTDIR_PROP, this.config.getCache() + "/bundles");
        // TODO set boot delegation and system packages ?

        FrameworkFactory frameworkFactory = new FrameworkFactory();
        framework = frameworkFactory.newFramework(frameworkConfig);

        try {
            framework.init();
            framework.start();
        } catch (BundleException e) {
            throw new RuntimeException(e);
        }

        FrameworkStartLevel frameworkStartLevel = framework.adapt(FrameworkStartLevel.class);
        frameworkStartLevel.setInitialBundleStartLevel(Integer.parseInt(this.config.getBundleStartLevel()));

        log.info("Starting module manager");
        moduleManager = new ModuleManager(framework);

        if (framework.getBundleContext().getBundles().length == 1) {
            log.info("Starting specs listener");
            SpecsListener specsListener = new SpecsListener();
            specsListener.start(framework.getBundleContext());

            log.info("Registering Karaf service");
            framework.getBundleContext().registerService(Karaf.class, this, null);

            log.info("Registering module manager service");
            framework.getBundleContext().registerService(ModuleManager.class, moduleManager, null);
        }
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

    public BundleContext getContext() {
        return framework.getBundleContext();
    }

    public Framework getFramework() {
        return framework;
    }

    public KarafConfig getConfig() {
        return config;
    }

    public void addModule(String url, String ... args) throws Exception {
        moduleManager.add(url, args);
    }

    public void removeModule(String id) throws Exception {
        moduleManager.remove(id);
    }

    public ModuleManager getModuleManager() {
        return this.moduleManager;
    }

}
