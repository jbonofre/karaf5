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
package org.apache.karaf.config.properties;

import lombok.extern.java.Log;
import org.apache.karaf.boot.config.Application;
import org.apache.karaf.boot.config.KarafConfig;
import org.apache.karaf.boot.service.ServiceRegistry;
import org.apache.karaf.boot.spi.Service;

import java.io.FileInputStream;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Load KarafConfig from a properties.
 */
@Log
public class PropertiesConfigLoaderService implements Service {

    @Override
    public String name() {
        return "properties-config-loader";
    }

    @Override
    public int priority() {
        return (-DEFAULT_PRIORITY) + 100;
    }

    @Override
    public void onRegister(final ServiceRegistry serviceRegistry) throws Exception {
        Properties properties = new Properties();
        if (System.getenv("KARAF_CONFIG") != null) {
            log.info("Loading properties from KARAF_CONFIG env variable");
            properties.load(new StringReader(System.getenv("KARAF_CONFIG")));
        } else if (System.getenv("KARAF_CONFIG_FILE") != null) {
            log.info("Loading configuration from " + System.getenv("KARAF_CONFIG_FILE"));
            properties.load(new FileInputStream(System.getenv("KARAF_CONFIG_FILE")));
        } else if (System.getProperty("karaf.config") != null) {
            log.info("Loading configuration from " + System.getProperty("karaf.config"));
            properties.load(new FileInputStream(System.getProperty("karaf.config")));
        } else if (PropertiesConfigLoaderService.class.getResourceAsStream("/META-INF/karaf.properties") != null) {
            log.info("Loading configuration from classpath META-INF/karaf.properties");
            properties.load(PropertiesConfigLoaderService.class.getResourceAsStream("/META-INF/karaf.properties"));
        } else if (PropertiesConfigLoaderService.class.getResourceAsStream("/karaf.properties") != null) {
            log.info("Loading configuration from classpath karaf.properties");
            properties.load(PropertiesConfigLoaderService.class.getResourceAsStream("/karaf.properties"));
        }
        KarafConfig karafConfig = parse(properties);
        final var existing = serviceRegistry.get(KarafConfig.class);
        existing.merge(karafConfig);
    }

    private KarafConfig parse(final Properties properties) {
        KarafConfig karafConfig = new KarafConfig();

        properties.keySet().stream().filter(key -> !((String) key).startsWith("application."))
                .forEach(key -> {
                    karafConfig.getProperties().put(((String) key), properties.get(key));
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
                } else if (value.equals("application." + appName + ".profiles")) {
                    application.getProfiles().add((String) properties.get(value));
                } else {
                    application.getProperties().put(((String) value).substring(("application." + appName).length() + 1), properties.get(value));
                }
            });
            karafConfig.getApplications().add(application);
        });

        return karafConfig;
    }

}
