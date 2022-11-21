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
package org.apache.karaf.minho.jmx;

import lombok.extern.java.Log;
import org.apache.karaf.minho.boot.service.ConfigService;
import org.apache.karaf.minho.boot.service.LifeCycleService;
import org.apache.karaf.minho.boot.service.ServiceRegistry;
import org.apache.karaf.minho.boot.spi.Service;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.util.HashMap;
import java.util.Map;

@Log
public class JmxService implements Service {

    private MBeanServer mBeanServer;

    @Override
    public String name() {
        return "minho-jmx-service";
    }

    @Override
    public void onRegister(ServiceRegistry serviceRegistry) throws Exception {
        ConfigService configService = serviceRegistry.get(ConfigService.class);
        if (configService == null) {
            throw new IllegalStateException("ConfigService is not found");
        }
        LifeCycleService lifeCycleService = serviceRegistry.get(LifeCycleService.class);
        if (lifeCycleService == null) {
            throw new IllegalStateException("LifeCycleService is not found");
        }
        String rmiRegistryHost = configService.property("jmx.rmiRegistryHost", "");
        int rmiRegistryPort = Integer.parseInt(configService.property("jmx.rmiRegistryPort", "1099"));
        String rmiServerHost = configService.property("jmx.rmiServerHost", "0.0.0.0");
        int rmiServerPort = Integer.parseInt(configService.property("jmx.rmiServerPort", "44444"));

        String serviceUrl = configService.property("jmx.serverUrl", "service:jmx:rmi://" + rmiServerHost + ":" + rmiServerPort + "/jndi/rmi://" + rmiRegistryHost + ":" + rmiRegistryPort + "/minho");

        boolean daemon = Boolean.parseBoolean(configService.property("jmx.daemon", "true"));
        boolean threaded = Boolean.parseBoolean(configService.property("jmx.threaded", "true"));
        ObjectName objectName = new ObjectName(configService.property("jmx.objectName", "connector:name=rmi"));
        boolean createRmiRegistry = Boolean.parseBoolean(configService.property("jmx.createRmiRegistry", "true"));
        boolean locateRmiRegistry = Boolean.parseBoolean(configService.property("jmx.locateRmiRegistry", "true"));
        boolean locateExistingMBeanServerIfPossible = Boolean.parseBoolean(configService.property("jmx.locateExistingMBeanServerIfPossible", "true"));

        final MBeanServerFactory mBeanServerFactory = new MBeanServerFactory();
        mBeanServerFactory.setLocateExistingServerIfPossible(locateExistingMBeanServerIfPossible);
        mBeanServerFactory.init();

        mBeanServer = mBeanServerFactory.getServer();

        final ConnectorServerFactory connectorServerFactory = new ConnectorServerFactory();
        connectorServerFactory.setCreate(createRmiRegistry);
        connectorServerFactory.setLocate(locateRmiRegistry);
        connectorServerFactory.setHost(rmiRegistryHost);
        connectorServerFactory.setPort(rmiRegistryPort);
        connectorServerFactory.setServer(mBeanServer);
        connectorServerFactory.setServiceUrl(serviceUrl);
        connectorServerFactory.setRmiServerHost(rmiServerHost);
        connectorServerFactory.setDaemon(daemon);
        connectorServerFactory.setThreaded(threaded);
        connectorServerFactory.setObjectName(objectName);
        Map<String, Object> environment = new HashMap<>();
        connectorServerFactory.setEnvironment(environment);

        lifeCycleService.onStart(() -> {
            try {
                connectorServerFactory.init();
            } catch (Throwable e) {
                log.severe("Can't init JMXConnectorServer: " + e.getMessage());
            }
        });

        lifeCycleService.onShutdown(() -> {
            if (connectorServerFactory != null) {
                try {
                    connectorServerFactory.destroy();
                } catch (Exception e) {
                    log.warning("Error destroying ConnectorServerFactory: " + e.getMessage());
                }
            }
            if (mBeanServerFactory != null) {
                try {
                    mBeanServerFactory.destroy();
                } catch (Exception e) {
                    log.warning("Error destroying MBeanServerFactory: " + e.getMessage());
                }
            }

        });
    }

    public void registerMBean(Object mbean, String name) throws Exception {
        mBeanServer.registerMBean(mbean, new ObjectName(name));
    }

    public MBeanServer getmBeanServer() {
        return this.mBeanServer;
    }

}
