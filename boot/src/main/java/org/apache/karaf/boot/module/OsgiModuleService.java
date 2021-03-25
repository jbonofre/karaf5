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

import lombok.extern.java.Log;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;

import java.net.URL;
import java.util.jar.JarInputStream;

@Log
public class OsgiModuleService implements ModuleService {

    private final Framework framework;

    public OsgiModuleService(Framework framework) {
        this.framework = framework;
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
            log.warning("Checking module " + url + " failed: " + e);
        }
        return false;
    }

    @Override
    public String add(String url, String ... args) throws Exception {
        log.info("Installing OSGi module " + url);
        Bundle bundle;
        try {
            bundle = framework.getBundleContext().installBundle(url);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to install OSGi module " + url, e);
        }
        log.info("Starting OSGi module " + bundle.getSymbolicName() + "/" + bundle.getVersion());
        try {
            if (bundle.getHeaders().get(Constants.FRAGMENT_HOST) == null) {
                bundle.start();
            } else {
                log.info("OSGi module " + bundle.getSymbolicName() + "/" + bundle.getVersion() + " is a fragment, so not started");
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to start OSGi module " + bundle.getSymbolicName() + "/" + bundle.getVersion(), e);
        }
        return Long.toString(bundle.getBundleId());
    }

    @Override
    public void remove(String id) throws Exception {
        log.info("Uninstalling OSGi module " + id);
        Bundle bundle = framework.getBundleContext().getBundle(id);
        if (bundle == null) {
            throw new IllegalArgumentException("OSGi module " + id + " not found");
        }
        try {
            bundle.uninstall();
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to uninstall OSGi module " + id, e);
        }
    }

}
