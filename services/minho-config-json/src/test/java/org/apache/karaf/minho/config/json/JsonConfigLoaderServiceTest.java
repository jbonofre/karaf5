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
package org.apache.karaf.minho.config.json;

import org.apache.karaf.minho.boot.Minho;
import org.apache.karaf.minho.boot.config.Application;
import org.apache.karaf.minho.boot.config.Config;
import org.apache.karaf.minho.boot.service.ConfigService;
import org.apache.karaf.minho.boot.service.ServiceRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JsonConfigLoaderServiceTest {

    @Test
    public void loadingTestFromSystemProp() throws Exception {
        System.setProperty("minho.config", "target/test-classes/emptyrun.json");

        ServiceRegistry serviceRegistry = new ServiceRegistry();
        ConfigService configService = new ConfigService();
        serviceRegistry.add(configService);
        JsonConfigLoaderService service = new JsonConfigLoaderService();
        service.onRegister(serviceRegistry);

        Config config = serviceRegistry.get(ConfigService.class);

        assertEquals("bar", config.property("foo"));
        assertEquals(0, config.getProfiles().size());
        assertEquals(0, config.getApplications().size());

        System.clearProperty("minho.config");
    }

    @Test
    public void loadingTestFromClasspath() throws Exception {
        ServiceRegistry serviceRegistry = new ServiceRegistry();
        ConfigService configService = new ConfigService();
        serviceRegistry.add(configService);
        JsonConfigLoaderService service = new JsonConfigLoaderService();
        service.onRegister(serviceRegistry);

        Config config = serviceRegistry.get(Config.class);

        // properties
        assertEquals("bar", config.property("foo"));
        assertTrue(Boolean.parseBoolean(config.property("lifecycle.enabled")));
        assertEquals("%m %n", config.property("log.patternLayout"));
        assertEquals("./osgi/cache", config.property("osgi.storageDirectory"));
        assertEquals(1, Long.parseLong(config.property("osgi.priority")));

        // profiles
        assertEquals(1, config.getProfiles().size());

        // applications
        assertEquals(2, config.getApplications().size());
        Application springBootApp = config.getApplications().get(0);
        assertEquals("/path/to/app/spring-boot.jar", springBootApp.getUrl());
        assertEquals("spring-boot", springBootApp.getType());
        assertTrue(Boolean.parseBoolean(springBootApp.property("enableHttp")));
        assertTrue(Boolean.parseBoolean(springBootApp.property("enablePrometheus")));
    }

    @Test
    public void runTest() {
        try (final var minho = Minho.builder().build().start()) {
            final var config = minho.getServiceRegistry().get(Config.class);
            assertEquals(2, config.getApplications().size());
        }
    }
}
