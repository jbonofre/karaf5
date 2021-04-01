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
package org.apache.karaf.config.json;

import lombok.extern.java.Log;
import org.apache.karaf.boot.config.KarafConfig;
import org.apache.karaf.boot.service.KarafConfigService;
import org.apache.karaf.boot.service.ServiceRegistry;
import org.apache.karaf.boot.spi.Service;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Load KarafConfig from a JSON file.
 */
@Log
public class JsonConfigLoaderService implements Service {

    private Jsonb jsonb = null;

    @Override
    public String name() {
        return "json-config-loader";
    }

    @Override
    public int priority() {
        return (-DEFAULT_PRIORITY) + 100;
    }

    @Override
    public void onRegister(final ServiceRegistry serviceRegistry) throws Exception {
        KarafConfig karafConfig = KarafConfig.builder().build();
        if (System.getenv("KARAF_CONFIG") != null) {
            log.info("Loading JSON configuration from " + System.getenv("KARAF_CONFIG"));
            karafConfig = loadJson(new FileInputStream(System.getenv("KARAF_CONFIG")));
        } else if (System.getProperty("karaf.config") != null) {
            log.info("Loading JSON configuration from " + System.getProperty("karaf.config"));
            karafConfig = loadJson(new FileInputStream(System.getProperty("karaf.config")));
        } else if (JsonConfigLoaderService.class.getResourceAsStream("META-INF/karaf.json") != null) {
            log.info("Loading JSON configuration from classpath META-INF/karaf.json");
            karafConfig = loadJson(JsonConfigLoaderService.class.getResourceAsStream("META-INF/karaf.json"));
        } else if (JsonConfigLoaderService.class.getResourceAsStream("/karaf.json") != null) {
            log.info("Loading JSON configuration from classpath karaf.json");
            karafConfig = loadJson(JsonConfigLoaderService.class.getResourceAsStream("/karaf.json"));
        } else {
            log.warning("JSON configuration not found");
        }

        KarafConfigService configService = serviceRegistry.get(KarafConfigService.class);
        configService.setConfig(karafConfig);
    }

    private KarafConfig loadJson(InputStream inputStream) {
        if (jsonb == null) {
            jsonb = JsonbBuilder.create();
        }
        return jsonb.fromJson(inputStream, KarafConfig.class);
    }

}
