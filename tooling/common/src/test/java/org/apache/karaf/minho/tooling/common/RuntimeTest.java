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
package org.apache.karaf.minho.tooling.common;

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
        Runtime runtime = new Runtime("test-package", "1.0-SNAPSHOT", properties);
        runtime.getDependencies().add("minho:minho-boot");
        runtime.getDependencies().add("minho-http");
        
        runtime.createPackage();

        Path runtimeFolder = Paths.get("target/runtime/test-package");
        Path libFolder = runtimeFolder.resolve("");
        Assertions.assertTrue(Files.exists(runtimeFolder));
        Assertions.assertTrue(Files.isDirectory(libFolder));

        Assertions.assertTrue(Files.exists(libFolder.resolve("minho-boot-1.0-SNAPSHOT.jar")));
        Assertions.assertTrue(Files.exists(libFolder.resolve("minho-http-1.0-SNAPSHOT.jar")));
        Assertions.assertTrue(Files.exists(libFolder.resolve("jetty-server-11.0.9.jar")));
    }

    @Test
    public void createPackageViaMinhoBuild() throws Exception {
        Runtime.createPackage(new FileInputStream("target/test-classes/minho-build.json"));

        Path runtimeFolder = Paths.get("target/runtime/test-build");
        Path libFolder = runtimeFolder.resolve("");
        Assertions.assertTrue(Files.exists(runtimeFolder));
        Assertions.assertTrue(Files.isDirectory(libFolder));

        Assertions.assertTrue(Files.exists(libFolder.resolve("minho-boot-1.0-SNAPSHOT.jar")));
        Assertions.assertTrue(Files.exists(libFolder.resolve("minho-http-1.0-SNAPSHOT.jar")));
        Assertions.assertTrue(Files.exists(libFolder.resolve("jetty-server-11.0.9.jar")));
        Assertions.assertTrue(Files.exists(libFolder.resolve("commons-lang-2.6.jar")));
    }

    @Test
    public void createRuntimeJarTest() throws Exception {
        Map<String, Object> properties = new HashMap<>();
        properties.put("base.directory", "target/runtime/test-runtime-jar");
        properties.put("include.transitive", "true");
        properties.put("minho.lib", "lib");
        Runtime runtime = new Runtime("test-runtime-jar", "1.0-SNAPSHOT", properties);
        runtime.getDependencies().add("minho-boot");
        runtime.getDependencies().add("mvn:org.apache.karaf.minho/minho-http/1.0-SNAPSHOT");
        runtime.getDependencies().add("file:pom.xml");
        runtime.getDependencies().add("https://repo1.maven.org/maven2/commons-lang/commons-lang/2.6/commons-lang-2.6.jar");
        runtime.getDependencies().add("mvn:commons-lang/commons-lang/2.5");

        runtime.createPackage();
        runtime.createJar();

        Path runtimeFolder = Paths.get("target/runtime/test-runtime-jar");
        Path libFolder = runtimeFolder.resolve("lib");
        Assertions.assertTrue(Files.exists(libFolder));
        Assertions.assertTrue(Files.isDirectory(libFolder));

        Assertions.assertTrue(Files.exists(libFolder.resolve("minho-boot-1.0-SNAPSHOT.jar")));
        Assertions.assertTrue(Files.exists(libFolder.resolve("minho-http-1.0-SNAPSHOT.jar")));
        Assertions.assertTrue(Files.exists(libFolder.resolve("pom.xml")));
        Assertions.assertTrue(Files.exists(libFolder.resolve("commons-lang-2.6.jar")));
        Assertions.assertTrue(Files.exists(libFolder.resolve("commons-lang-2.5.jar")));
    }

    @Test
    public void createArchiveTest() throws Exception {
        Map<String, Object> properties = new HashMap<>();
        properties.put("base.directory", "target/runtime/test-archive");
        properties.put("include.transitive", "true");
        properties.put("minho.lib", "lib");
        Runtime runtime = new Runtime("test-archive", "1.0-SNAPSHOT", properties);
        runtime.getDependencies().add("minho-boot");

        runtime.createPackage();
        runtime.createArchive();

        Path runtimeFolder = Paths.get("target/runtime/test-archive");
        Path binFolder = runtimeFolder.resolve("bin");
        Assertions.assertTrue(Files.exists(binFolder));
        Assertions.assertTrue(Files.isDirectory(binFolder));

        Path scriptPath = binFolder.resolve("minho.sh");
        Assertions.assertTrue(Files.exists(scriptPath));
        Assertions.assertFalse(Files.isDirectory(scriptPath));
        Assertions.assertTrue(Files.isExecutable(scriptPath));

        Path archivePath = runtimeFolder.resolve("test-archive.zip");
        Assertions.assertTrue(Files.exists(archivePath));
    }

}
