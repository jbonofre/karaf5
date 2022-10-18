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

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import java.io.*;

/**
 * Load Config from a JSON file.
 */
@Log
public class JsonConfigLoaderService implements Service {

    private Jsonb jsonb = null;

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
        Config config = null;
        if (System.getenv("MINHO_CONFIG") != null) {
            log.info("Loading JSON configuration from MINHO_CONFIG env variable");
            StringReader reader = new StringReader(System.getenv("MINHO_CONFIG"));
            config = loadJson(new StringReader(System.getenv("MINHO_CONFIG")));
        } else if (System.getenv("MINHO_CONFIG_FILE") != null) {
            log.info("Loading JSON configuration from " + System.getenv("MINHO_CONFIG_FILE"));
            config = loadJson(new FileInputStream(System.getenv("MINHO_CONFIG_FILE")));
        } else if (System.getProperty("minho.config") != null) {
            log.info("Loading JSON configuration from " + System.getProperty("minho.config"));
            config = loadJson(new FileInputStream(System.getProperty("minho.config")));
        } else if (JsonConfigLoaderService.class.getResourceAsStream("/META-INF/minho.json") != null) {
            log.info("Loading JSON configuration from classpath META-INF/minho.json");
            config = loadJson(JsonConfigLoaderService.class.getResourceAsStream("/META-INF/minho.json"));
        } else if (JsonConfigLoaderService.class.getResourceAsStream("/minho.json") != null) {
            log.info("Loading JSON configuration from classpath minho.json");
            config = loadJson(JsonConfigLoaderService.class.getResourceAsStream("/minho.json"));
        } else {
            log.info("JSON configuration not found");
            return;
        }

        final var existing = serviceRegistry.get(Config.class);
        existing.merge(config);
    }

    private Config loadJson(InputStream inputStream) {
        if (jsonb == null) {
            jsonb = JsonbBuilder.create();
        }
        return jsonb.fromJson(inputStream, Config.class);
    }

    private Config loadJson(Reader reader) {
        if (jsonb == null) {
            jsonb = JsonbBuilder.create();
        }
        return jsonb.fromJson(reader, Config.class);
    }

}
