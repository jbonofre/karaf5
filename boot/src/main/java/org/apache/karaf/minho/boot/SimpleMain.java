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

import org.apache.karaf.minho.boot.service.ConfigService;

import java.util.concurrent.CountDownLatch;

public class SimpleMain {
    private SimpleMain() {
        // no-op
    }

    public static void main(final String... args) throws Exception {
        try (final var instance = Minho.builder().build().start()) {
            final var registry = instance.getServiceRegistry();
            final var awaiter = registry.get(Awaiter.class);
            if (awaiter != null) {
                awaiter.await();
            } else if (Boolean.getBoolean(registry.get(ConfigService.class).property("minho.awaiter.implicit", "false"))) {
                new CountDownLatch(1).await();
            }
        }
    }

    /**
     * Service implementation enabling the main to not quit immediately if set.
     * Often used for servers.
     */
    public interface Awaiter {
        /**
         * Wait until the application completes and can exit.
         */
        void await();
    }
}
