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
 * Service able to manage one kind of application.
 */
public interface ApplicationManagerService {

    /**
     * Initialization hook.
     *
     * @param properties configuration passed to the application manager.
     */
    void init(Map<String, Object> properties) throws Exception;

    /**
     * Get the application manager name.
     *
     * @return application manager name.
     */
    String getName();

    /**
     * Check if an artifact can be handled by this application manager.
     *
     * @param url the artifact location.
     * @return true if the artifact can be handled by this application manager, false else.
     */
    boolean canHandle(String url) throws Exception;

    /**
     * Add an application in this application manager.
     *
     * @param url the location of the application artifact.
     * @param properties the properties used for application handling.
     * @return the application id.
     */
    String start(String url, Map<String, Object> properties) throws Exception;

    /**
     * Remove an application from this application manager.
     *
     * @param id the application id.
     */
    void stop(String id) throws Exception;

}
