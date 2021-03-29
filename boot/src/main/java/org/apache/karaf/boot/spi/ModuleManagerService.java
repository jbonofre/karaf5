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
package org.apache.karaf.boot.spi;

import java.util.Map;

/**
 * Service able to manage one kind of module.
 */
public interface ModuleManagerService {

    /**
     * Initialization hook.
     *
     * @param properties configuration passed to the module manager.
     */
    void init(Map<String, Object> properties) throws Exception;

    /**
     * Get the module manager name.
     *
     * @return module manager name.
     */
    String getName();

    /**
     * Check if an artifact can be handled by this module manager.
     *
     * @param url the artifact location.
     * @return true if the artifact can be handled by this module manager, false else.
     */
    boolean canHandle(String url) throws Exception;

    /**
     * Add a module in this module manager.
     *
     * @param url the location of the module artifact.
     * @param properties the properties used for module handling.
     * @return the module id.
     */
    String add(String url, Map<String, Object> properties) throws Exception;

    /**
     * Remove a module from this module manager.
     *
     * @param id the module id.
     */
    void remove(String id) throws Exception;

}
