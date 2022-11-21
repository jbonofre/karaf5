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

import org.apache.karaf.minho.boot.config.Config;
import org.apache.karaf.minho.boot.spi.Service;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

/**
 * Main class loader manager service.
 */
public class ClassLoaderService implements Service, AutoCloseable {

    private final Map<String, URLClassLoader> profiles = new HashMap<>();

    @Override
    public String name() {
        return "minho-classloader-service";
    }

    @Override
    public int priority() {
        return -DEFAULT_PRIORITY;
    }

    @Override
    public void close() {
        profiles.values().forEach(it -> {
            try {
                it.close();
            } catch (final IOException e) {
                // no-op, not critical
            }
        });
    }

    @Override
    public void onRegister(ServiceRegistry serviceRegistry) throws Exception {
        Config configService = serviceRegistry.get(Config.class);
        configService.getProfiles().forEach(profile -> {
            URLClassLoader profileClassLoader = new URLClassLoader(profile.getUrls().toArray(new URL[0]), this.getClass().getClassLoader());
            profiles.put(profile.getName(), profileClassLoader);
        });
    }

    public URLClassLoader getClassLoader(final String profile) {
        return profiles.get(profile);
    }

}
