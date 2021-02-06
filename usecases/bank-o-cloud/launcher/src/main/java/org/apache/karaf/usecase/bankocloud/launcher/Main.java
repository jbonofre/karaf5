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
package org.apache.karaf.usecase.bankocloud.launcher;

import org.apache.karaf.core.Karaf;
import org.apache.karaf.core.KarafConfig;

public class Main {

    private final String[] args;
    private Karaf karaf;

    public static void main(String[] args) throws Exception {
        final Main main = new Main(args);
        main.launch();
    }

    public Main(String[] args) {
        this.args = args;
    }

    public void launch() throws Exception {
        KarafConfig config = KarafConfig.builder()
                .homeDirectory("karaf")
                .dataDirectory("karaf/data")
                .cacheDirectory("karaf/data/cache")
                .build();
        karaf = Karaf.build(config);
        karaf.init();

        System.setProperty("org.apache.cxf.osgi.http.transport.disabled", "true");
        System.setProperty("org.apache.felix.http.host", "localhost");
        System.setProperty("org.apache.service.http.port", "8181");
        System.setProperty("org.apache.aries.spifly.auto.consumers", "jakarta.*");
        System.setProperty("org.apache.aries.spifly.auto.providers", "com.sun.*");

        karaf.addExtension("mvn:org.apache.karaf.extensions/config/5.0.0-SNAPSHOT");
        karaf.addExtension("mvn:org.apache.karaf.extensions/scr/5.0.0-SNAPSHOT");
        karaf.addModule("mvn:jakarta.activation/jakarta.activation-api/1.2.2");
        karaf.addModule("mvn:org.apache.felix/org.apache.felix.http.servlet-api/1.1.2");
        karaf.addModule("mvn:org.apache.felix/org.apache.felix.http.jetty/4.1.4");
        karaf.addModule("mvn:org.apache.felix/org.apache.felix.http.whiteboard/4.0.0");
        karaf.addModule("mvn:org.apache.aries.spifly/org.apache.aries.spifly.dynamic.framework.extension/1.3.2");
        karaf.addModule("mvn:org.apache.geronimo.specs/geronimo-jaxrs_2.1_spec/1.1");
        karaf.addModule("mvn:org.apache.aries.jax.rs/org.apache.aries.jax.rs.whiteboard/1.0.10");
        karaf.addModule("mvn:org.apache.karaf.usecases.bank-o-cloud/api/5.0.0-SNAPSHOT");
        karaf.start();
    }

}
