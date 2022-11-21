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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Core LifeCycle service responsible for starting the registered (hooked) services.
 */
public class LifeCycleService implements Service, AutoCloseable {
    private final Logger log = Logger.getLogger(LifeCycleService.class.getName());
    private final List<Runnable> startCallbacks = new ArrayList<>();
    private final List<Runnable> shutdownCallbacks = new ArrayList<>();

    @Override
    public String name() {
        return "minho-lifecycle-service";
    }

    @Override
    public int priority() {
        return -DEFAULT_PRIORITY;
    }

    /**
     * Add a start callback in the lifecycle.
     * @param callback The runnable start callback.
     */
    public void onStart(final Runnable callback) {
        startCallbacks.add(callback);
    }

    /**
     * Add a stop callback in the lifecycle.
     * @param callback The runnable stop callback.
     */
    public void onShutdown(final Runnable callback) {
        shutdownCallbacks.add(callback);
    }

    /**
     * Start the lifecycle service.
     */
    public void start() {
        log.info("Starting lifecycle service");
        final IllegalStateException ise = new IllegalStateException("Can't start lifecycle service");
        startCallbacks.forEach(runnable -> {
            try {
                runnable.run();
            } catch (final Exception e) {
                ise.addSuppressed(e);
            }
        });
        if (ise.getSuppressed().length > 0) {
            throw ise;
        }
    }

    /**
     * Close (stop) the lifecycle service.
     */
    @Override
    public void close() {
        log.info("Stopping lifecycle service");
        final IllegalStateException ise = new IllegalStateException("Can't stop lifecycle service");
        shutdownCallbacks.forEach(runnable -> {
            try {
                runnable.run();
            } catch (final Exception e) {
                ise.addSuppressed(e);
            }
        });
        if (ise.getSuppressed().length > 0) {
            throw ise;
        }
    }
}
