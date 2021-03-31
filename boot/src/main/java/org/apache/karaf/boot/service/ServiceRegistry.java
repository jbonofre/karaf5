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
package org.apache.karaf.boot.service;

import lombok.extern.java.Log;
import org.apache.karaf.boot.config.KarafConfig;
import org.apache.karaf.boot.spi.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main Karaf service registry.
 */
@Log
public class ServiceRegistry implements AutoCloseable {

    private final Map<Class<?>, Service> registry = new ConcurrentHashMap<>();

    /**
     * Retrieve a service from the registry.
     * @param serviceClass the service class identifying the service.
     * @param <T> the service type.
     * @return the service instance from the registry.
     */
    public <T> T get(final Class<T> serviceClass) {
        return (T) registry.get(serviceClass);
    }

    /**
     * Register a service in the registry.
     * @param service the service to add in the registry.
     * @return true if the service has been added, false else.
     */
    public boolean add(final Service service) {
        return registry.putIfAbsent(service.getClass(), service) == null;
    }

    /**
     * Remove a service from the registry.
     * @param service the service to remove.
     */
    public void remove(final Service service) {
        registry.remove(service.getClass(), service);
    }

    /**
     * Close (stop) the service registry.
     */
    @Override
    public void close() {
        log.info("Closing service registry");
        final IllegalStateException ise = new IllegalStateException("Can't stop service registry");
        registry.values().stream() // we should filter only for lifecycle service as others must use it
                .filter(AutoCloseable.class::isInstance)
                .map(AutoCloseable.class::cast)
                .forEach(service -> {
                    try {
                        service.close();
                    } catch (final Exception e) {
                        ise.addSuppressed(e);
                    }
                });
        if (ise.getSuppressed().length > 0) {
            throw ise;
        }
    }

    public void start(final KarafConfig config) {
        log.info("Starting service registry");
        final IllegalStateException ise = new IllegalStateException("Can't register service");
        registry.values().forEach(service -> {
            try {
                log.info("Registering " + service.name() + " service");
                service.onRegister(new Service.Registration(config, this));
            } catch (final Exception e) {
                ise.addSuppressed(e);
            }
        });
        if (ise.getSuppressed().length > 0) {
            throw ise;
        }
        log.info("Starting services");
        get(KarafLifeCycleService.class).start();
    }

}
