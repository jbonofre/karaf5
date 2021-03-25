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

import org.osgi.framework.launch.Framework;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ModuleManager {

    private final static Map<String, String> modules = new ConcurrentHashMap<>();

    private final OsgiModuleService osgiModuleService;
    private final SpringBootModuleService springBootModuleService;

    public ModuleManager(Framework framework) {
        this.osgiModuleService = new OsgiModuleService(framework);
        this.springBootModuleService = new SpringBootModuleService();
    }

    public void add(String url, String ... args) throws Exception {
        if (modules.containsValue(url)) {
            throw new IllegalStateException("Module " + url + " already exists");
        }
        String id;
        if (osgiModuleService.canHandle(url)) {
            id = osgiModuleService.add(url, args);
        } else if (springBootModuleService.canHandle(url)) {
            id = springBootModuleService.add(url, args);
        } else {
            throw new IllegalArgumentException("Not supported module " + url);
        }
        modules.put(id, url);
    }

    public void remove(String id) throws Exception {
        if (!modules.containsKey(id)) {
            throw new IllegalArgumentException("Module " + id + " doesn't exist");
        }
        osgiModuleService.remove(id);
        springBootModuleService.remove(id);
        modules.remove(id);
    }

    public String getId(String url) {
        for (String id : modules.keySet()) {
            if (modules.get(id).equals(url)) {
                return id;
            }
        }
        return null;
    }

    public String getLocation(String id) {
        return modules.get(id);
    }

}
