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

public class KarafConfig {

    public String baseDirectory;
    public String dataDirectory;
    public String etcDirectory;
    public String cacheDirectory;
    public String mavenRepositories;
    public boolean clearCache;
    public int defaultBundleStartLevel;

    private KarafConfig(String baseDirectory,
                        String dataDirectory,
                        String etcDirectory,
                        String cacheDirectory,
                        String mavenRepositories,
                        boolean clearCache,
                        int defaultBundleStartLevel) {
        this.baseDirectory = baseDirectory;
        this.dataDirectory = dataDirectory;
        this.etcDirectory = etcDirectory;
        this.cacheDirectory = cacheDirectory;
        this.mavenRepositories = mavenRepositories;
        this.clearCache = clearCache;
        this.defaultBundleStartLevel = defaultBundleStartLevel;
    }

    public static class Builder {

        private String baseDirectory;
        private String dataDirectory;
        private String etcDirectory;
        private String cacheDirectory;
        private String mavenRepositories;
        private boolean clearCache;
        private int defaultBundleStartLevel;

        public Builder() {
            baseDirectory = System.getProperty("java.io.tmpdir") + "/karaf";
            dataDirectory = baseDirectory + "/data";
            cacheDirectory = dataDirectory + "/cache";
            etcDirectory = baseDirectory + "/etc";
            mavenRepositories = System.getProperty("user.home") + "/.m2/repository," +
                    baseDirectory + "/system," +
                    "https://repo1.maven.org/maven2";
            clearCache = false;
            defaultBundleStartLevel = 50;
        }

        public Builder baseDirectory(String baseDirectory) {
            if (baseDirectory == null) {
                throw new IllegalArgumentException("baseDirectory can't be empty");
            }
            this.baseDirectory = baseDirectory;
            return this;
        }

        public Builder dataDirectory(String dataDirectory) {
            if (dataDirectory == null) {
                throw new IllegalArgumentException("dataDirectory can't be empty");
            }
            this.dataDirectory = dataDirectory;
            return this;
        }

        public Builder etcDirectory(String etcDirectory) {
            if (etcDirectory == null) {
                throw new IllegalArgumentException("etcDirectory can't be empty");
            }
            this.etcDirectory = etcDirectory;
            return this;
        }

        public Builder cacheDirectory(String cacheDirectory) {
            if (cacheDirectory == null) {
                throw new IllegalArgumentException("cacheDirectory can't be empty");
            }
            this.cacheDirectory = cacheDirectory;
            return this;
        }

        public Builder mavenRepositories(String mavenRepositories) {
            this.mavenRepositories = mavenRepositories;
            return this;
        }

        public Builder clearCache(boolean clearCache) {
            this.clearCache = clearCache;
            return this;
        }

        public Builder defaultBundleStartLevel(int defaultBundleStartLevel) {
            this.defaultBundleStartLevel = defaultBundleStartLevel;
            return this;
        }

        public KarafConfig build() {
            return new KarafConfig(baseDirectory,
                    dataDirectory,
                    etcDirectory,
                    cacheDirectory,
                    mavenRepositories,
                    clearCache,
                    defaultBundleStartLevel);
        }

    }

}
