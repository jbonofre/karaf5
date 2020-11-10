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
package org.apache.karaf.core;

import org.apache.felix.framework.FrameworkFactory;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class KarafApplication {

    private static final Logger logger = LoggerFactory.getLogger(KarafApplication.class);

    private Config config;
    private Framework fwk = null;

    private KarafApplication(Config config) {
        this.config = config;
    }

    public void run() {
        FrameworkFactory fwkFactory = new FrameworkFactory();
        Map<String, String> fwkConfig = new HashMap<>();
        fwkConfig.put(Constants.FRAMEWORK_STORAGE, config.cache);
        fwkConfig.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
        fwk = fwkFactory.newFramework(fwkConfig);

        try {
            fwk.init();
            fwk.start();
        } catch (BundleException e) {
            throw new RuntimeException(e);
        }

        logger.info("Karaf started!");
    }

    public static class Config {

        public String cache;

        private Config() {
        }

        public Config withCache(String cache) {
            this.cache = cache;
            return this;
        }

        public static Config build() {
            return new Config();
        }

    }

    public static KarafApplication withConfig(Config config) {
        return new KarafApplication(config);
    }
}
