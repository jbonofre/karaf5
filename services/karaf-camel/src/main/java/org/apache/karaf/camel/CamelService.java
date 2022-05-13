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
package org.apache.karaf.camel;

import lombok.extern.java.Log;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.karaf.boot.service.KarafLifeCycleService;
import org.apache.karaf.boot.service.ServiceRegistry;
import org.apache.karaf.boot.spi.Service;

@Log
public class CamelService implements Service {

    private DefaultCamelContext camelContext;

    @Override
    public String name() {
        return "karaf-camel";
    }

    @Override
    public int priority() {
        return 1001;
    }

    @Override
    public void onRegister(ServiceRegistry serviceRegistry) {
        log.info("Creating default CamelContext");
        camelContext = new DefaultCamelContext();
        camelContext.setName("default-camel-context");
        log.info("Looking for RouteBuilder in the registry");
        serviceRegistry.findByType(RouteBuilder.class).forEach(routeBuilder -> {
            try {
                camelContext.addRoutes((RouteBuilder) routeBuilder);
            } catch (Exception e) {
                log.warning("Can't add route in the default CamelContext: " + e.getMessage());
            }
        });
        KarafLifeCycleService lifeCycleService = serviceRegistry.get(KarafLifeCycleService.class);
        lifeCycleService.onStart(() -> {
            camelContext.start();
        });
        lifeCycleService.onShutdown(() -> {
            camelContext.stop();
        });
    }

    public CamelContext getCamelContext() {
        return camelContext;
    }

}
