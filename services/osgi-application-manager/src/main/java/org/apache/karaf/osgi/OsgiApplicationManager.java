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
package org.apache.karaf.osgi;

import lombok.extern.java.Log;
import org.apache.felix.framework.FrameworkFactory;
import org.apache.felix.framework.cache.BundleCache;
import org.apache.felix.framework.util.FelixConstants;
import org.apache.karaf.boot.spi.ApplicationManagerService;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.startlevel.FrameworkStartLevel;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarInputStream;

@Log
public class OsgiApplicationManager implements ApplicationManagerService {

    private final static String STORAGE_PROPERTY = "storageDirectory";
    private final static String CLEAR_CACHE_PROPERTY = "clearCache";
    private final static String START_LEVEL_PROPERTY = "startLevel";
    private final static String BUNDLE_START_LEVEL_PROPERTY = "bundleStartLevel";
    private final static String LOG_LEVEL_PROPERTY = "logLevel";
    private final static String CACHE_PROPERTY = "cache";

    private Framework framework = null;

    @Override
    public String getName() {
        return "osgi";
    }

    @Override
    public void init(Map<String, Object> properties) throws Exception {
        log.info("Starting OSGi application manager");
        log.info("Creating OSGi framework runtime");
        Map<String, Object> frameworkConfig = new HashMap<>();

        // cache
        String cache;
        if (properties != null && properties.get(STORAGE_PROPERTY) != null) {
            cache = properties.get(STORAGE_PROPERTY).toString();
        } else {
            cache = "./osgi";
        }
        frameworkConfig.put(Constants.FRAMEWORK_STORAGE, cache);
        // clear cache
        if (properties != null && properties.get(CLEAR_CACHE_PROPERTY) != null) {
            frameworkConfig.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
        }
        // start level
        if (properties != null && properties.get(START_LEVEL_PROPERTY) != null) {
            frameworkConfig.put(Constants.FRAMEWORK_BEGINNING_STARTLEVEL, properties.get(START_LEVEL_PROPERTY));
        } else {
            frameworkConfig.put(Constants.FRAMEWORK_BEGINNING_STARTLEVEL, "100");
        }
        // bundle start level
        int bundleStartLevel = 80;
        if (properties != null && properties.get(BUNDLE_START_LEVEL_PROPERTY) != null) {
            bundleStartLevel = (int) properties.get(BUNDLE_START_LEVEL_PROPERTY);
        }
        // log level
        if (properties != null && properties.get(LOG_LEVEL_PROPERTY) != null) {
            frameworkConfig.put(FelixConstants.LOG_LEVEL_PROP, properties.get(LOG_LEVEL_PROPERTY));
        } else {
            frameworkConfig.put(FelixConstants.LOG_LEVEL_PROP, "3");
        }
        // cache
        if (properties != null && properties.get(CACHE_PROPERTY) != null) {
            frameworkConfig.put(BundleCache.CACHE_ROOTDIR_PROP, properties.get(CACHE_PROPERTY));
        } else {
            frameworkConfig.put(BundleCache.CACHE_ROOTDIR_PROP, "./osgi/bundles");
        }

        FrameworkFactory frameworkFactory = new FrameworkFactory();
        framework = frameworkFactory.newFramework(frameworkConfig);

        framework.init();
        framework.start();

        FrameworkStartLevel frameworkStartLevel = framework.adapt(FrameworkStartLevel.class);
        frameworkStartLevel.setInitialBundleStartLevel(bundleStartLevel);
    }

    @Override
    public boolean canHandle(String url) {
        try {
            try (JarInputStream jarInputStream = new JarInputStream(new URL(url).openStream())) {
                if (jarInputStream.getManifest().getMainAttributes().getValue("Bundle-Version") != null) {
                    return true;
                }
            }
        } catch (Exception e) {
            log.warning("Checking application " + url + " failed: " + e);
        }
        return false;
    }

    @Override
    public String start(String url, Map<String, Object> properties) throws Exception {
        log.info("Starting OSGi application " + url);
        Bundle bundle;
        try {
            bundle = framework.getBundleContext().installBundle(url);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to install OSGi application " + url, e);
        }
        log.info("Activating OSGi application " + bundle.getSymbolicName() + "/" + bundle.getVersion());
        try {
            if (bundle.getHeaders().get(Constants.FRAGMENT_HOST) == null) {
                bundle.start();
            } else {
                log.info("OSGi application " + bundle.getSymbolicName() + "/" + bundle.getVersion() + " is a fragment, so not started");
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to start OSGi application " + bundle.getSymbolicName() + "/" + bundle.getVersion(), e);
        }
        return Long.toString(bundle.getBundleId());
    }

    @Override
    public void stop(String id) throws Exception {
        log.info("Stopping OSGi application " + id);
        Bundle bundle = framework.getBundleContext().getBundle(id);
        if (bundle == null) {
            throw new IllegalArgumentException("OSGi application " + id + " not found");
        }
        try {
            bundle.uninstall();
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to stop OSGi application " + id, e);
        }
    }

}
