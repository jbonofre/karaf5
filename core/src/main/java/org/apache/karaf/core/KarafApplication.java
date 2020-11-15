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
import org.osgi.framework.startlevel.FrameworkStartLevel;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class KarafApplication {

    private static final Logger logger = Logger.getLogger(KarafApplication.class.getName());

    private KarafConfig config;
    private Framework fwk = null;

    public KarafApplication(KarafConfig config) {
        this.config = config;
    }

    public static KarafApplication withConfig(KarafConfig config) {
        return new KarafApplication(config);
    }

    public void run() {
        logger.info("Starting Karaf Application...");
        FrameworkFactory fwkFactory = new FrameworkFactory();
        Map<String, String> fwkConfig = new HashMap<>();
        fwkConfig.put(Constants.FRAMEWORK_STORAGE, config.cache);
        if (config.clearCache) {
            fwkConfig.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
        }
        fwk = fwkFactory.newFramework(fwkConfig);

        try {
            fwk.init();
            fwk.start();
        } catch (BundleException e) {
            throw new RuntimeException(e);
        }

        FrameworkStartLevel sl = fwk.adapt(FrameworkStartLevel.class);
        sl.setInitialBundleStartLevel(config.defaultBundleStartlevel);
        logger.info("Karaf started!");
    }

}
