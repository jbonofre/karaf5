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
package org.apache.karaf.boot.module;

import lombok.extern.java.Log;
import org.apache.karaf.boot.config.KarafConfig;
import org.apache.karaf.boot.config.Service;
import org.apache.karaf.boot.spi.ModuleManagerService;

import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

@Log
public class ModuleManager {

    // id:manager
    private final static Map<String, String> modules = new ConcurrentHashMap<>();

    private final KarafConfig karafConfig;
    private final ServiceLoader<ModuleManagerService> managers;

    public ModuleManager(KarafConfig karafConfig) {
        this.karafConfig = karafConfig;
        this.managers = ServiceLoader.load(ModuleManagerService.class);

        managers.forEach(manager -> {
            log.info("Starting module manager " + manager.getName());
            try {
                manager.init(getConfig(manager.getName()));
            } catch (Exception e) {
                log.warning("Can't start module manager " + manager.getName() + ": " + e);
                throw new RuntimeException("Can't start module manager " + manager.getName(), e);
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

    public void add(String url, Map<String, Object> properties) throws Exception {
        add(url, null, properties);
    }

    public void add(String url, String name, Map<String, Object> properties) throws Exception {
        if (modules.containsValue(url)) {
            throw new IllegalStateException("Module " + url + " already exists");
        }
        managers.forEach(manager -> {
            try {
                if (name == null) {
                    if (manager.canHandle(url)) {
                        log.info("Adding module " + url + " via manager " + manager.getName());
                        String id = manager.add(url, properties);
                        modules.put(id, manager.getName());
                    }
                } else {
                    if (manager.getName().equals(name)) {
                        log.info("Adding module " + url + " via manager " + manager.getName());
                        String id = manager.add(url, properties);
                        modules.put(id, manager.getName());
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Can't add module " + url, e);
            }
        });
    }

    public void remove(String id) throws Exception {
        if (!modules.containsKey(id)) {
            throw new IllegalArgumentException("Module " + id + " doesn't exist");
        }
        String managerName = modules.get(id);
        managers.forEach(manager -> {
            if (manager.getName().equals(managerName)) {
                try {
                    manager.remove(id);
                } catch (Exception e) {
                    throw new RuntimeException("Can't remove module " + id, e);
                }
            }
        });
        modules.remove(id);
    }

}
