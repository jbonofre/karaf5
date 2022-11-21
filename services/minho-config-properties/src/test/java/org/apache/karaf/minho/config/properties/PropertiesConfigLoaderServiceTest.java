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

import org.apache.karaf.minho.boot.Minho;
import org.apache.karaf.minho.boot.config.Application;
import org.apache.karaf.minho.boot.config.Config;
import org.apache.karaf.minho.boot.service.ConfigService;
import org.apache.karaf.minho.boot.service.ServiceRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PropertiesConfigLoaderServiceTest {

    @Test
    public void loadingTestFromSysProp() throws Exception {
        System.setProperty("minho.config", "target/test-classes/simple.properties");

        ServiceRegistry serviceRegistry = new ServiceRegistry();
        ConfigService configService = new ConfigService();
        serviceRegistry.add(configService);
        PropertiesConfigLoaderService service = new PropertiesConfigLoaderService();
        service.onRegister(serviceRegistry);

        Config config = serviceRegistry.get(ConfigService.class);

        Assertions.assertEquals("bar", config.property("foo"));

        System.clearProperty("minho.config");
    }

    @Test
    public void loadingTestFromClasspath() throws Exception {
        ServiceRegistry serviceRegistry = new ServiceRegistry();
        ConfigService configService = new ConfigService();
        serviceRegistry.add(configService);
        PropertiesConfigLoaderService service = new PropertiesConfigLoaderService();
        service.onRegister(serviceRegistry);

        Config config = serviceRegistry.get(Config.class);

        // properties
        Assertions.assertEquals("bar", config.property("foo"));
        Assertions.assertEquals("true", config.property("lifecycle.enabled"));
        Assertions.assertEquals("%m %n", config.property("log.patternLayout"));
        Assertions.assertEquals("./osgi/cache", config.property("osgi.storageDirectory"));
        Assertions.assertEquals("1", config.property("osgi.priority"));

        // TODO profiles
        // Assertions.assertEquals(1, config.getProfiles().size());

        // applications
        Assertions.assertEquals(2, config.getApplications().size());
        Application springBootApp = config.getApplications().get(1);
        Assertions.assertEquals("/path/to/app/spring-boot.jar", springBootApp.getUrl());
        Assertions.assertEquals("spring-boot", springBootApp.getType());
        Assertions.assertEquals("true", springBootApp.property("enableHttp"));
        Assertions.assertEquals("true", springBootApp.property("enablePrometheus"));
    }

    @Test
    public void runTest() {
        try (final var minho = Minho.builder().build().start()) {
            Config config = minho.getServiceRegistry().get(Config.class);
            Assertions.assertEquals(2, config.getApplications().size());
        }
    }
}
