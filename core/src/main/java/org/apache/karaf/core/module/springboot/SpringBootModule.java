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
package org.apache.karaf.core.module.springboot;

import org.apache.karaf.core.Karaf;
import org.apache.karaf.core.module.Module;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.UUID;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

public class SpringBootModule implements Module {

    @Override
    public boolean canHandle(String url) {
        try {
            return (getManifestAttribute(url, "Spring-Boot-Version") != null);
        } catch (Exception e) {
            // no-op
        }
        return false;
    }

    @Override
    public void add(String url, String ... args) throws Exception {
        Karaf.modulesLock.writeLock().lock();
        try {
            if (!url.startsWith("file:") && !url.startsWith("http:") && !url.startsWith("https:")) {
                url = "file:" + url;
            }
            final URLClassLoader classLoader = new URLClassLoader(new URL[]{ new URL(url) }, this.getClass().getClassLoader());
            ClassLoader original = Thread.currentThread().getContextClassLoader();
            try {
                /*
                Thread.currentThread().setContextClassLoader(classLoader);
                final Method tomcat = classLoader.loadClass("org.apache.catalina.webresources.TomcatURLStreamHandlerFactory").getMethod("disable");
                if (!tomcat.isBridge()) {
                    tomcat.setAccessible(true);
                }
                tomcat.invoke(null, null);
                 */
                final Method method = classLoader.loadClass("org.springframework.boot.loader.JarLauncher").getMethod("main", String[].class);
                if (!method.isAccessible()) {
                    method.setAccessible(true);
                }
                method.invoke(null, new Object[]{args});
            } finally {
                Thread.currentThread().setContextClassLoader(original);
            }
            // throw new IllegalArgumentException("Spring Boot Module is not yet supported");

            org.apache.karaf.core.model.Module moduleModel = new org.apache.karaf.core.model.Module();
            moduleModel.setLocation(url);
            moduleModel.setName(getManifestAttribute(url, "Implementation-Title"));
            moduleModel.setId(UUID.randomUUID().toString());
            moduleModel.getMetadata().put("Main-Class", getManifestAttribute(url,"Main-Class"));
            moduleModel.getMetadata().put("Start-Class", getManifestAttribute(url, "Start-Class"));
            moduleModel.getMetadata().put("Spring-Boot-Version", getManifestAttribute(url, "Spring-Boot-Version"));
            moduleModel.getMetadata().put("Spring-Boot-Classes", getManifestAttribute(url, "Spring-Boot-Classes"));
            moduleModel.getMetadata().put("Spring-Boot-Lib", getManifestAttribute(url, "Spring-Boot-Lib"));
        } finally {
            Karaf.modulesLock.writeLock().unlock();
        }
    }

    @Override
    public boolean is(String id) {
        return false;
    }

    @Override
    public void remove(String bundleId) {
        // TODO
    }

    public String getManifestAttribute(String url, String attribute) {
        try {
            if (url.startsWith("http:") || url.startsWith("https:")) {
                try (JarInputStream jarInputStream = new JarInputStream(new URL(url).openStream())) {
                    return (String) jarInputStream.getManifest().getMainAttributes().getValue(attribute);
                }
            } else {
                try (JarFile jarFile = new JarFile(new File(url))) {
                    return (String) jarFile.getManifest().getMainAttributes().getValue(attribute);
                }
            }
        } catch (Exception e) {
            // no-op
        }
        return null;
    }

}
