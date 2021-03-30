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
import org.apache.karaf.boot.config.KarafConfig;
import org.apache.karaf.boot.config.Launcher;
import org.apache.karaf.boot.config.Service;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class OsgiApplicationManagerTest {

    @Test
    public void simpleTest() throws Exception {
        KarafConfig karafConfig = new KarafConfig();
        Launcher launcher = new Launcher();
        Service osgiConfig = new Service();
        osgiConfig.setName("osgi");
        osgiConfig.getProperties().put("storageDirectory", "target/karaf");
        osgiConfig.getProperties().put("cache", "target/karaf/cache");
        launcher.getManagers().add(osgiConfig);
        karafConfig.setLauncher(launcher);

        Karaf karaf = Karaf.build(karafConfig);
        karaf.init();
        karaf.start();

        karaf.startApplication("https://repo1.maven.org/maven2/commons-lang/commons-lang/2.6/commons-lang-2.6.jar", "osgi", null);

        Assertions.assertEquals(1, karaf.getApplicationIds().size());

        Assertions.assertEquals("1", karaf.getApplicationIds().get(0));
        Assertions.assertEquals("osgi", karaf.getApplicationManager("1"));
        Assertions.assertEquals("https://repo1.maven.org/maven2/commons-lang/commons-lang/2.6/commons-lang-2.6.jar", karaf.getApplicationUrl("1"));
    }

}
