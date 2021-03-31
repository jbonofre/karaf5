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
package org.apache.karaf.boot.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.math.BigDecimal;

public class KarafConfigTest {

    @Test
    public void readTest() throws Exception {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("karaf.json");
        KarafConfig karafConfig = KarafConfig.read(inputStream);

        // properties
        Assertions.assertEquals("bar", karafConfig.getProperties().get("foo"));
        Assertions.assertTrue((Boolean) karafConfig.getProperties().get("lifecycle.enabled"));
        Assertions.assertEquals("%m %n", karafConfig.getProperties().get("log.patternLayout"));
        Assertions.assertEquals("./osgi/cache", karafConfig.getProperties().get("osgi.storageDirectory"));
        Assertions.assertEquals(1, ((BigDecimal) karafConfig.getProperties().get("osgi.priority")).longValue());

        // profiles
        Assertions.assertEquals(1, karafConfig.getProfiles().size());

        // applications
        Assertions.assertEquals(2, karafConfig.getApplications().size());
        Application springBootApp = karafConfig.getApplications().get(0);
        Assertions.assertEquals("/path/to/app/spring-boot.jar", springBootApp.getUrl());
        Assertions.assertEquals("spring-boot", springBootApp.getType());
        Assertions.assertTrue((boolean) springBootApp.getProperties().get("enableHttp"));
        Assertions.assertTrue((boolean) springBootApp.getProperties().get("enablePrometheus"));
    }

}
