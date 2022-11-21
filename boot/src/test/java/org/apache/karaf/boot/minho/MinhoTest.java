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
package org.apache.karaf.boot.minho;

import org.apache.karaf.minho.boot.Minho;
import org.apache.karaf.minho.boot.config.Config;
import org.apache.karaf.minho.boot.service.ConfigService;
import org.apache.karaf.minho.boot.service.LifeCycleService;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MinhoTest {
    @Test
    void simpleRun() throws Exception {
        try (final var minho = Minho.builder().build().start()) {
        }
    }

    @Test
    void simpleRunWithConfig() {
        final ConfigService config = new ConfigService();
        config.getProperties().put("foo", "bar");
        config.getProperties().put("hello", "world");

        try (final var minho = Minho.builder()
                .loader(() -> Stream.of(config, new LifeCycleService()))
                .build()
                .start()) {
            final var configService = minho.getServiceRegistry().get(Config.class);
            assertNotNull(configService);
            assertEquals("bar", configService.property("foo"));
            assertEquals("world", configService.property("hello"));
        }
    }
}
