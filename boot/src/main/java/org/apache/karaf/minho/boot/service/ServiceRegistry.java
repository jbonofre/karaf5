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

import org.apache.karaf.minho.boot.spi.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

/**
 * Main service registry.
 */
public class ServiceRegistry implements AutoCloseable {
    private final Logger log = Logger.getLogger(LifeCycleService.class.getName());
    private final Map<Class<?>, Service> registry = new ConcurrentHashMap<>();

    public Map<Class<?>, Service> getAll() {
        return registry;
    }

    /**
     * Retrieve a service from the registry.
     *
     * @param serviceClass the service class identifying the service.
     * @param <T>          the service type.
     * @return the service instance from the registry.
     */
    public <T> T get(final Class<T> serviceClass) {
        return serviceClass.cast(ofNullable(registry.get(serviceClass)) // direct lookup, faster
                .orElseGet(() -> { // fallback (hierarchy)
                    final var selected = findByType(serviceClass).collect(toList());
                    switch (selected.size()) {
                        case 0:
                            return null;
                        case 1:
                            return selected.iterator().next();
                        default:
                            throw new IllegalStateException("Ambiguous service lookup: " + serviceClass);
                    }
                }));
    }

    /**
     * Lookup a stream of service by type.
     *
     * @param serviceClass looked up type.
     * @param <T>          expected type.
     * @return the instances matching the requested type.
     */
    public <T> Stream<Service> findByType(final Class<T> serviceClass) {
        return registry.values().stream().filter(serviceClass::isInstance);
    }

    /**
     * Register a service in the registry.
     *
     * @param service the service to add in the registry.
     * @return true if the service has been added, false else.
     */
    public boolean add(final Service service) {
        if (registry.putIfAbsent(service.getClass(), service) != null) {
            return false;
        }

        log.info(() -> "Adding " + service.name() + " service (" + service.priority() + ")");
        try {
            service.onRegister(this);
        } catch (final Exception e) {
            throw new IllegalStateException("Can't register " + service.name(), e);
        }
        return true;
    }

    /**
     * Remove a service from the registry.
     *
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
        final var ise = new IllegalStateException("Can't stop service registry");
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

    public void start() {
        ofNullable(get(LifeCycleService.class)).ifPresent(it -> {
            log.info("Starting services");
            it.start();
        });
    }

}
