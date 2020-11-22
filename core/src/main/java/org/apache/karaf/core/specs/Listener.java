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
package org.apache.karaf.core.specs;

import lombok.extern.java.Log;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.SynchronousBundleListener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Log
public class Listener implements SynchronousBundleListener {

    private static boolean debug = false;

    private ConcurrentMap<Long, Map<String, Callable<Class>>> factories = new ConcurrentHashMap<>();

    private BundleContext bundleContext;

    static {
        try {
            String prop = System.getProperty("org.apache.karaf.specs.debug");
            debug = prop != null && !"false".equals(prop);
        } catch (Throwable t) {
            // no-op
        }
    }

    private void debugLog(String msg) {
        if (debug) {
            log.info(msg);
        }
    }

    public synchronized void start(BundleContext bundleContext) throws Exception {
        log.info("Starting specs listener");
        this.bundleContext = bundleContext;
        debugLog("Starting bundle listener");
        bundleContext.addBundleListener(this);
        debugLog("Checking existing bundles");
        for (Bundle bundle : bundleContext.getBundles()) {
            if (bundle.getState() == Bundle.RESOLVED || bundle.getState() == Bundle.STARTING
                || bundle.getState() == Bundle.ACTIVE || bundle.getState() == Bundle.STOPPING) {
                register(bundle);
            }
        }
        debugLog("Specs listener is activated");
    }

    public synchronized void stop() throws Exception {
        debugLog("Deactivating specs listener");
        if (bundleContext != null) {
            bundleContext.removeBundleListener(this);
        }
        while (!factories.isEmpty()) {
            unregister(factories.keySet().iterator().next());
        }
        debugLog("Specs listener is deactivated");
        this.bundleContext = null;
    }

    @Override
    public void bundleChanged(BundleEvent event) {
        synchronized (this) {
            if (bundleContext == null) {
                return;
            }
        }
        if (event.getType() == BundleEvent.RESOLVED) {
            register(event.getBundle());
        } else if (event.getType() == BundleEvent.UNRESOLVED || event.getType() == BundleEvent.UNINSTALLED) {
            unregister(event.getBundle().getBundleId());
        }
    }

    protected void register(final Bundle bundle) {
        debugLog("Checking bundle " + bundle.getBundleId() + " (" + bundle.getSymbolicName() + ")");
        Map<String, Callable<Class>> map = factories.get(bundle.getBundleId());
        Enumeration e = bundle.findEntries("META-INF/services/", "*", false);
        if (e != null) {
            while (e.hasMoreElements()) {
                final URL u = (URL) e.nextElement();
                final String url = u.toString();
                if (url.endsWith("/")) {
                    continue;
                }
                final String factoryId = url.substring(url.lastIndexOf("/") + 1);
                if (map == null) {
                    map = new HashMap<>();
                    factories.put(bundle.getBundleId(), map);
                }
                map.put(factoryId, new BundleFactoryLoader(factoryId, u, bundle));
            }
        }
        if (map != null) {
            for (Map.Entry<String, Callable<Class>> entry : map.entrySet()) {
                debugLog("Registering service " + entry.getKey() + ": " + entry.getValue());
                Locator.register(entry.getKey(), entry.getValue());
            }
        }
    }

    protected void unregister(long bundleId) {
        Map<String, Callable<Class>> map = factories.remove(bundleId);
        if (map != null) {
            for (Map.Entry<String, Callable<Class>> entry : map.entrySet()) {
                debugLog("Unregistering service " + entry.getKey() + ": " + entry.getValue());
                Locator.unregister(entry.getKey(), entry.getValue());
            }
        }
    }

    private class BundleFactoryLoader implements Callable<Class> {

        private final String factoryId;
        private final URL u;
        private final Bundle bundle;
        private volatile Class<?> clazz;

        public BundleFactoryLoader(String factoryId, URL u, Bundle bundle) {
            this.factoryId = factoryId;
            this.u = u;
            this.bundle = bundle;
        }

        @Override
        public Class call() throws Exception {
            try {
                debugLog("Loading factory " + factoryId);
                if (clazz == null) {
                    synchronized (this) {
                        if (clazz == null) {
                            debugLog("Creating factory " + factoryId);
                            try (BufferedReader br = new BufferedReader(new InputStreamReader(u.openStream(), StandardCharsets.UTF_8))) {
                                String factoryClassName = br.readLine();
                                while (factoryClassName != null) {
                                    factoryClassName = factoryClassName.trim();
                                    if (factoryClassName.charAt(0) != '#') {
                                        debugLog("Factory implementation: " + factoryClassName);
                                        clazz = bundle.loadClass(factoryClassName);
                                        return clazz;
                                    }
                                    factoryClassName = br.readLine();
                                }
                            }
                        }
                    }
                }
                return clazz;
            } catch (Exception e) {
                debugLog("Exception while creating factory: " + e);
                throw e;
            } catch (Error e) {
                debugLog("Error while creating factory: " + e);
                throw e;
            }
        }

        @Override
        public String toString() {
            return u.toString();
        }

        @Override
        public int hashCode() {
            return u.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof BundleFactoryLoader) {
                return u.toExternalForm().equals(((BundleFactoryLoader) o).u.toExternalForm());
            } else {
                return false;
            }
        }
    }

}
