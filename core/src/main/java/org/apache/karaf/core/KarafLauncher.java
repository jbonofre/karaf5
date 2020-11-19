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

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class KarafLauncher {

    private static final Logger logger = Logger.getLogger(KarafLauncher.class.getName());

    public static void main(String... args) throws Exception {
        logger.info("Launching Karaf...");
        List<URL> urls = new ArrayList<>();
        ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
        URLClassLoader urlClassLoader = new URLClassLoader(urls.toArray(new URL[urls.size()]), currentClassLoader);

        final Class<?> karafApplicationClass = urlClassLoader.loadClass("org.apache.karaf.core.Karaf");
        Karaf instance = Karaf.class.cast(
                karafApplicationClass
                        .getConstructor(KarafConfig.class)
                        .newInstance(
                            new KarafConfig.Builder().build()));
        instance.run();
        logger.info("Karaf launcher succeed!");
    }

}
