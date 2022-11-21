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

import jakarta.servlet.Servlet;
import lombok.extern.java.Log;
import org.apache.karaf.minho.boot.service.ConfigService;
import org.apache.karaf.minho.boot.service.LifeCycleService;
import org.apache.karaf.minho.boot.service.ServiceRegistry;
import org.apache.karaf.minho.boot.spi.Service;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

@Log
public class JettyWebContainerService implements Service, AutoCloseable {

    public static final String HTTP_MAX_THREADS = "http.maxThreads";
    public static final String HTTP_MIN_THREADS = "http.minThreads";
    public static final String HTTP_IDLE_TIMEOUT = "http.idleTimeout";
    public static final String HTTP_ACCEPTORS = "http.acceptors";
    public static final String HTTP_SELECTORS = "http.selectors";
    public static final String HTTP_PORT = "http.port";
    public static final String HTTP_HOST = "http.host";
    public static final String HTTP_ACCEPT_QUEUE_SIZE = "http.acceptQueueSize";

    private Server server;
    private ServerConnector connector;
    private ServletContextHandler servlets;

    @Override
    public String name() {
        return "minho-http-service";
    }

    @Override
    public void onRegister(ServiceRegistry serviceRegistry) throws Exception {
        ConfigService configService = serviceRegistry.get(ConfigService.class);
        if (configService == null) {
            log.warning("ConfigService is not found in the registry");
        }

        log.info("Starting HTTP service");

        int maxThreads = (configService != null && configService.property(HTTP_MAX_THREADS) != null) ? Integer.parseInt(configService.property(HTTP_MAX_THREADS)) : 200;
        int minThreads = (configService != null && configService.property(HTTP_MIN_THREADS) != null) ? Integer.parseInt(configService.property(HTTP_MIN_THREADS)) : Math.min(8, maxThreads);
        int idleTimeout = (configService != null && configService.property(HTTP_IDLE_TIMEOUT) != null) ? Integer.parseInt(configService.property(HTTP_IDLE_TIMEOUT)) : 60000;

        QueuedThreadPool threadPool = new QueuedThreadPool(maxThreads, minThreads, idleTimeout);
        threadPool.setName("minho-http");
        log.info("Creating HTTP queued thread pool");
        log.info("\tmaxThreads: " + maxThreads);
        log.info("\tminThreads: " + minThreads);
        log.info("\tidleTimeout: " + idleTimeout);

        server = new Server(threadPool);

        int acceptors = (configService != null && configService.property(HTTP_ACCEPTORS) != null) ? Integer.parseInt(configService.property(HTTP_ACCEPTORS)) : -1;
        int selectors = (configService != null && configService.property(HTTP_SELECTORS) != null) ? Integer.parseInt(configService.property(HTTP_SELECTORS)) : -1;
        int port = (configService != null && configService.property(HTTP_PORT) != null) ? Integer.parseInt(configService.property(HTTP_PORT)) : 8080;
        String host = (configService != null && configService.property(HTTP_HOST) != null) ? configService.property(HTTP_HOST) : "0.0.0.0";
        int acceptQueueSize = (configService != null && configService.property(HTTP_ACCEPT_QUEUE_SIZE) != null) ? Integer.parseInt(configService.property(HTTP_ACCEPT_QUEUE_SIZE)) : 0;

        log.info("Creating HTTP server connector");
        log.info("\tacceptors: " + acceptors);
        log.info("\tselectors: " + selectors);
        log.info("\tport: " + port);
        log.info("\thost: " + host);
        log.info("\tacceptQueueSize: " + acceptQueueSize);
        connector = new ServerConnector(server, acceptors, selectors, new HttpConnectionFactory());
        connector.setPort(port);
        connector.setHost(host);
        connector.setAcceptQueueSize(acceptQueueSize);

        server.addConnector(connector);

        servlets = new ServletContextHandler(ServletContextHandler.SESSIONS);
        servlets.setContextPath("/");
        server.setHandler(servlets);

        addServlets(serviceRegistry);

        server.insertHandler(new StatisticsHandler());

        LifeCycleService lifeCycleService = serviceRegistry.get(LifeCycleService.class);
        lifeCycleService.onStart(() -> {
            try {
                server.start();
                // server.join();
            } catch (Exception e) {
                throw new RuntimeException("Can't start HTTP service", e);
            }
        });
        lifeCycleService.onShutdown(() -> {
            try {
                connector.close();
                server.stop();
            } catch (Exception e) {
                log.warning("Can't stop HTTP service: " + e.getMessage());
            }
        });
    }

    public Server getServer() {
        return this.server;
    }

    public ServerConnector getServerConnector() {
        return this.connector;
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

    public ServletHolder addServlet(Class<? extends Servlet> servlet, String contextPath) throws Exception {
        log.info("Adding servlet " + servlet.getClass().getName() + " with context " + contextPath);
        return servlets.addServlet(servlet, contextPath);
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