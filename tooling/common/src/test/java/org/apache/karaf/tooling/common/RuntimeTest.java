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
package org.apache.karaf.tooling.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class RuntimeTest {

    @Test
    public void createPackageTest() throws Exception {
        Map<String, Object> properties = new HashMap<>();
        properties.put("base.directory", "target/runtime/test-package");
        properties.put("include.transitive", "true");
        org.apache.karaf.tooling.common.Runtime runtime = new org.apache.karaf.tooling.common.Runtime("test-package", "5.0-SNAPSHOT", properties);
        runtime.getDependencies().add("k5:karaf-boot");
        runtime.getDependencies().add("karaf-http");
        
        runtime.createPackage();

        Path runtimeFolder = Paths.get("target/runtime/test-package");
        Path libFolder = runtimeFolder.resolve("");
        Assertions.assertTrue(Files.exists(runtimeFolder));
        Assertions.assertTrue(Files.isDirectory(libFolder));

        Assertions.assertTrue(Files.exists(libFolder.resolve("karaf-boot-5.0-SNAPSHOT.jar")));
        Assertions.assertTrue(Files.exists(libFolder.resolve("karaf-http-5.0-SNAPSHOT.jar")));
        Assertions.assertTrue(Files.exists(libFolder.resolve("jetty-server-11.0.9.jar")));
    }

    @Test
    public void createPackageViaKarafBuild() throws Exception {
        Runtime.createPackage(new FileInputStream("target/test-classes/karaf-build.json"));

        Path runtimeFolder = Paths.get("target/runtime/test-build");
        Path libFolder = runtimeFolder.resolve("");
        Assertions.assertTrue(Files.exists(runtimeFolder));
        Assertions.assertTrue(Files.isDirectory(libFolder));

        Assertions.assertTrue(Files.exists(libFolder.resolve("karaf-boot-5.0-SNAPSHOT.jar")));
        Assertions.assertTrue(Files.exists(libFolder.resolve("karaf-http-5.0-SNAPSHOT.jar")));
        Assertions.assertTrue(Files.exists(libFolder.resolve("jetty-server-11.0.9.jar")));
        Assertions.assertTrue(Files.exists(libFolder.resolve("commons-lang-2.6.jar")));
    }

    @Test
    public void createRuntimeJarTest() throws Exception {
        Map<String, Object> properties = new HashMap<>();
        properties.put("base.directory", "target/runtime/test-runtime-jar");
        properties.put("include.transitive", "true");
        properties.put("karaf.lib", "lib");
        org.apache.karaf.tooling.common.Runtime runtime = new org.apache.karaf.tooling.common.Runtime("test-runtime-jar", "5.0-SNAPSHOT", properties);
        runtime.getDependencies().add("karaf-boot");
        runtime.getDependencies().add("mvn:org.apache.karaf/karaf-http/5.0-SNAPSHOT");
        runtime.getDependencies().add("file:pom.xml");
        runtime.getDependencies().add("https://repo1.maven.org/maven2/commons-lang/commons-lang/2.6/commons-lang-2.6.jar");
        runtime.getDependencies().add("mvn:commons-lang/commons-lang/2.5");

        runtime.createPackage();
        runtime.createJar();

        Path runtimeFolder = Paths.get("target/runtime/test-runtime-jar");
        Path libFolder = runtimeFolder.resolve("lib");
        Assertions.assertTrue(Files.exists(libFolder));
        Assertions.assertTrue(Files.isDirectory(libFolder));

        Assertions.assertTrue(Files.exists(libFolder.resolve("karaf-boot-5.0-SNAPSHOT.jar")));
        Assertions.assertTrue(Files.exists(libFolder.resolve("karaf-http-5.0-SNAPSHOT.jar")));
        Assertions.assertTrue(Files.exists(libFolder.resolve("pom.xml")));
        Assertions.assertTrue(Files.exists(libFolder.resolve("commons-lang-2.6.jar")));
        Assertions.assertTrue(Files.exists(libFolder.resolve("commons-lang-2.5.jar")));
    }

    @Test
    public void createArchiveTest() throws Exception {
        Map<String, Object> properties = new HashMap<>();
        properties.put("base.directory", "target/runtime/test-archive");
        properties.put("include.transitive", "true");
        properties.put("karaf.lib", "lib");
        org.apache.karaf.tooling.common.Runtime runtime = new org.apache.karaf.tooling.common.Runtime("test-archive", "5.0-SNAPSHOT", properties);
        runtime.getDependencies().add("karaf-boot");

        runtime.createPackage();
        runtime.createArchive();

        Path runtimeFolder = Paths.get("target/runtime/test-archive");
        Path binFolder = runtimeFolder.resolve("bin");
        Assertions.assertTrue(Files.exists(binFolder));
        Assertions.assertTrue(Files.isDirectory(binFolder));

        Path scriptPath = binFolder.resolve("karaf.sh");
        Assertions.assertTrue(Files.exists(scriptPath));
        Assertions.assertFalse(Files.isDirectory(scriptPath));
        Assertions.assertTrue(Files.isExecutable(scriptPath));

        Path archivePath = runtimeFolder.resolve("test-archive.zip");
        Assertions.assertTrue(Files.exists(archivePath));
    }

}
