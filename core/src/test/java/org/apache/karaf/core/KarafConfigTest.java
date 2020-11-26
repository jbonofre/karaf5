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
package org.apache.karaf.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class KarafConfigTest {

    @BeforeEach
    public void cleanup() throws Exception {
        System.clearProperty("karaf.home");
        System.clearProperty("karaf.base");
        System.clearProperty("karaf.data");
        System.clearProperty("karaf.etc");
        System.clearProperty("karaf.cache");
    }

    @Test
    public void testDefaultBuild() throws Exception {
        KarafConfig config = KarafConfig.builder().build();

        String javaTmpDir = System.getProperty("java.io.tmpdir");
        String userHomeDir = System.getProperty("user.home");

        Assertions.assertEquals(javaTmpDir + "/karaf", config.homeDirectory);
        Assertions.assertEquals(javaTmpDir + "/karaf/data", config.dataDirectory);
        Assertions.assertEquals(javaTmpDir + "/karaf/data/cache", config.cacheDirectory);
        Assertions.assertEquals(javaTmpDir + "/karaf/etc", config.etcDirectory);
        Assertions.assertEquals("file:" + userHomeDir + "/.m2/repository,file:" + javaTmpDir + "/karaf/system,https://repo1.maven.org/maven2",
                config.mavenRepositories);
        Assertions.assertEquals(false, config.clearCache);
        Assertions.assertEquals(80, config.defaultBundleStartLevel);
    }

    @Test
    public void testBuildWithOption() throws Exception {
        KarafConfig config = KarafConfig.builder()
                .homeDirectory("target/test")
                .dataDirectory("target/test/data")
                .cacheDirectory("target/test/data/cache")
                .etcDirectory("target/test/etc")
                .mavenRepositories("")
                .clearCache(true)
                .defaultBundleStartLevel(30)
                .build();

        Assertions.assertEquals("target/test", config.homeDirectory);
        Assertions.assertEquals("target/test/data", config.dataDirectory);
        Assertions.assertEquals("target/test/data/cache", config.cacheDirectory);
        Assertions.assertEquals("target/test/etc", config.etcDirectory);
        Assertions.assertEquals("", config.mavenRepositories);
        Assertions.assertTrue(config.clearCache);
        Assertions.assertEquals(30, config.defaultBundleStartLevel);
    }

}
