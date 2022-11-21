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

import org.apache.karaf.minho.boot.Minho;
import org.apache.karaf.minho.boot.service.ConfigService;
import org.apache.karaf.minho.boot.service.LifeCycleService;
import org.apache.karaf.minho.extractor.ExtractorService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class ExtractorServiceTest {

    @Test
    @Disabled
    public void singleDefault() {
        ConfigService config = new ConfigService();
        config.getProperties().put("extractor.target", "./target/extracted/single");
        try (final var minho = Minho.builder().loader(() -> Stream.of(config, new LifeCycleService(), new ExtractorService())).build().start()) {
            Assertions.assertTrue(Files.exists(Paths.get("./target/extracted/single/bin")));
            Assertions.assertTrue(Files.exists(Paths.get("./target/extracted/single/bin/client")));
            Assertions.assertTrue(Files.exists(Paths.get("./target/extracted/single/system/README")));
        }
    }

    @Test
    @Disabled
    public void multiResources() {
        ConfigService config = new ConfigService();
        config.getProperties().put("extractor.target", "./target/extracted/multi");
        config.getProperties().put("extractor.sources", "resources,META-INF");
        try (final var minho = Minho.builder().loader(() -> Stream.of(config, new LifeCycleService(), new ExtractorService())).build().start()) {
            Assertions.assertTrue(Files.exists(Paths.get("./target/extracted/multi/bin")));
            Assertions.assertTrue(Files.exists(Paths.get("./target/extracted/multi/MANIFEST.MF")));
        }
    }

}
