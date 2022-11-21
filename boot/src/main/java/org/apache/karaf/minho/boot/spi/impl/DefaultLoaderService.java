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
package org.apache.karaf.minho.boot.spi.impl;

import org.apache.karaf.minho.boot.service.ConfigService;
import org.apache.karaf.minho.boot.spi.Service;
import org.apache.karaf.minho.boot.spi.ServiceLoader;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

public class DefaultLoaderService implements ServiceLoader {
    private final List<Service> custom = new ArrayList<>();

    public DefaultLoaderService add(final Service service) {
        custom.add(service);
        return this;
    }

    @Override
    public Stream<Service> load() {
        final var services = Stream.concat(
                        java.util.ServiceLoader.load(Service.class).stream()
                                .map(java.util.ServiceLoader.Provider::get),
                        custom.stream())
                .collect(toList());
        final var conf = services.stream().filter(ConfigService.class::isInstance).findFirst();
        return services.stream()
                .sorted(Comparator.comparingInt(service -> {
                    final var key = service.name() + ".priority";
                    return conf
                            .map(c -> ofNullable(c.properties().getProperty(key)).map(Integer::parseInt).orElse(service.priority()))
                            .orElseGet(() -> Integer.getInteger(key, service.priority()));
                }));
    }
}
