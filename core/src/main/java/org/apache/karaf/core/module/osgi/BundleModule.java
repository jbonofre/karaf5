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
package org.apache.karaf.core.module.osgi;

import lombok.extern.java.Log;
import org.apache.karaf.core.Karaf;
import org.apache.karaf.core.module.Module;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.startlevel.BundleStartLevel;
import org.osgi.service.startlevel.StartLevel;

import java.io.File;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

/**
 * OSGi Bundle Module.
 */
@Log
public class BundleModule implements Module {

    private final Framework framework;
    private final int startLevel;

    public BundleModule(Framework framework, int startLevel) {
        this.framework = framework;
        this.startLevel = startLevel;
    }

    @Override
    public boolean canHandle(String url) {
        try {
            if (url.startsWith("http") || url.startsWith("https")) {
                try (JarInputStream jarInputStream = new JarInputStream(new URL(url).openStream())) {
                    if (jarInputStream.getManifest().getMainAttributes().getValue("Bundle-Version") != null) {
                        return true;
                    }
                }
            } else {
                if (url.startsWith("file:")) {
                    url = url.substring("file:".length());
                }
                try (JarFile jarFile = new JarFile(new File(url))) {
                    if (jarFile.getManifest().getMainAttributes().getValue("Bundle-Version") != null) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            // no-op
        }
        return false;
    }

    @Override
    public void add(String url, String ... args) throws Exception {
        Karaf.modulesLock.writeLock().lock();
        try {
            log.info("Installing OSGi bundle module " + url);
            if (!url.startsWith("file:") && !url.startsWith("http:") && !url.startsWith("https:")) {
                url = "file:" + url;
            }
            Bundle bundle;
            try {
                bundle = framework.getBundleContext().installBundle(url);
            } catch (Exception e) {
                throw new Exception("Unable to install OSGi bundle module " + url + ": " + e.toString(), e);
            }
            log.info("Starting OSGi bundle module " + bundle.getSymbolicName() + "/" + bundle.getVersion());
            try {
                if (bundle.getHeaders().get(Constants.FRAGMENT_HOST) == null) {
                    bundle.start();
                }
            } catch (Exception e) {
                throw new Exception("Unable to start OSGi bundle module " + bundle.getSymbolicName() + "/" + bundle.getVersion() + ": " + e.toString(), e);
            }
            org.apache.karaf.core.model.Module moduleModel = new org.apache.karaf.core.model.Module();
            moduleModel.setId(String.valueOf(bundle.getBundleId()));
            moduleModel.setName(bundle.getSymbolicName());
            moduleModel.setLocation(url);
            Enumeration<String> headers = bundle.getHeaders().keys();
            while (headers.hasMoreElements()) {
                String header = headers.nextElement();
                moduleModel.getMetadata().put(header, bundle.getHeaders().get(header));
            }
            moduleModel.getMetadata().put("State", bundle.getState());
            Karaf.modules.put(String.valueOf(bundle.getBundleId()), moduleModel);
        } finally {
            Karaf.modulesLock.writeLock().unlock();
        }
    }

    @Override
    public boolean is(String id) {
        return (framework.getBundleContext().getBundle(Long.parseLong(id)) != null);
    }

    @Override
    public void remove(String bundleId) throws Exception {
        Karaf.modulesLock.writeLock().lock();
        try {
            Bundle bundle = framework.getBundleContext().getBundle(Long.parseLong(bundleId));
            if (bundle == null) {
                throw new IllegalArgumentException("Module " + bundleId + " not found or not an OSGi bundle");
            }
            try {
                bundle.uninstall();
            } catch (Exception e) {
                throw new IllegalStateException("Can't remove OSGi module " + bundleId + ": " + e.getMessage(), e);
            }
            Karaf.modules.remove(bundleId);
            log.info("OSGi module " + bundle.getSymbolicName() + " uninstalled");
        } finally {
            Karaf.modulesLock.writeLock().unlock();
        }
    }

}
