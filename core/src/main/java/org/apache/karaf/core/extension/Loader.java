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
import org.apache.karaf.core.Karaf;
import org.apache.karaf.core.extension.model.Module;
import org.apache.karaf.core.extension.model.Extension;
import org.apache.karaf.core.maven.Resolver;
import org.osgi.framework.BundleContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

@Log
public class Loader {

    public static void load(String url, Karaf karaf) throws Exception {
        String resolved = karaf.getResolver().resolve(url);
        if (resolved == null) {
            throw new IllegalArgumentException(url + " not found");
        }
        InputStream inputStream;
        try {
            JarFile jarFile = new JarFile(new File(resolved));
            ZipEntry entry = jarFile.getEntry("KARAF-INF/extension.json");
            if (entry == null) {
                throw new IllegalArgumentException(url + " is not a Karaf extension");
            }
            inputStream = jarFile.getInputStream(entry);
        } catch (ZipException zipException) {
            log.log(Level.FINE, url + " is not a jar file");
            inputStream = new FileInputStream(new File(resolved));
        }
        Extension extension = org.apache.karaf.core.extension.model.Loader.read(inputStream);
        log.info("Loading " + extension.getName() + "/" + extension.getVersion() + " extension");
        if (extension.getExtension() != null) {
            for (String innerExtension : extension.getExtension()) {
                load(karaf.getResolver().resolve(innerExtension), karaf);
            }
        }
        if (extension.getModule() != null) {
            for (Module module : extension.getModule()) {
                karaf.addModule(karaf.getResolver().resolve(module.getLocation()));
            }
        }
    }

}
