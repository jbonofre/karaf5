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
package org.apache.karaf.minho.config.properties;

import lombok.extern.java.Log;
import org.apache.karaf.minho.boot.config.Application;
import org.apache.karaf.minho.boot.config.Config;
import org.apache.karaf.minho.boot.service.ServiceRegistry;
import org.apache.karaf.minho.boot.spi.Service;

import java.io.FileInputStream;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Load Config from a properties.
 */
@Log
public class PropertiesConfigLoaderService implements Service {

    @Override
    public String name() {
        return "minho-config-properties-service";
    }

    @Override
    public int priority() {
        return (-DEFAULT_PRIORITY) + 100;
    }

    @Override
    public void onRegister(final ServiceRegistry serviceRegistry) throws Exception {
        Properties properties = new Properties();
        if (System.getenv("MINHO_CONFIG") != null) {
            log.info("Loading properties from MINHO_CONFIG env variable");
            properties.load(new StringReader(System.getenv("MINHO_CONFIG")));
        } else if (System.getenv("MINHO_CONFIG_FILE") != null) {
            log.info("Loading configuration from " + System.getenv("MINHO_CONFIG_FILE"));
            properties.load(new FileInputStream(System.getenv("MINHO_CONFIG_FILE")));
        } else if (System.getProperty("minho.config") != null) {
            log.info("Loading configuration from " + System.getProperty("minho.config"));
            properties.load(new FileInputStream(System.getProperty("minho.config")));
        } else if (PropertiesConfigLoaderService.class.getResourceAsStream("/META-INF/minho.properties") != null) {
            log.info("Loading configuration from classpath META-INF/minho.properties");
            properties.load(PropertiesConfigLoaderService.class.getResourceAsStream("/META-INF/minho.properties"));
        } else if (PropertiesConfigLoaderService.class.getResourceAsStream("/minho.properties") != null) {
            log.info("Loading configuration from classpath minho.properties");
            properties.load(PropertiesConfigLoaderService.class.getResourceAsStream("/minho.properties"));
        }
        Config config = parse(properties);
        final var existing = serviceRegistry.get(Config.class);
        existing.merge(config);
    }

    private Config parse(final Properties properties) {
        Config config = new Config();

        properties.keySet().stream().filter(key -> !((String) key).startsWith("application."))
                .forEach(key -> {
                    config.getProperties().put(((String) key), properties.get(key).toString());
                });

        List<Object> applicationKeys = properties.keySet().stream().filter(key -> ((String) key).startsWith("application."))
                .collect(Collectors.toList());
        Map<Object, List<Object>> groupBy = applicationKeys.stream()
                .collect(Collectors.groupingBy(key -> {
                    String local = ((String) key).substring("application.".length());
                    return local.substring(0, local.indexOf("."));
                }));
        groupBy.entrySet().forEach(entry -> {
            Application application = new Application();
            String appName = (String) entry.getKey();
            entry.getValue().forEach(value -> {
                if (value.equals("application." + appName + ".type")) {
                    application.setType((String) properties.get(value));
                } else if (value.equals("application." + appName + ".url")) {
                    application.setUrl((String) properties.get(value));
                } else if (value.equals("application." + appName + ".profile")) {
                    application.setProfile((String) properties.get(value));
                } else {
                    application.getProperties().put(((String) value).substring(("application." + appName).length() + 1), properties.get(value).toString());
                }
            });
            config.getApplications().add(application);
        });

        return config;
    }

}
