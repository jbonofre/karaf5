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
package org.apache.karaf.springboot;

import lombok.extern.java.Log;
import org.apache.karaf.boot.config.Application;
import org.apache.karaf.boot.config.KarafConfig;
import org.apache.karaf.boot.service.ClassLoaderService;
import org.apache.karaf.boot.service.KarafConfigService;
import org.apache.karaf.boot.service.KarafLifeCycleService;
import org.apache.karaf.boot.service.ServiceRegistry;
import org.apache.karaf.boot.spi.Service;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.jar.JarInputStream;

@Log
public class SpringBootApplicationManagerService implements Service {

    @Override
    public String name() {
        return "spring-boot";
    }

    @Override
    public int priority() {
        return DEFAULT_PRIORITY + 100;
    }

    @Override
    public void onRegister(ServiceRegistry serviceRegistry) throws Exception {
        log.info("Starting Spring Boot application manager service");
        log.info("Registering Spring Boot application manager service");
        ClassLoaderService classLoaderService = serviceRegistry.get(ClassLoaderService.class);
        KarafLifeCycleService karafLifeCycleService = serviceRegistry.get(KarafLifeCycleService.class);
        karafLifeCycleService.onStart(() -> {
            getApplications(serviceRegistry.get(KarafConfigService.class)).forEach(application -> {
                try {
                    start(application.getUrl(), application.getProfile(), classLoaderService, application.getProperties());
                } catch (Exception e) {
                    throw new RuntimeException("Can't start Spring Boot application " + application.getUrl(), e);
                }
            });
        });
        // TODO add shutdown hook
    }

    private List<Application> getApplications(KarafConfig karafConfig) {
        List<Application> applications = new LinkedList<>();
        if (karafConfig != null) {
            karafConfig.getApplications().forEach(application -> {
                if (application.getType() == null) {
                    if (canHandle(application.getUrl())) {
                        applications.add(application);
                    }
                } else if (application.getType().equals(this.getClass().getName())) {
                    applications.add(application);
                }
            });
        }
        return applications;
    }

    private boolean canHandle(String url) {
        try {
            try (JarInputStream jarInputStream = new JarInputStream(new URL(url).openStream())) {
                if (jarInputStream.getManifest().getMainAttributes().getValue("Spring-Boot-Version") != null) {
                    return true;
                }
            }
        } catch (Exception e) {
            // no-op
        }
        return false;
    }

    private String start(String url, String profile, ClassLoaderService classLoaderService, Map<String, Object> properties) throws Exception {
        log.info("Starting Spring Boot application " + url);
        URLClassLoader classLoader = null;
        if (profile == null) {
            classLoader = new URLClassLoader(new URL[]{new URL(url)}, this.getClass().getClassLoader());
        } else {
            classLoader = new URLClassLoader(new URL[]{new URL(url)}, classLoaderService.getClassLoader(profile));
        }
        ClassLoader original = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);
            // disable tomcat stream handler
            final Method tomcat = classLoader.loadClass("org.apache.catalina.webresources.TomcatURLStreamHandlerFactory").getMethod("disable");
            if (!tomcat.isBridge()) {
                tomcat.setAccessible(true);
            }
            tomcat.invoke(null, null);
            // invoke spring boot main
            final Method main = classLoader.loadClass("org.springframework.boot.loader.JarLauncher").getMethod("main", String[].class);
            main.setAccessible(true);
            if (properties.get("args") != null) {
                main.invoke(null, new Object[]{ properties.get("args") });
            } else {
                main.invoke(null);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(original);
        }
        String id;
        try (JarInputStream jarInputStream = new JarInputStream(new URL(url).openStream())) {
            id = jarInputStream.getManifest().getMainAttributes().getValue("Start-Class");
        }
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        return id;
    }

    private void stop(String id) throws Exception {
        // TODO
    }

}
