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
import org.apache.karaf.boot.config.KarafConfig;
import org.apache.karaf.boot.service.KarafConfigService;
import org.apache.karaf.boot.service.KarafLifeCycleService;
import org.apache.karaf.boot.service.ServiceRegistry;
import org.apache.karaf.boot.spi.Service;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.startlevel.FrameworkStartLevel;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarInputStream;

import static java.util.stream.Collectors.toMap;

@Log
public class OsgiApplicationManagerService implements Service {

    private final static String PREFIX = "osgi.";

    private final static String STORAGE_PROPERTY = "storageDirectory";
    private final static String CLEAR_CACHE_PROPERTY = "clearCache";
    private final static String START_LEVEL_PROPERTY = "startLevel";
    private final static String BUNDLE_START_LEVEL_PROPERTY = "bundleStartLevel";
    private final static String LOG_LEVEL_PROPERTY = "logLevel";
    private final static String CACHE_PROPERTY = "cache";

    private Framework framework = null;

    private final Map<String, String> store = new ConcurrentHashMap<>();

    @Override
    public String name() {
        return "karaf-osgi";
    }

    @Override
    public int priority() {
        return DEFAULT_PRIORITY + 99;
    }

    @Override
    public void onRegister(final ServiceRegistry serviceRegistry) throws Exception {
        log.info("Starting OSGi application manager service");
        log.info("Creating OSGi framework runtime");
        Map<String, Object> frameworkConfig = new HashMap<>();

        // looking for service properties
        final Map<String, Object> properties = serviceRegistry.get(KarafConfigService.class).getProperties().entrySet()
                .stream().filter(entry -> entry.getKey().startsWith(PREFIX))
                .collect(toMap(entry -> entry.getKey().substring(PREFIX.length()), Map.Entry::getValue));

        // cache
        String cache;
        if (properties.get(STORAGE_PROPERTY) != null) {
            cache = properties.get(STORAGE_PROPERTY).toString();
        } else {
            cache = "./osgi";
        }
        log.info("OSGi framework storage: " + cache);
        frameworkConfig.put(Constants.FRAMEWORK_STORAGE, cache);
        // clear cache
        if (properties.get(CLEAR_CACHE_PROPERTY) != null) {
            frameworkConfig.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
        }
        // start level
        if (properties.get(START_LEVEL_PROPERTY) != null) {
            frameworkConfig.put(Constants.FRAMEWORK_BEGINNING_STARTLEVEL, properties.get(START_LEVEL_PROPERTY));
        } else {
            frameworkConfig.put(Constants.FRAMEWORK_BEGINNING_STARTLEVEL, "100");
        }
        // bundle start level
        int bundleStartLevel = 80;
        if (properties.get(BUNDLE_START_LEVEL_PROPERTY) != null) {
            bundleStartLevel = (int) properties.get(BUNDLE_START_LEVEL_PROPERTY);
        }
        // log level
        if (properties.get(LOG_LEVEL_PROPERTY) != null) {
            frameworkConfig.put(FelixConstants.LOG_LEVEL_PROP, properties.get(LOG_LEVEL_PROPERTY));
        } else {
            frameworkConfig.put(FelixConstants.LOG_LEVEL_PROP, "3");
        }
        // cache
        String cacheRootDir;
        if (properties.get(CACHE_PROPERTY) != null) {
            cacheRootDir = properties.get(CACHE_PROPERTY).toString();
        } else {
            cacheRootDir = "./osgi/bundles";
        }
        log.info("OSGi bundles cache: " + cacheRootDir);
        frameworkConfig.put(BundleCache.CACHE_ROOTDIR_PROP, cacheRootDir);

        FrameworkFactory frameworkFactory = new FrameworkFactory();
        framework = frameworkFactory.newFramework(frameworkConfig);

        framework.init();
        framework.start();

        FrameworkStartLevel frameworkStartLevel = framework.adapt(FrameworkStartLevel.class);
        frameworkStartLevel.setInitialBundleStartLevel(bundleStartLevel);

        log.info("Registering service into Karaf lifecycle");
        KarafLifeCycleService karafLifeCycleService = serviceRegistry.get(KarafLifeCycleService.class);
        karafLifeCycleService.onStart(() -> {
            serviceRegistry.get(KarafConfig.class).getApplications().forEach(application -> {
                try {
                    if (application.getType() == null && canHandle(application.getUrl())) {
                        store.put(start(application.getUrl()), application.getUrl());
                    } else if (application.getType().equals(name())) {
                        store.put(start(application.getUrl()), application.getUrl());
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Can't start OSGi application " + application.getUrl(), e);
                }
            });
        });
        karafLifeCycleService.onShutdown(() -> {
            store.keySet().forEach(id -> {
                try {
                    stop(id);
                } catch (Exception e) {
                    throw new RuntimeException("Can't stop OSGi application " + id, e);
                }
            });
        });
    }

    private boolean canHandle(String url) {
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

    public String start(String url) throws Exception {
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
        return bundle.getLocation();
    }

    public void stop(String location) throws Exception {
        log.info("Stopping OSGi application " + location);
        Bundle bundle = framework.getBundleContext().getBundle(location);
        if (bundle == null) {
            throw new IllegalArgumentException("OSGi application " + location + " not found");
        }
        try {
            bundle.uninstall();
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to stop OSGi application " + location, e);
        }
    }

}
