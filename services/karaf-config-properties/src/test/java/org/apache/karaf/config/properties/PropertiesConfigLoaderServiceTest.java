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

import org.apache.karaf.boot.Karaf;
import org.apache.karaf.boot.config.Application;
import org.apache.karaf.boot.config.KarafConfig;
import org.apache.karaf.boot.service.KarafConfigService;
import org.apache.karaf.boot.service.ServiceRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PropertiesConfigLoaderServiceTest {

    @Test
    public void loadingTestFromSysProp() throws Exception {
        System.setProperty("karaf.config", "target/test-classes/simple.properties");

        ServiceRegistry serviceRegistry = new ServiceRegistry();
        KarafConfigService karafConfigService = new KarafConfigService();
        serviceRegistry.add(karafConfigService);
        PropertiesConfigLoaderService service = new PropertiesConfigLoaderService();
        service.onRegister(serviceRegistry);

        KarafConfig karafConfig = serviceRegistry.get(KarafConfigService.class);

        Assertions.assertEquals("bar", karafConfig.getProperties().get("foo"));

        System.clearProperty("karaf.config");
    }

    @Test
    public void loadingTestFromClasspath() throws Exception {
        ServiceRegistry serviceRegistry = new ServiceRegistry();
        KarafConfigService configService = new KarafConfigService();
        serviceRegistry.add(configService);
        PropertiesConfigLoaderService service = new PropertiesConfigLoaderService();
        service.onRegister(serviceRegistry);

        KarafConfig karafConfig = serviceRegistry.get(KarafConfig.class);

        // properties
        Assertions.assertEquals("bar", karafConfig.getProperties().get("foo"));
        Assertions.assertEquals("true", karafConfig.getProperties().get("lifecycle.enabled"));
        Assertions.assertEquals("%m %n", karafConfig.getProperties().get("log.patternLayout"));
        Assertions.assertEquals("./osgi/cache", karafConfig.getProperties().get("osgi.storageDirectory"));
        Assertions.assertEquals("1", karafConfig.getProperties().get("osgi.priority"));

        // TODO profiles
        // Assertions.assertEquals(1, karafConfig.getProfiles().size());

        // applications
        Assertions.assertEquals(2, karafConfig.getApplications().size());
        Application springBootApp = karafConfig.getApplications().get(1);
        Assertions.assertEquals("/path/to/app/spring-boot.jar", springBootApp.getUrl());
        Assertions.assertEquals("spring-boot", springBootApp.getType());
        Assertions.assertEquals("true", springBootApp.getProperties().get("enableHttp"));
        Assertions.assertEquals("true", springBootApp.getProperties().get("enablePrometheus"));
    }

    @Test
    public void runTest() throws Exception {
        Karaf karaf = Karaf.builder().build();
        karaf.start();

        KarafConfig karafConfig = karaf.getServiceRegistry().get(KarafConfig.class);

        Assertions.assertEquals(2, karafConfig.getApplications().size());
    }

}
