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
package org.apache.karaf.minho.boot.service;

import org.apache.karaf.minho.boot.Minho;
import org.apache.karaf.minho.boot.spi.impl.DefaultLoaderService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class InstanceSingletonServiceTest {
    @Test
    void singleton() {
        final var holder = new InstanceSingletonService();
        assertNull(InstanceSingletonService.getInstance());
        try (final var minho = Minho.builder()
                .loader(new DefaultLoaderService()
                        .add(holder))
                .build()
                .start()) {
            assertSame(minho, InstanceSingletonService.getInstance());
        }
        assertNull(InstanceSingletonService.getInstance());
    }
}
