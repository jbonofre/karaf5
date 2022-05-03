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
package org.apache.karaf.web.jetty;

import jakarta.servlet.Servlet;
import lombok.extern.java.Log;
import org.apache.karaf.boot.service.KarafLifeCycleService;
import org.apache.karaf.boot.service.ServiceRegistry;
import org.apache.karaf.boot.spi.Service;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

@Log
public class JettyWebContainerService implements Service, AutoCloseable {

    private Server server;
    private ServletContextHandler servlets;

    @Override
    public String name() {
        return "jetty-web-container";
    }

    @Override
    public void onRegister(ServiceRegistry serviceRegistry) throws Exception {
        log.info("Starting Jetty web container");

        QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setName("jetty-web-container");

        server = new Server(threadPool);

        ServerConnector connector = new ServerConnector(server, 1, 1, new HttpConnectionFactory());
        connector.setPort(8080);
        connector.setHost("0.0.0.0");
        connector.setAcceptQueueSize(128);

        server.addConnector(connector);

        servlets = new ServletContextHandler(ServletContextHandler.SESSIONS);
        servlets.setContextPath("/");
        server.setHandler(servlets);

        addServlets(serviceRegistry);

        server.insertHandler(new StatisticsHandler());

        KarafLifeCycleService karafLifeCycleService = serviceRegistry.get(KarafLifeCycleService.class);
        karafLifeCycleService.onStart(() -> {
            try {
                server.start();
                // server.join();
            } catch (Exception e) {
                throw new RuntimeException("Can't start Jetty server", e);
            }
        });
        karafLifeCycleService.onShutdown(() -> {
            try {
                server.stop();
            } catch (Exception e) {
                log.warning("Can't stop Jetty server: " + e.getMessage());
            }
        });
    }

    public void start() throws Exception {
        server.start();
    }

    public void stop() throws Exception {
        server.stop();
    }

    public void restart() throws Exception {
        stop();
        start();
    }

    @Override
    public void close() throws Exception {
        server.stop();
    }

    public void addServlet(Class<? extends Servlet> servlet, String contextPath) throws Exception {
        log.info("Adding servlet " + servlet.getClass().getName() + " with context " + contextPath);
        servlets.addServlet(servlet, contextPath);
    }

    private void addServlets(ServiceRegistry serviceRegistry) {
        serviceRegistry.getAll().values().stream().forEach(service -> {
            if (service instanceof Servlet) {
                if (!service.properties().containsKey("contextPath")) {
                    log.warning("Servlet " + service.getClass().getName() + " doesn't have contextPath property");
                } else {
                    Servlet servlet = (Servlet) service;
                    String contextPath = service.properties().get("contextPath").toString();
                    log.info("Adding servlet " + servlet.getClass().getName() + " with context " + contextPath);
                    servlets.addServlet(servlet.getClass(), contextPath);
                }
            }
        });
    }

}