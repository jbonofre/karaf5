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

import lombok.Builder;

@Builder
public class KarafConfig {

    @Builder.Default
    private String homeDirectory = "${java.io.tmpdir}/karaf";

    @Builder.Default
    private String dataDirectory = "${java.io.tmpdir}/karaf/data";

    @Builder.Default
    private String etcDirectory = "${java.io.tmpdir}/karaf/etc";

    @Builder.Default
    private String cacheDirectory = "${java.io.tmpdir}/karaf/data/cache";

    @Builder.Default
    private boolean clearCache = false;

    @Builder.Default
    private int defaultBundleStartLevel = 50;

    @Builder.Default
    private String mavenRepositories =  "file:${user.home}/.m2/repository," +
                    "file:${java.io.tmpdir}/karaf/system," +
                    "https://repo1.maven.org/maven2";

    public String homeDirectory() {
        return substitute(homeDirectory);
    }

    public String dataDirectory() {
        return substitute(dataDirectory);
    }

    public String etcDirectory() {
        return substitute(etcDirectory);
    }

    public String cacheDirectory() {
        return substitute(cacheDirectory);
    }

    public String mavenRepositories() {
        return substitute(mavenRepositories);
    }

    public boolean clearCache() {
        return clearCache;
    }

    public int defaultBundleStartLevel() {
        return defaultBundleStartLevel;
    }

    private String substitute(String raw) {
        if (raw == null) {
            return null;
        }
        String result = raw;
        for (String property : System.getProperties().stringPropertyNames()) {
            result = result.replaceAll("\\$\\{" + property + "}", System.getProperty(property));
        }

        return result;
    }

}
