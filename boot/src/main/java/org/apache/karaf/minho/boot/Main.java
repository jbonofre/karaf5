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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Predicate;
import java.util.logging.Logger;

import static java.util.Optional.ofNullable;

public class Main {
    private Main() {
        // no-op
    }

    public static void main(final String... args) throws Exception {
        final boolean minhoJar = Boolean.parseBoolean(System.getenv("MINHO_JAR")) || Boolean.parseBoolean(System.getProperty("minho.jar"));

        final var log = Logger.getLogger(Main.class.getName());
        if (!minhoJar) {
            log.info("Starting runtime in exploded mode");
            // try to load classpath
            final var minhoLib = ofNullable(System.getProperty("minho.lib"))
                    .orElseGet(() -> System.getProperty("user.dir"));
            System.out.println("Minho lib: " + minhoLib);
            final var libFolder = Paths.get(minhoLib);
            try (final var walk = Files.walk(libFolder)) {
                final var urls = walk
                        .filter(Predicate.not(Files::isDirectory))
                        .map(it -> {
                            try {
                                return it.toUri().toURL();
                            } catch (final MalformedURLException e) {
                                throw new IllegalStateException(e);
                            }
                        })
                        .toArray(URL[]::new);
                final var classLoader = new URLClassLoader(urls);
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try {
                        classLoader.close();
                    } catch (final IOException e) {
                        // no-op, not critical
                    }
                }, Main.class.getName() + "-classloader-close"));
                Thread.currentThread().setContextClassLoader(classLoader);
            }
        } else {
            log.info("Starting runtime in uber jar mode");
        }
        SimpleMain.main(args);
    }
}
