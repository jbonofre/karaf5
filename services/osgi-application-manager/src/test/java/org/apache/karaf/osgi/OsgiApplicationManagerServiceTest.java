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
package org.apache.karaf.osgi;

import org.apache.karaf.boot.Karaf;
import org.apache.karaf.boot.config.Application;
import org.apache.karaf.boot.config.KarafConfig;
import org.apache.karaf.boot.service.KarafConfigService;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

public class OsgiApplicationManagerServiceTest {

    @Test
    public void simpleTest() throws Exception {
        final var karafConfig = new KarafConfigService();
        karafConfig.getProperties().put("osgi.storageDirectory", "target/osgi");
        karafConfig.getProperties().put("osgi.cache", "target/osgi/cache");
        Application application = new Application();
        application.setUrl("https://repo1.maven.org/maven2/commons-lang/commons-lang/2.6/commons-lang-2.6.jar");
        application.setType("org.apache.karaf.osgi.OsgiApplicationManagerService");
        karafConfig.getApplications().add(application);

        Karaf.builder().loader(() -> Stream.of(karafConfig)).build().start();
    }

}
