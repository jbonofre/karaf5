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
package org.apache.karaf.boot.spi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.karaf.boot.config.KarafConfig;
import org.apache.karaf.boot.service.ServiceRegistry;

import java.util.Locale;

/**
 * Generic Karaf Service.
 */
public interface Service {

    int DEFAULT_PRIORITY = 1000;

    /**
     * Register a service in the Karaf registry.
     * @param registration the registration provides KarafConfig and ServiceRegistry.
     * @throws Exception if register fails.
     */
    default void onRegister(final Registration registration) throws Exception {
        // no-op, service
    }

    /**
     * Retrieve the service priority (allow services sorting).
     * Default is 1000.
     * @return the service priority.
     */
    default int priority() {
        return DEFAULT_PRIORITY;
    }

    /**
     * Retrieve the service name.
     * Default is the service simple class name.
     * @return the service name.
     */
    default String name() {
        return getClass().getSimpleName().toLowerCase(Locale.ROOT).replaceAll("Service", "");
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    class Registration {
        private KarafConfig config;
        private ServiceRegistry registry;
    }

}
