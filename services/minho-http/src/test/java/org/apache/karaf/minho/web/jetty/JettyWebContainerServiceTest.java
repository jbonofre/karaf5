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
package org.apache.karaf.minho.web.jetty;

import org.apache.karaf.minho.boot.Minho;
import org.apache.karaf.minho.boot.service.ConfigService;
import org.apache.karaf.minho.boot.service.LifeCycleService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Stream;

public class JettyWebContainerServiceTest {

    @Test
    public void testDefaultConfig() throws Exception {
        JettyWebContainerService webContainerService = new JettyWebContainerService();
        try (final var minho = Minho.builder().loader(() -> Stream.of(new ConfigService(), new LifeCycleService(), webContainerService)).build().start()) {

            Assertions.assertEquals(8080, webContainerService.getServerConnector().getPort());
            Assertions.assertEquals("0.0.0.0", webContainerService.getServerConnector().getHost());
            Assertions.assertEquals(0, webContainerService.getServerConnector().getAcceptQueueSize());

        }
    }

    @Test
    public void testSettingsConfig() throws Exception {
        System.setProperty("http.port", "8181");
        System.setProperty("http.host", "127.0.0.1");
        System.setProperty("http.acceptQueueSize", "10");

        JettyWebContainerService webContainerService = new JettyWebContainerService();
        try (final var minho = Minho.builder().loader(() -> Stream.of(new ConfigService(), new LifeCycleService(), webContainerService)).build().start()) {

            Assertions.assertEquals(8181, webContainerService.getServerConnector().getPort());
            Assertions.assertEquals("127.0.0.1", webContainerService.getServerConnector().getHost());
            Assertions.assertEquals(10, webContainerService.getServerConnector().getAcceptQueueSize());

        }

        System.clearProperty("http.port");
        System.clearProperty("http.host");
        System.clearProperty("http.acceptQueueSize");
    }

    @Test
    public void addServletAsService() throws Exception {
        ConfigService config = new ConfigService();
        JettyWebContainerService webContainerService = new JettyWebContainerService();
        try (final var minho = Minho.builder().loader(() -> Stream.of(config, new LifeCycleService(), new TestServlet(), webContainerService)).build().start()) {

            verify("/test");

        }
    }

    @Test
    public void addServletViaMethod() throws Exception {
        ConfigService config = new ConfigService();
        JettyWebContainerService webContainerService = new JettyWebContainerService();
        try (final var minho =  Minho.builder().loader(() -> Stream.of(config, new LifeCycleService(), webContainerService)).build().start()) {

            webContainerService.addServlet(TestServlet.class, "/test-method");

            verify("/test-method");

        }
    }

    private void verify(String path) throws Exception {
        URL url = new URL("http://localhost:8080" + path);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setDoInput(true);
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        StringBuilder buffer = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }
        Assertions.assertEquals(buffer.toString(), "<html><head><title>Test</title></head><body>Hello World!</body></html>");
    }

}
