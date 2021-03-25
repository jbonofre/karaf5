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
package org.apache.karaf.boot;

import lombok.extern.java.Log;
import org.apache.karaf.boot.config.KarafConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

@Log
public class KarafTest {

    @Test
    public void emptyRunProgrammaticallyTest() throws Exception {
        KarafConfig karafConfig = KarafConfig.build();
        karafConfig.setHome("target/karaf");
        karafConfig.setData("target/karaf/data");
        karafConfig.setCache("target/karaf/cache/1");
        karafConfig.setClearCache(true);
        Karaf karaf = Karaf.build(karafConfig);

        karaf.init();
        karaf.start();

        BundleContext bundleContext = karaf.getContext();
        ServiceReference<Karaf> reference = bundleContext.getServiceReference(Karaf.class);
        Assertions.assertNotNull(reference);
        Karaf service = bundleContext.getService(reference);
        Assertions.assertNotNull(service);
    }

    @Test
    public void emptyPropertyJsonTest() throws Exception {
        System.setProperty("karaf.config", "target/test-classes/emptyrun.json");
        Karaf karaf = Karaf.build();

        karaf.init();
        karaf.start();

        Assertions.assertEquals("target/karaf", karaf.getConfig().getHome());
        Assertions.assertEquals("target/karaf/data", karaf.getConfig().getData());
        System.clearProperty("karaf.config");
    }

    @Test
    public void emptyRunTest() throws Exception {
        Karaf karaf = Karaf.build();
        // avoid to pollute the Maven module directory
        // karaf.init();
        // karaf.start();

        Assertions.assertEquals(".", karaf.getConfig().getHome());
        Assertions.assertEquals("./data", karaf.getConfig().getData());
    }

    @Test
    public void addOsgiModuleWithHttpUrl() throws Exception {
        KarafConfig karafConfig = KarafConfig.build();
        karafConfig.setHome("target/karaf");
        karafConfig.setData("target/karaf/data");
        karafConfig.setCache("target/karaf/cache/2");
        Karaf karaf = Karaf.build(karafConfig);

        karaf.init();
        karaf.addModule("https://repo1.maven.org/maven2/org/ops4j/pax/url/pax-url-mvn/1.3.7/pax-url-mvn-1.3.7.jar");
        karaf.start();
    }

    @Test
    public void addOsgiModuleWithMvnUrl() throws Exception {
        KarafConfig karafConfig = KarafConfig.build();
        karafConfig.setHome("target/karaf");
        karafConfig.setData("target/karaf/data");
        karafConfig.setCache("target/karaf/cache/3");
        Karaf karaf = Karaf.build(karafConfig);

        karaf.init();
        karaf.addModule("mvn:commons-lang/commons-lang/2.6");
        karaf.start();

        Assertions.assertNotNull(karaf.getModuleManager().getId("mvn:commons-lang/commons-lang/2.6"));
    }

}
