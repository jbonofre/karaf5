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
package org.apache.karaf.minho.boot;

import lombok.extern.java.Log;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

@Log
public class Main {

    public static final void main(String[] args) throws Exception {
        boolean minhoJar = false;
        minhoJar = (System.getenv("MINHO_JAR") != null) ? System.getenv("MINHO_JAR").equalsIgnoreCase("true") : minhoJar;
        minhoJar = (System.getProperty("minho.jar") != null) ? System.getProperty("minho.jar").equalsIgnoreCase("true") : minhoJar;

        if (!minhoJar) {
            log.info("Starting runtime in exploded mode");
            // try to load classpath
            String minhoLib = (System.getProperty("minho.lib") != null) ? System.getProperty("minho.lib") : System.getProperty("user.dir");
            System.out.println("Minho lib: " + minhoLib);
            Path libFolder = Paths.get(minhoLib);
            ArrayList<URL> urls = new ArrayList<URL>();
            Files.walkFileTree(libFolder, new SimpleFileVisitor<>() {
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
                    if (!Files.isDirectory(file)) {
                        urls.add(file.toFile().toURI().toURL());
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            URLClassLoader classLoader = new URLClassLoader(urls.toArray(new URL[]{}));
            Thread.currentThread().setContextClassLoader(classLoader);
        } else {
            log.info("Starting runtime in uber jar mode");
        }
        Minho.builder().build().start();
    }

}
