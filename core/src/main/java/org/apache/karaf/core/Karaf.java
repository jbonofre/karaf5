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
package org.apache.karaf.core;

import lombok.extern.java.Log;
import org.apache.felix.framework.FrameworkFactory;
import org.apache.felix.framework.cache.BundleCache;
import org.apache.felix.framework.util.FelixConstants;
import org.apache.karaf.core.extension.ExtensionLoader;
import org.apache.karaf.core.maven.MavenResolver;
import org.apache.karaf.core.model.Extension;
import org.apache.karaf.core.model.Module;
import org.apache.karaf.core.module.osgi.BundleModule;
import org.apache.karaf.core.module.microprofile.MicroprofileModule;
import org.apache.karaf.core.module.springboot.SpringBootModule;
import org.apache.karaf.core.specs.SpecsListener;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.startlevel.FrameworkStartLevel;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;

@Log
public class Karaf {

    private static Karaf instance;

    private KarafConfig config;
    private Framework framework = null;
    private MavenResolver mavenResolver;
    private long start;

    public final static Map<String, org.apache.karaf.core.model.Module> modules = new HashMap<>();
    public final static ReadWriteLock modulesLock = new ReentrantReadWriteLock();

    public final static List<Extension> extensions = new LinkedList<>();
    public final static ReadWriteLock extensionsLock = new ReentrantReadWriteLock();

    private Karaf(KarafConfig config) {
        this.config = config;
        instance = this;
    }

    public static Karaf build() {
        return new Karaf(KarafConfig.builder().build());
    }

    public static Karaf build(KarafConfig config) {
        return new Karaf(config);
    }

    public void init() throws Exception {
        start = System.currentTimeMillis();

        mavenResolver = new MavenResolver(config.mavenRepositories);

        if (System.getProperty("java.util.logging.config.file") == null) {
            if (System.getenv("KARAF_LOG_FORMAT") != null) {
                System.setProperty("java.util.logging.SimpleFormatter.format", System.getenv("KARAF_LOG_FORMAT"));
            }
            if (System.getProperty("java.util.logging.SimpleFormatter.format") == null) {
                System.setProperty("java.util.logging.SimpleFormatter.format",
                        "%1$tF %1$tT.%1$tN %4$s [ %2$s ] : %5$s%6$s%n");
            }
        }

        log.info("\n" +
                "        __ __                  ____      \n" +
                "       / //_/____ __________ _/ __/      \n" +
                "      / ,<  / __ `/ ___/ __ `/ /_        \n" +
                "     / /| |/ /_/ / /  / /_/ / __/        \n" +
                "    /_/ |_|\\__,_/_/   \\__,_/_/         \n" +
                "\n" +
                "  Apache Karaf (5.0.0-SNAPSHOT)\n");

        log.info("Base directory: " + this.config.homeDirectory);
        log.info("Cache directory: " + this.config.cacheDirectory);
        Map<String, Object> config = new HashMap<>();
        config.put(Constants.FRAMEWORK_STORAGE, this.config.cacheDirectory);
        if (this.config.clearCache) {
            config.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
        }
        config.put(Constants.FRAMEWORK_BEGINNING_STARTLEVEL, "100");
        config.put(FelixConstants.LOG_LEVEL_PROP, "3");
        config.put(BundleCache.CACHE_ROOTDIR_PROP, this.config.cacheDirectory);
        String bootDelegation = loadBootDelegation();
        if (bootDelegation != null) {
            log.info("Using predefined boot delegation");
            config.put(Constants.FRAMEWORK_BOOTDELEGATION, bootDelegation);
        }
        String systemPackages = loadSystemPackages();
        if (systemPackages != null) {
            log.info("Using predefined system packages");
            config.put(Constants.FRAMEWORK_SYSTEMPACKAGES, systemPackages);
        }

        FrameworkFactory frameworkFactory = new FrameworkFactory();
        framework = frameworkFactory.newFramework(config);

        try {
            framework.init();
            framework.start();
        } catch (BundleException e) {
            throw new RuntimeException(e);
        }

        FrameworkStartLevel sl = framework.adapt(FrameworkStartLevel.class);
        sl.setInitialBundleStartLevel(this.config.defaultBundleStartLevel);

        if (framework.getBundleContext().getBundles().length == 1) {

            log.info("Starting specs listener");
            SpecsListener specsListener = new SpecsListener();
            specsListener.start(framework.getBundleContext());

            log.info("Registering Karaf service");
            framework.getBundleContext().registerService(Karaf.class, this, null);

            loadModules();
            loadExtensions();
        }
    }

    public void start() {
        long now = System.currentTimeMillis();

        log.info(getStartedMessage(start, now));
    }

    private String getStartedMessage(long start, long now) {
        StringBuilder message = new StringBuilder();
        message.append("Started in ");
        message.append((now - start) / 1000.0);
        message.append(" seconds");
        try {
            double uptime = ManagementFactory.getRuntimeMXBean().getUptime() / 1000.0;
            message.append(" (JVM running for ").append(uptime).append(")");
        } catch (Throwable e) {
            // no-op
        }
        return message.toString();
    }

    private String loadFile(String resource) {
        try {
            try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(resource)) {
                if (inputStream == null) {
                    throw new IllegalStateException(resource + " not found");
                }
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                    String line;
                    StringBuilder builder = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        if (!line.startsWith("#") && !line.startsWith("//")) {
                            builder.append(line);
                        }
                    }
                    return builder.toString();
                }
            }
        } catch (Exception e) {
            log.log(Level.WARNING, "Can't load " + resource, e);
        }
        return null;
    }

    private String loadBootDelegation() {
        return loadFile("etc/boot.delegation");
    }

    private String loadSystemPackages() {
        return loadFile("etc/system.packages");
    }

    public void loadModules() throws Exception {
        log.info("Loading KARAF_MODULES env");
        String modulesEnv = System.getenv("KARAF_MODULES");
        if (modulesEnv != null) {
            String[] modulesSplit = modulesEnv.split(",");
            for (String module : modulesSplit) {
                addModule(module);
            }
        }
    }

    public void loadExtensions() throws Exception {
        log.info("Loading KARAF_EXTENSIONS env");
        String extensionsEnv = System.getenv("KARAF_EXTENSIONS");
        if (extensionsEnv != null) {
            String[] extensionsSplit = extensionsEnv.split(",");
            for (String extension : extensionsSplit) {
                addExtension(extension);
            }
        }
    }

    public void addModule(String url) throws Exception {
        log.info("Installing module " + url);

        String resolved = mavenResolver.resolve(url);

        if (resolved == null) {
            throw new IllegalArgumentException("Module " + url + " not found");
        }

        BundleModule bundleModule = new BundleModule(framework, this.config.defaultBundleStartLevel);
        if (bundleModule.canHandle(resolved)) {
            bundleModule.add(resolved);
        }

        SpringBootModule springBootModule = new SpringBootModule();
        if (springBootModule.canHandle(resolved)) {
            springBootModule.add(resolved);
        }

        MicroprofileModule microprofileModule = new MicroprofileModule();
        if (microprofileModule.canHandle(resolved)) {
            microprofileModule.add(resolved);
        }
    }

    public void removeModule(String id) throws Exception {
        log.info("Uninstalling module " + id);

        BundleModule bundleModule = new BundleModule(framework, this.config.defaultBundleStartLevel);
        if (bundleModule.is(id)) {
            bundleModule.remove(id);
        }

        SpringBootModule springBootModule = new SpringBootModule();
        if (springBootModule.is(id)) {
            springBootModule.remove(id);
        }

        MicroprofileModule microprofileModule = new MicroprofileModule();
        if (microprofileModule.is(id)) {
            microprofileModule.remove(id);
        }
    }

    public void addExtension(String url) throws Exception {
        log.info("Loading extension from " + url);
        ExtensionLoader.load(url, this);
    }

    public Map<String, Module> getModules() {
        Karaf.modulesLock.readLock().lock();
        try {
            return modules;
        } finally {
            Karaf.modulesLock.readLock().unlock();
        }
    }

    public List<Extension> getExtensions() {
        Karaf.extensionsLock.readLock().lock();
        try {
            return extensions;
        } finally {
            Karaf.extensionsLock.readLock().unlock();
        }
    }

    public Object getService(Class clazz) {
        ServiceReference reference = framework.getBundleContext().getServiceReference(clazz);
        if (reference != null) {
            return framework.getBundleContext().getService(reference);
        }
        return null;
    }

    public void addService(Class clazz, Object object) {
        framework.getBundleContext().registerService(clazz, object, null);
    }

    public MavenResolver getResolver() {
        return this.mavenResolver;
    }

    public static Karaf get() {
        return instance;
    }

}
