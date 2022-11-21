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
package org.apache.karaf.minho.rest.jersey;

import org.apache.karaf.minho.boot.Minho;
import org.apache.karaf.minho.boot.service.ConfigService;
import org.apache.karaf.minho.boot.service.LifeCycleService;
import org.apache.karaf.minho.web.jetty.JettyWebContainerService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Stream;

public class JerseyRestServiceTest {

    @Test
    public void simple() throws Exception {
        System.setProperty("rest.packages", "org.apache.karaf.minho.rest.jersey");

        JerseyRestService jerseyRestService = new JerseyRestService();
        try (final var minho =  Minho.builder().loader(() -> Stream.of(new ConfigService(), new LifeCycleService(), new JettyWebContainerService(), jerseyRestService)).build().start()) {

            Assertions.assertEquals("/rest/*", jerseyRestService.getRestPath());
            Assertions.assertEquals("org.apache.karaf.minho.rest.jersey", jerseyRestService.getRestPackages());

            URL url = new URL("http://localhost:8080/rest/test");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder buffer = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            Assertions.assertEquals("Hello World!", buffer.toString());

        }
    }

}
