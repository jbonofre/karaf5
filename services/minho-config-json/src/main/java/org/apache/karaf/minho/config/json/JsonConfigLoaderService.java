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
package org.apache.karaf.minho.config.json;

import lombok.extern.java.Log;
import org.apache.karaf.minho.boot.config.Config;
import org.apache.karaf.minho.boot.service.ServiceRegistry;
import org.apache.karaf.minho.boot.spi.Service;

import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Load Config from a JSON file.
 */
@Log
public class JsonConfigLoaderService implements Service {
    @Override
    public String name() {
        return "minho-config-json-service";
    }

    @Override
    public int priority() {
        return (-DEFAULT_PRIORITY) + 100;
    }

    @Override
    public void onRegister(final ServiceRegistry serviceRegistry) throws Exception {
        final var config = System.getProperty("minho.config");
        if (config != null) {
            log.info(() -> "Loading JSON configuration from " + config);
            try (final var in = Files.newBufferedReader(Path.of(config))) {
                doMerge(serviceRegistry, loadJson(in));
            }
            return;
        }

        final var envConfigFile = System.getenv("MINHO_CONFIG_FILE");
        if (envConfigFile != null) {
            log.info(() -> "Loading JSON configuration from " + envConfigFile);
            try (final var in = Files.newBufferedReader(Path.of(envConfigFile))) {
                doMerge(serviceRegistry, loadJson(in));
            }
            return;
        }

        final var envConfig = System.getenv("MINHO_CONFIG");
        if (envConfig != null) {
            log.info("Loading JSON configuration from MINHO_CONFIG env variable");
            try (final var reader = new StringReader(envConfig)) {
                doMerge(serviceRegistry, loadJson(reader));
            }
            return;
        }

        final var metaInfMinHo = JsonConfigLoaderService.class.getResourceAsStream("/META-INF/minho.json");
        if (metaInfMinHo != null) {
            log.info("Loading JSON configuration from classpath META-INF/minho.json");
            try (final var reader = new InputStreamReader(metaInfMinHo)) {
                doMerge(serviceRegistry, loadJson(reader));
            }
            return;
        }

        final var rootMinho = JsonConfigLoaderService.class.getResourceAsStream("/minho.json");
        if (rootMinho != null) {
            log.info("Loading JSON configuration from classpath minho.json");
            try (final var reader = new InputStreamReader(rootMinho)) {
                doMerge(serviceRegistry, loadJson(reader));
            }
            return;
        }

        log.info("JSON configuration not found");
    }

    private void doMerge(final ServiceRegistry serviceRegistry, final Config config) {
        serviceRegistry.get(Config.class).merge(config);
    }

    private Config loadJson(final Reader reader) throws Exception {
        try (final var jsonb = JsonbBuilder.create(new JsonbConfig().setProperty("johnzon.skip-cdi", true))) {
            return jsonb.fromJson(reader, Config.class);
        }
    }
}
