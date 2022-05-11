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
package org.apache.karaf.rest.jersey;

import lombok.extern.java.Log;
import org.apache.karaf.boot.service.KarafConfigService;
import org.apache.karaf.boot.service.ServiceRegistry;
import org.apache.karaf.boot.spi.Service;
import org.apache.karaf.web.jetty.JettyWebContainerService;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;

@Log
public class JerseyRestService implements Service {

    public final static String JERSEY_PATH = "jersey.path";
    public final static String JERSEY_PACKAGES = "jersey.packages";

    private String restPath;
    private String restPackages;

    @Override
    public String name() {
        return "jersey-rest";
    }

    @Override
    public void onRegister(ServiceRegistry serviceRegistry) throws Exception {
        KarafConfigService config = serviceRegistry.get(KarafConfigService.class);
        if (config == null) {
            log.warning("KarafConfigService is not found in the service registry");
        }

        // get jetty service from the registry
        JettyWebContainerService webContainerService = serviceRegistry.get(JettyWebContainerService.class);
        if (webContainerService == null) {
            throw new IllegalStateException("JettyWebContainerService is not found in the service registry");
        }

        restPath = (config != null && config.getProperties().get(JERSEY_PATH) != null) ? config.getProperties().get(JERSEY_PATH).toString() : "/rest/*";
        if (config != null && config.getProperties().get(JERSEY_PACKAGES) != null) {
            restPackages = config.getProperties().get(JERSEY_PACKAGES).toString();
        } else {
            throw new IllegalStateException("jersey.packages configuration is not found in the KarafConfigService");
        }

        log.info("Starting JerseyRestService");
        log.info("\tpath: " + restPath);
        log.info("\tpackages: " + restPackages);
        ServletHolder servletHolder = webContainerService.addServlet(ServletContainer.class, restPath);
        servletHolder.setInitOrder(1);
        servletHolder.setInitParameter("jersey.config.server.provider.packages", restPackages);
    }

    public String getRestPath() {
        return this.restPath;
    }

    public String getRestPackages() {
        return this.restPackages;
    }

}
