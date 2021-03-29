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

public class KarafConfigTest {

    @Test
    public void defaultTest() throws Exception {
        KarafConfig karafConfig = KarafConfig.build();

        Assertions.assertNull(karafConfig.getLauncher());
    }

    @Test
    public void readTest() throws Exception {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("karaf.json");
        KarafConfig karafConfig = KarafConfig.read(inputStream);

        // properties
        Assertions.assertEquals("bar", karafConfig.getLauncher().getProperties().get("foo"));

        // managers
        Assertions.assertEquals(3, karafConfig.getLauncher().getManagers().size());
        // osgi
        Service osgi = karafConfig.getLauncher().getManagers().get(0);
        Assertions.assertEquals("osgi", osgi.getName());
        Assertions.assertEquals("./osgi/cache", osgi.getProperties().get("storageDirectory"));
        Assertions.assertEquals("80", osgi.getProperties().get("startLevel"));
        // spring boot
        Service springBoot = karafConfig.getLauncher().getManagers().get(1);
        Assertions.assertEquals("spring-boot", springBoot.getName());
        // microprofile
        Service microprofile = karafConfig.getLauncher().getManagers().get(2);
        Assertions.assertEquals("microprofile", microprofile.getName());
        Assertions.assertFalse(((boolean) microprofile.getProperties().get("enabled")));

        Assertions.assertEquals(1, karafConfig.getLauncher().getServices().size());
        // log service
        Service log = karafConfig.getLauncher().getServices().get(0);
        Assertions.assertEquals("log", log.getName());
        Assertions.assertEquals("/path/to/log-service.jar", log.getLocation());
        Assertions.assertEquals("%m %n", log.getProperties().get("patternLayout"));

        // profiles
        Assertions.assertEquals(1, karafConfig.getProfiles().size());

        // applications
        Assertions.assertEquals(2, karafConfig.getApplications().size());
    }

}
