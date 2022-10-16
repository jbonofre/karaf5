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
package org.apache.karaf.minho.tooling.common.maven;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ParserTest {

    @Test
    public void mvnToPath() throws Exception {
        Assertions.assertEquals("org/apache/karaf/minho/minho-boot/LATEST/minho-boot-LATEST.jar",
                Parser.pathFromMaven("mvn:org.apache.karaf.minho/minho-boot"));

        Assertions.assertEquals("org/apache/karaf/minho/minho-boot/1.1/minho-boot-1.1.jar",
                Parser.pathFromMaven("mvn:org.apache.karaf.minho/minho-boot/1.1"));

        Assertions.assertEquals("org/apache/karaf/minho/minho-boot/1.1/minho-boot-1.1.zip",
                Parser.pathFromMaven("mvn:org.apache.karaf.minho/minho-boot/1.1/zip"));

        Assertions.assertEquals("http://myrepository.nanthrax.net/maven2/org/apache/karaf/minho/minho-boot/1.1/minho-boot-1.1.jar",
                Parser.pathFromMaven("mvn:http://myrepository.nanthrax.net/maven2!org.apache.karaf.minho/minho-boot/1.1/jar"));

        Assertions.assertEquals("file:/path/to/my/repo/org/apache/karaf/minho/minho-boot/1.1/minho-boot-1.1.jar",
                Parser.pathFromMaven("mvn:file:/path/to/my/repo!org.apache.karaf.minho/minho-boot/1.1/jar"));

        String resolved = Parser.pathFromMaven("mvn:org.apache.karaf.minho/minho-boot/1.0-SNAPSHOT");
        resolved = "file:" + System.getProperty("user.home") + "/.m2/repository/" + resolved;
        String assertPath = "file:" + System.getProperty("user.home") + "/.m2/repository/org/apache/karaf/minho/minho-boot/1.0-SNAPSHOT/minho-boot-1.0-SNAPSHOT.jar";
        Assertions.assertEquals(assertPath, resolved);
    }

    @Test
    public void dependenciesTest() throws Exception {
        File resolved = Parser.resolve("mvn:org.apache.karaf.minho/minho-banner/1.0-SNAPSHOT/pom");
        List<String> dependencies = new ArrayList<>();
        Parser.getDependencies(resolved, dependencies, false);

        final AtomicBoolean found = new AtomicBoolean(false);
        dependencies.forEach(dependency -> {
            if (dependency.equals("mvn:org.apache.karaf.minho/minho-boot/1.0-SNAPSHOT/jar")) {
                found.set(true);
            }
        });

        Assertions.assertTrue(found.get());
    }

    @Test
    public void transitiveDependenciesTest() throws Exception {
        File resolved = Parser.resolve("mvn:org.apache.karaf.minho/minho-http/1.0-SNAPSHOT/pom");
        List<String> dependencies = new ArrayList<>();
        Parser.getDependencies(resolved, dependencies, true);

        final AtomicBoolean found = new AtomicBoolean(false);

        dependencies.forEach(dependency -> {
            System.out.println(dependency);
            if (dependency.equals("mvn:org.eclipse.jetty/jetty-util/11.0.9/jar")) {
                found.set(true);
            }
        });

        Assertions.assertTrue(found.get());
    }

    @Test
    public void resolve() throws Exception {
        File file = Parser.resolve("mvn:org.apache.karaf.minho/minho-http/1.0-SNAPSHOT");
        System.out.println(file.getAbsolutePath());
        file = Parser.resolve("mvn:commons-lang/commons-lang/2.6");
        System.out.println(file.getAbsolutePath());
    }

    @Test
    public void parserTest() throws Exception {
        Parser parser = new Parser("mvn:org.apache.karaf.minho/minho-http/1.0-SNAPSHOT");
        Assertions.assertEquals("org.apache.karaf.minho", parser.getGroup());
        Assertions.assertEquals("minho-http", parser.getArtifact());
        Assertions.assertEquals("jar", parser.getType());
        Assertions.assertEquals("1.0-SNAPSHOT", parser.getVersion());
    }

}
