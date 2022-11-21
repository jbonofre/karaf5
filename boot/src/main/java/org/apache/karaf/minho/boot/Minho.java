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

import org.apache.karaf.minho.boot.service.ServiceRegistry;
import org.apache.karaf.minho.boot.spi.Service;
import org.apache.karaf.minho.boot.spi.ServiceLoader;
import org.apache.karaf.minho.boot.spi.impl.DefaultLoaderService;

import java.util.Objects;

/**
 * Main Karaf runtime.
 */
public class Minho implements AutoCloseable, Service {
    private final ServiceLoader loader;
    private final ServiceRegistry serviceRegistry = new ServiceRegistry();
    private volatile boolean closed = false;

    protected Minho(final ServiceLoader loader) {
        this.loader = loader;
    }

    public ServiceRegistry getServiceRegistry() {
        return serviceRegistry;
    }

    @Override
    public String name() {
        return "minho";
    }

    @Override
    public int priority() {
        return Integer.MIN_VALUE;
    }

    /**
     * Start the Karaf runtime.
     *
     * @return the Karaf runtime.
     */
    public Minho start() {
        // log format
        if (System.getProperty("java.util.logging.config.file") == null &&
                System.getProperty("java.util.logging.SimpleFormatter.format") == null) {
            System.setProperty("java.util.logging.SimpleFormatter.format",
                    Objects.requireNonNullElse(System.getenv("MINHO_LOG_FORMAT"), "%1$tF %1$tT.%1$tN %4$s [ %2$s ] : %5$s%6$s%n"));
        }
        serviceRegistry.add(this);
        loader.load().forEach(serviceRegistry::add);
        serviceRegistry.start();
        return this;
    }

    /**
     * Close (stop) the Minho runtime.
     */
    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        serviceRegistry.close();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ServiceLoader loader = new DefaultLoaderService();

        public Builder loader(final ServiceLoader loader) {
            this.loader = loader;
            return this;
        }

        public Minho build() {
            return new Minho(loader);
        }
    }
}
