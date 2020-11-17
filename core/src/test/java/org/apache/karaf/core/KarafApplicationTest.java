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
import org.junit.jupiter.api.*;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import java.util.Enumeration;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Log
public class KarafApplicationTest {

    private static final String fwkCacheDir = "/tmp/cache";

    @Test
    @Order(1)
    public void testKarafApplicationRunWithResolvedModule() throws Exception {
        log.info("Using cache " + fwkCacheDir);
        KarafApplication application = KarafApplication.withConfig(
                KarafConfig.build()
                        .withCache(fwkCacheDir)
                        .withClearCache(true)
                        .withDefaultBundleStartLevel(50));
        application.run();
        application.addModule("https://repo1.maven.org/maven2/org/ops4j/pax/url/pax-url-mvn/1.3.7/pax-url-mvn-1.3.7.jar");
    }

    @Test
    @Order(2)
    public void testKarafApplicationRunWithUnresolvedModule() throws Exception {
        KarafApplication application = KarafApplication.withConfig(
                KarafConfig.build()
                    .withCache(fwkCacheDir)
                    .withClearCache(true)
                    .withDefaultBundleStartLevel(50));
        application.run();
        try {
            application.addModule("https://repo1.maven.org/maven2/org/ops4j/pax/url/pax-url-aether/2.6.3/pax-url-aether-2.6.3.jar");
            Assertions.fail("Bundle exception expected");
        } catch (Exception e) {
            // no-op
        }
    }
}
