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
package org.apache.karaf.core.maven;

import lombok.extern.java.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;

/**
 * Resolves artifacts on Maven repostories.
 */
@Log
public class Resolver {

    private final String[] mavenRepositories;

    public Resolver(String mavenRepositories) {
        if (mavenRepositories != null) {
            this.mavenRepositories = mavenRepositories.split(",");
        } else {
            this.mavenRepositories = new String[]{};
        }
    }

    /**
     * Resolve mvn URL format to "concrete" path.
     * The mvn URL format is:
     *   mvn:groupId/artifactId/version/type/classifier
     *
     * @param artifactUri
     * @return
     */
    public String resolve(String artifactUri) throws Exception {
        String uri = Parser.pathFromMaven(artifactUri);

        if (uri.startsWith("file:")) {
            return uri.substring("file:".length());
        }

        if (uri.startsWith("http:")) {
            return uri;
        }

        return uri;
    }

    public InputStream open(String artifactUri, String inner) throws Exception {
        String uri = resolve(artifactUri);
        if (uri.startsWith("file:") || uri.startsWith("http:")) {
            return new URL(uri).openStream();
        }
        if (mavenRepositories != null) {
            for (String mavenRepository : mavenRepositories) {
                try {
                    String concat = mavenRepository + "/" + uri;
                    InputStream inputStream = new URL(concat).openStream();
                    log.info(concat + " found");
                    return inputStream;
                } catch (IOException ioException) {
                    log.log(Level.FINE, "Artifact " + artifactUri + " not found on " + mavenRepository, ioException);
                }
            }
        }
        if (inner != null) {
            try {
                String fileName = inner + "/" + Parser.fileNameFromMaven(artifactUri, false);
                InputStream inputStream = new URL(fileName).openStream();
                log.info(fileName + " found in " + inner);
                return inputStream;
            } catch (IOException ioException) {
                log.log(Level.FINE, "Artifact " + artifactUri + " not found in " + inner, ioException);
            }
            try {
                String fileName = inner + "/" + Parser.fileNameFromMaven(artifactUri, true);
                InputStream inputStream = new URL(fileName).openStream();
                log.info(fileName + " found in " + inner);
                return inputStream;
            } catch (IOException ioException) {
                log.log(Level.FINE, "Artifact " + artifactUri + " not found in " + inner, ioException);
            }
        }
        return null;
    }

}
