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
package org.apache.karaf.core.extension;

import lombok.extern.java.Log;
import org.apache.karaf.core.extension.model.Module;
import org.apache.karaf.core.extension.model.Extension;
import org.osgi.framework.BundleContext;

import java.io.File;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

@Log
public class Loader {

    public static void load(String url, BundleContext bundleContext) throws Exception {
        JarFile jarFile = new JarFile(new File(url));
        ZipEntry entry = jarFile.getEntry("KARAF-INF/extension.json");
        if (entry == null) {
            throw new IllegalArgumentException(url + " is not a Karaf extension");
        }
        Extension extension = org.apache.karaf.core.extension.model.Loader.read(jarFile.getInputStream(entry));
        log.info("Loading " + extension.getName() + "/" + extension.getVersion() + " extension");
        for (Module module : extension.getModule()) {
            log.info("Installing " + module.getLocation());
            org.osgi.framework.Bundle concreteBundle;
            try {
                concreteBundle = bundleContext.installBundle(module.getLocation());
            } catch (Exception e) {
                throw new Exception("Unable to install module " + module.getLocation() + ": " + e.toString(), e);
            }
            log.info("Starting " + concreteBundle.getSymbolicName() + "/" + concreteBundle.getVersion());
            try {
                concreteBundle.start();
            } catch (Exception e) {
                throw new Exception("Unable to start module " + concreteBundle.getSymbolicName() + "/" + concreteBundle.getVersion() + ": " + e.toString(), e);
            }
        }
    }

}
