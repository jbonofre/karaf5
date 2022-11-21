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
package org.apache.karaf.minho.extractor;

import lombok.extern.java.Log;
import org.apache.karaf.minho.boot.config.Config;
import org.apache.karaf.minho.boot.service.ServiceRegistry;
import org.apache.karaf.minho.boot.spi.Service;

import java.net.URL;
import java.nio.file.*;
import java.util.Collections;

@Log
public class ExtractorService implements Service {

    @Override
    public String name() {
        return "minho-extractor-service";
    }

    @Override
    public void onRegister(ServiceRegistry serviceRegistry) throws Exception {
        log.info("Starting extractor service");
        // looking for extractor configuration
        Config config = serviceRegistry.get(Config.class);
        if (config == null) {
            log.warning("Config service is not registered");
            return;
        }
        String sources = "resources";
        if (config.property("extractor.sources") != null) {
            sources = config.property("extractor.sources").toString();
        }
        String target = ".";
        if (config.property("extractor.target") != null) {
            target = config.property("extractor.target").toString();
        }

        String[] urls = sources.split(",");
        for (String url : urls) {

            log.info("Extracting " + url + " to " + target);

            URL resource = Thread.currentThread().getContextClassLoader().getResource(url);
            if (resource != null) {
                try (FileSystem fs = FileSystems.newFileSystem(resource.toURI(), Collections.emptyMap())) {
                    String finalTarget = target;
                    try (final var walk = Files.walk(fs.getPath("."))) {
                        walk.filter(Files::isRegularFile)
                                .forEach(path -> {
                                    try {
                                        String resourceTarget = path.getParent().toAbsolutePath().toString();
                                        if (resourceTarget.startsWith("/" + url)) {
                                            resourceTarget = resourceTarget.substring(("/" + url).length());
                                            Path directory = Paths.get(finalTarget + Paths.get(resourceTarget));
                                            Files.createDirectories(directory);
                                            Path copy = Paths.get(directory.toAbsolutePath() + "/" + path.getFileName());
                                            Files.copy(path, copy, StandardCopyOption.REPLACE_EXISTING);
                                        }
                                    } catch (Exception e) { // todo: fail?
                                        log.warning("Can't copy " + path.toAbsolutePath() + " to " + finalTarget);
                                        e.printStackTrace();
                                    }
                                });
                    }
                }
            } else {
                log.warning("URL resource '" + url + "' not found!");
            }
        }
    }

}
