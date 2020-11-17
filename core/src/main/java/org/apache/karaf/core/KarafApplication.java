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
import org.apache.felix.framework.Felix;
import org.apache.felix.framework.FrameworkFactory;
import org.apache.felix.framework.Logger;
import org.apache.felix.framework.util.FelixConstants;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.startlevel.FrameworkStartLevel;
import org.osgi.framework.wiring.FrameworkWiring;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

@Log
public class KarafApplication {

    private KarafConfig config;
    private Felix framework = null;

    public KarafApplication(KarafConfig config) {
        this.config = config;
    }

    public static KarafApplication withConfig(KarafConfig config) {
        return new KarafApplication(config);
    }

    public void run() throws Exception {
        log.info("Starting Karaf Application...");
        Map<String, Object> config = new HashMap<>();
        config.put(Constants.FRAMEWORK_STORAGE, this.config.cache);
        config.put(Constants.FRAMEWORK_BOOTDELEGATION, "com.sun.*," +
                "javax.transaction," +
                "javax.transaction.*," +
                "javax.xml.crypto," +
                "javax.xml.crypto.*," +
                "jdk.nashorn," +
                "sun.*," +
                "jdk.internal.reflect," +
                "jdk.internal.reflect.*");
        String systemPackages = loadSystemPackages();
        if (systemPackages != null) {
            log.info("Using predefined system packages");
            config.put(Constants.FRAMEWORK_SYSTEMPACKAGES, systemPackages);
        }
        config.put(FelixConstants.LOG_LEVEL_PROP, "4");
        Logger logger = new Logger();
        logger.setLogger(log);
        config.put(FelixConstants.LOG_LOGGER_PROP, logger);
        if (this.config.clearCache) {
            config.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
        }

        framework = new Felix(config);

        try {
            framework.init();
            framework.start();
        } catch (BundleException e) {
            throw new RuntimeException(e);
        }

        FrameworkStartLevel sl = framework.adapt(FrameworkStartLevel.class);
        sl.setInitialBundleStartLevel(this.config.defaultBundleStartLevel);

        if (framework.getBundleContext().getBundles().length == 1) {

            loadModules();

            loadExtensions();

            loadApplication();

        }

        log.info("Karaf Application started!");
    }

    private String loadSystemPackages() {
        String systemPackages = null;
        try {
            try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("etc/system.packages")) {
                if (inputStream == null) {
                    throw new IllegalStateException("/etc/system.packages not found");
                }
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                    String line;
                    StringBuilder builder = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                    }
                    return builder.toString();
                }
            }
        } catch (Exception e) {
            log.log(Level.WARNING, "Can't load system.packages", e);
        }
        return null;
    }

    // create an uber bundle from KARAF-INF/application
    public void loadApplication() throws Exception {
        // TODO
    }

    // load inner bundles from KARAF-INF/modules
    public void loadModules() throws Exception {
        log.info("Loading KARAF-INF/modules");
        URL modulesUrl = this.getClass().getClassLoader().getResource("KARAF-INF/modules");
        if (modulesUrl != null) {
            File modulesFile = new File(modulesUrl.getPath());
            File[] modules = modulesFile.listFiles();
            for (File module : modules) {
                addModule(module.toURI().toURL().toString());
            }
        }
        log.info("Loading KARAF_MODULES env");
        String modulesEnv = System.getenv("KARAF_MODULES");
        if (modulesEnv != null) {
            String[] modulesSplit = modulesEnv.split(",");
            for (String module : modulesSplit) {
                addModule(module);
            }
        }
    }

    // load inner extensions from KARAF-INF/extensions
    public void loadExtensions() throws Exception {
        log.info("Loading KARAF-INF/extensions");
        URL extensionsUrl = this.getClass().getClassLoader().getResource("KARAF-INF/extensions");
        if (extensionsUrl != null) {
            File extensionsFile = new File(extensionsUrl.getPath());
            File[] extensions = extensionsFile.listFiles();
            for (File extension : extensions) {
                addExtension(extension.toURI().toURL().toString());
            }
        }
        log.info("Loading KARAF_EXTENSIONS env");
        String extensionsEnv = System.getenv("KARAF_EXTENSIONS");
        if (extensionsEnv != null) {
            String[] extensionsSplit = extensionsEnv.split(",");
            for (String extension : extensionsSplit) {
                addExtension(extension);
            }
        }
    }

    // module == bundle
    public void addModule(String url) throws Exception {
        log.info("Installing module " + url);
        Bundle bundle;
        try {
            bundle = framework.getBundleContext().installBundle(url, new URL(url).openStream());
        } catch (Exception e) {
            throw new Exception("Unable to install module " + url + ": " + e.toString(), e);
        }
        try {
            framework.adapt(FrameworkWiring.class).resolveBundles(Collections.singletonList(bundle));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        if (bundle.getState() != Bundle.RESOLVED) {
            throw new Exception("Module " + url + " is not resolved");
        }
        log.info("Starting module " + bundle.getSymbolicName() + "/" + bundle.getVersion());
        try {
            bundle.start();
        } catch (Exception e) {
            throw new Exception("Unable to start module " + bundle.getSymbolicName() + "/" + bundle.getVersion() + ": " + e.toString(), e);
        }
    }

    // extension == feature
    public void addExtension(String url) throws Exception {
        log.info("Loading extension from " + url);
        org.apache.karaf.core.extension.Loader.load(url, framework.getBundleContext());
    }

    public BundleContext getBundleContext() {
        return framework.getBundleContext();
    }

}
