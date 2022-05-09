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
import org.apache.karaf.boot.service.KarafConfigService;
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

    public static final String JETTY_MAX_THREADS = "jetty.maxThreads";
    public static final String JETTY_MIN_THREADS = "jetty.minThreads";
    public static final String JETTY_IDLE_TIMEOUT = "jetty.idleTimeout";
    public static final String JETTY_ACCEPTORS = "jetty.acceptors";
    public static final String JETTY_SELECTORS = "jetty.selectors";
    public static final String JETTY_PORT = "jetty.port";
    public static final String HTTP_PORT = "http.port";
    public static final String JETTY_HOST = "jetty.host";
    public static final String HTTP_HOST = "http.host";
    public static final String JETTY_ACCEPT_QUEUE_SIZE = "jetty.acceptQueueSize";

    private Server server;
    private ServerConnector connector;
    private ServletContextHandler servlets;

    @Override
    public String name() {
        return "jetty-web-container";
    }

    @Override
    public void onRegister(ServiceRegistry serviceRegistry) throws Exception {
        KarafConfigService configService = serviceRegistry.get(KarafConfigService.class);
        if (configService == null) {
            log.warning("KarafConfigService is not found in the registry");
        }

        log.info("Starting Jetty web container");

        int maxThreads = (configService != null && configService.getProperties().get(JETTY_MAX_THREADS) != null) ? Integer.parseInt(configService.getProperties().get(JETTY_MAX_THREADS).toString()) : 200;
        int minThreads = (configService != null && configService.getProperties().get(JETTY_MIN_THREADS) != null) ? Integer.parseInt(configService.getProperties().get(JETTY_MIN_THREADS).toString()) : Math.min(8, maxThreads);
        int idleTimeout = (configService != null && configService.getProperties().get(JETTY_IDLE_TIMEOUT) != null) ? Integer.parseInt(configService.getProperties().get(JETTY_IDLE_TIMEOUT).toString()) : 60000;

        QueuedThreadPool threadPool = new QueuedThreadPool(maxThreads, minThreads, idleTimeout);
        threadPool.setName("jetty-web-container");
        log.info("Creating Jetty queued thread pool jetty-web-container");
        log.info("\tmaxThreads: " + maxThreads);
        log.info("\tminThreads: " + minThreads);
        log.info("\tidleTimeout: " + idleTimeout);

        server = new Server(threadPool);

        int acceptors = (configService != null && configService.getProperties().get(JETTY_ACCEPTORS) != null) ? Integer.parseInt(configService.getProperties().get(JETTY_ACCEPTORS).toString()) : -1;
        int selectors = (configService != null && configService.getProperties().get(JETTY_SELECTORS) != null) ? Integer.parseInt(configService.getProperties().get(JETTY_SELECTORS).toString()) : -1;
        int port = (configService != null && configService.getProperties().get(JETTY_PORT) != null) ? Integer.parseInt(configService.getProperties().get(JETTY_PORT).toString()) : 8080;
        port = (configService != null && configService.getProperties().get(HTTP_PORT) != null) ? Integer.parseInt(configService.getProperties().get(HTTP_PORT).toString()) : port;
        String host = (configService != null && configService.getProperties().get(JETTY_HOST) != null) ? configService.getProperties().get(JETTY_HOST).toString() : "0.0.0.0";
        host = (configService != null && configService.getProperties().get(HTTP_HOST) != null) ? configService.getProperties().get(HTTP_HOST).toString() : host;
        int acceptQueueSize = (configService != null && configService.getProperties().get(JETTY_ACCEPT_QUEUE_SIZE) != null) ? Integer.parseInt(configService.getProperties().get(JETTY_ACCEPT_QUEUE_SIZE).toString()) : 0;

        log.info("Creating Jetty server connector");
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
                connector.close();
                server.stop();
            } catch (Exception e) {
                log.warning("Can't stop Jetty server: " + e.getMessage());
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