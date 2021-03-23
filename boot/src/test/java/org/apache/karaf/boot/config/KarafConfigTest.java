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

        Assertions.assertEquals(".", karafConfig.getHome());
        Assertions.assertEquals("./data", karafConfig.getData());
        Assertions.assertEquals("./data/cache", karafConfig.getCache());
        Assertions.assertEquals("100", karafConfig.getStartLevel());
        Assertions.assertEquals("3", karafConfig.getFrameworkLogLevel());
    }

    @Test
    public void overwriteTest() throws Exception {
        KarafConfig karafConfig = KarafConfig.build();
        karafConfig.setHome("foo");
        karafConfig.setCache("other/cache");
        karafConfig.setFrameworkLogLevel("2");

        Assertions.assertEquals("foo", karafConfig.getHome());
        Assertions.assertEquals("other/cache", karafConfig.getCache());
        Assertions.assertEquals("100", karafConfig.getStartLevel());
        Assertions.assertEquals("2", karafConfig.getFrameworkLogLevel());
    }

    @Test
    public void readTest() throws Exception {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("karaf.json");
        KarafConfig karafConfig = KarafConfig.read(inputStream);

        Assertions.assertEquals("/path/to/home/directory", karafConfig.getHome());
        Assertions.assertEquals("${home}/data/directory", karafConfig.getData());
        Assertions.assertEquals("${data}/cache/directory", karafConfig.getCache());

        Assertions.assertEquals("java.lang,java.util", karafConfig.getSystemPackages());

        Assertions.assertEquals("bar", karafConfig.getProperties().get("foo"));
        Assertions.assertEquals("world", karafConfig.getProperties().get("hello"));

        Assertions.assertEquals(2, karafConfig.getLibraries().size());
        Library jsonb = karafConfig.getLibraries().get(0);
        Assertions.assertEquals("json-b", jsonb.getName());
        Assertions.assertTrue(jsonb.isSystem());
        Assertions.assertEquals(2, jsonb.getResources().size());
        Assertions.assertEquals("mvn:org.apache.geronimo.specs/geronimo-jsonb_1.0_spec/1.0", jsonb.getResources().get(0));
        Assertions.assertEquals("mvn:org.apache.johnzon/johnzon-core/1.2.10", jsonb.getResources().get(1));
        Library myLib = karafConfig.getLibraries().get(1);
        Assertions.assertEquals("my-lib", myLib.getName());
        Assertions.assertFalse(myLib.isSystem());
        Assertions.assertEquals(1, myLib.getResources().size());
        Assertions.assertEquals("embedded:/foo/bar/my.jar", myLib.getResources().get(0));

        Configs configs = karafConfig.getConfigs();
        Assertions.assertEquals(2, configs.getEnvs().size());
        Assertions.assertEquals("configMap1", configs.getEnvs().get(0));
        Assertions.assertEquals("configMap2", configs.getEnvs().get(1));
        Assertions.assertEquals(1, configs.getFiles().size());
        Assertions.assertEquals("org.my.pid.file", configs.getFiles().get(0).getId());
        Assertions.assertEquals("mvn:groupId/artifactId/1.0/cfg", configs.getFiles().get(0).getLocation());
        Assertions.assertEquals(1, configs.getInlines().size());
        Assertions.assertEquals("org.my.pid", configs.getInlines().get(0).getId());
        Assertions.assertEquals("firstvalue", configs.getInlines().get(0).getProperties().get("first"));
        Assertions.assertEquals("secondvalue", configs.getInlines().get(0).getProperties().get("second"));

        Assertions.assertEquals(3, karafConfig.getProfiles().size());
        Profile myProfile = karafConfig.getProfiles().get(0);
        Assertions.assertEquals("myprofile", myProfile.getName());
        Assertions.assertNull(myProfile.getLocation());
        Assertions.assertTrue(myProfile.isShared());
        Assertions.assertEquals("value", myProfile.getProperties().get("other"));
        Assertions.assertEquals("true", myProfile.getProperties().get("karaf.shell.console"));
        Assertions.assertEquals("appConfigMap", myProfile.getConfigs().getEnvs().get(0));
        Assertions.assertEquals(0, myProfile.getConfigs().getFiles().size());
        Assertions.assertEquals(0, myProfile.getConfigs().getInlines().size());
        Assertions.assertEquals(1, myProfile.getLibraries().size());
        Assertions.assertEquals("my-lib", myProfile.getLibraries().get(0));
        Assertions.assertEquals(2, myProfile.getResources().size());
        Assertions.assertEquals("mvn:org.apache.karaf.services/shell/5.0-SNAPSHOT", myProfile.getResources().get(0));
        Assertions.assertEquals("embedded:/path/relative/to/profile/my.jar", myProfile.getResources().get(1));
        Assertions.assertNull(karafConfig.getProfiles().get(1).getName());
        Assertions.assertEquals("mvn:org.apache.karaf.profiles/other/1.0/json", karafConfig.getProfiles().get(1).getLocation());
        Assertions.assertNull(karafConfig.getProfiles().get(2).getName());
        Assertions.assertEquals("mvn:org.apache.karaf.profiles/third/1.0", karafConfig.getProfiles().get(2).getLocation());

        Assertions.assertEquals(4, karafConfig.getModules().size());
        Assertions.assertEquals("mvn:groupId/artifact/1.0", karafConfig.getModules().get(0).getLocation());
        Assertions.assertNull(karafConfig.getModules().get(0).getProfile());
        Assertions.assertEquals("http://...", karafConfig.getModules().get(1).getLocation());
        Assertions.assertEquals("myprofile", karafConfig.getModules().get(1).getProfile());
        Assertions.assertEquals("embedded:/inner/path/module.jar", karafConfig.getModules().get(2).getLocation());
        Assertions.assertEquals("other", karafConfig.getModules().get(2).getProfile());
        Assertions.assertEquals("file:....", karafConfig.getModules().get(3).getLocation());
        Assertions.assertNull(karafConfig.getModules().get(3).getProfile());
    }

}
