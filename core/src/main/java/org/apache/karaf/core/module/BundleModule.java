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
package org.apache.karaf.core.module;

import lombok.extern.java.Log;
import org.apache.karaf.core.Karaf;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.startlevel.BundleStartLevel;

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
                bundle.adapt(BundleStartLevel.class).setStartLevel(startLevel);
            } catch (Exception e) {
                throw new Exception("Unable to install OSGi bundle module " + url + ": " + e.toString(), e);
            }
            log.info("Starting OSGi bundle module " + bundle.getSymbolicName() + "/" + bundle.getVersion());
            try {
                // framework.adapt(FrameworkWiring.class).resolveBundles(null);
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
            Karaf.modules.add(moduleModel);
        } finally {
            Karaf.modulesLock.writeLock().unlock();
        }
    }

}
