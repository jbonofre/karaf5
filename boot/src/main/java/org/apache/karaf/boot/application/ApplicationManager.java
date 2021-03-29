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
package org.apache.karaf.boot.application;

import lombok.extern.java.Log;
import org.apache.karaf.boot.config.KarafConfig;
import org.apache.karaf.boot.config.Service;
import org.apache.karaf.boot.spi.ApplicationManagerService;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

@Log
public class ApplicationManager {

    // TODO id:url:manager
    private final static Map<String, String> applications = new ConcurrentHashMap<>();

    private final KarafConfig karafConfig;
    private final ServiceLoader<ApplicationManagerService> managers;

    public ApplicationManager(KarafConfig karafConfig) {
        this.karafConfig = karafConfig;
        this.managers = ServiceLoader.load(ApplicationManagerService.class);

        managers.forEach(manager -> {
            log.info("Starting applications manager " + manager.getName());
            try {
                manager.init(getConfig(manager.getName()));
            } catch (Exception e) {
                log.warning("Can't start applications manager " + manager.getName() + ": " + e);
                throw new RuntimeException("Can't start applications manager " + manager.getName(), e);
            }
        });
    }

    private Map<String, Object> getConfig(String manager) {
        if (this.karafConfig != null && this.karafConfig.getLauncher() != null && this.karafConfig.getLauncher().getManagers() != null) {
            for (Service service : this.karafConfig.getLauncher().getManagers()) {
                if (service.getName().equals(manager)) {
                    return service.getProperties();
                }
            }
        }
        return null;
    }

    public void start(String url, Map<String, Object> properties) throws Exception {
        start(url, null, properties);
    }

    public void start(String url, String name, Map<String, Object> properties) throws Exception {
        if (applications.containsValue(url)) {
            throw new IllegalStateException("Application " + url + " already exists");
        }
        managers.forEach(manager -> {
            try {
                if (name == null) {
                    if (manager.canHandle(url)) {
                        log.info("Starting application " + url + " via manager " + manager.getName());
                        String id = manager.start(url, properties);
                        applications.put(id, manager.getName());
                    }
                } else {
                    if (manager.getName().equals(name)) {
                        log.info("Starting application " + url + " via manager " + manager.getName());
                        String id = manager.start(url, properties);
                        applications.put(id, manager.getName());
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Can't start application " + url, e);
            }
        });
    }

    public void stop(String id) throws Exception {
        if (!applications.containsKey(id)) {
            throw new IllegalArgumentException("Application " + id + " doesn't exist");
        }
        String managerName = applications.get(id);
        managers.forEach(manager -> {
            if (manager.getName().equals(managerName)) {
                try {
                    manager.stop(id);
                } catch (Exception e) {
                    throw new RuntimeException("Can't stop application " + id, e);
                }
            }
        });
        applications.remove(id);
    }

}
