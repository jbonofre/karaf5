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
import org.apache.karaf.core.Karaf;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.zip.ZipEntry;

/**
 * Resolves artifacts on Maven repostories.
 */
@Log
public class MavenResolver {

    private final String[] mavenRepositories;

    public MavenResolver(String mavenRepositories) {
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
        String uri = MavenUrlParser.pathFromMaven(artifactUri);

        if (uri.startsWith("file:")) {
            return uri.substring("file:".length());
        }

        if (uri.startsWith("http:") || uri.startsWith("https:")) {
            return uri;
        }

        String fileNameWithVersion = MavenUrlParser.fileNameFromMaven(artifactUri, false);
        String fileNameWithoutVersion = MavenUrlParser.fileNameFromMaven(artifactUri, true);

        // looking for cached file
        File cachedFile = new File(Karaf.get().config.cacheDirectory + "/" + fileNameWithoutVersion);
        if (cachedFile.exists()) {
            log.info("Loading cached from " + cachedFile.getAbsolutePath());
            return cachedFile.getAbsolutePath();
        }
        cachedFile = new File(Karaf.get().config.cacheDirectory + "/" + fileNameWithVersion);
        if (cachedFile.exists()) {
            log.info("Loading cached from " + cachedFile.getAbsolutePath());
            return cachedFile.getAbsolutePath();
        }

        // looking for embedded file
        if (getArchiveResource(fileNameWithoutVersion) != null) {
            log.info("Loading from " + getArchiveResource(fileNameWithoutVersion));
            return getArchiveResource(fileNameWithoutVersion);
        }
        if (getArchiveResource(fileNameWithVersion) != null) {
            log.info("Loading from " + getArchiveResource(fileNameWithVersion));
            return getArchiveResource(fileNameWithVersion);
        }
        if (getClassLoaderResource(fileNameWithoutVersion) != null) {
            log.info("Loading from " + getClassLoaderResource(fileNameWithoutVersion));
            return getClassLoaderResource(fileNameWithoutVersion);
        }
        if (getClassLoaderResource(fileNameWithVersion) != null) {
            log.info("Loading from " + getClassLoaderResource(fileNameWithVersion));
            return getClassLoaderResource(fileNameWithVersion);
        }

        if (mavenRepositories != null) {
            for (String mavenRepository : mavenRepositories) {
                try {
                    String concat = mavenRepository + "/" + uri;
                    InputStream inputStream = new URL(concat).openStream();
                    return concat;
                } catch (IOException ioException) {
                    log.log(Level.FINE, "Artifact " + artifactUri + " not found on " + mavenRepository, ioException);
                }
            }
        }
        return null;
    }

    public String getArchiveResource(String resource) throws Exception {
        ProtectionDomain protectionDomain = getClass().getProtectionDomain();
        CodeSource codeSource = protectionDomain.getCodeSource();
        URI location = (codeSource != null) ? codeSource.getLocation().toURI() : null;
        String path = (location != null) ? location.getSchemeSpecificPart() : null;
        if (path == null) {
            throw new IllegalStateException("Unable to determine code source archive");
        }
        File root = new File(path);
        if (!root.exists()) {
            throw new IllegalStateException("Unable to determine code source archive from " + root);
        }
        if (root.isDirectory()) {
            File file = new File(root, resource);
            if (file.exists()) {
                return file.getAbsolutePath();
            }
        } else {
            JarFile jarFile = new JarFile(root);
            ZipEntry entry = jarFile.getEntry(resource);
            if (entry != null && !entry.isDirectory()) {
                File destFile = new File(Karaf.get().config.cacheDirectory, entry.getName());
                extract(jarFile, entry, destFile);
                return destFile.getAbsolutePath();
            }
        }
        return null;
    }

    public String getClassLoaderResource(String resource) throws Exception {
        if (getClass().getResourceAsStream("KARAF-REPO/" + resource) != null) {
            File destFile = new File(Karaf.get().config.cacheDirectory, resource);
            destFile.getParentFile().mkdirs();
            copyStream(getClass().getResourceAsStream("KARAF-REPO/" + resource), new FileOutputStream(destFile));
            return destFile.getAbsolutePath();
        } else {
            if (getClass().getClassLoader().getResourceAsStream("KARAF-REPO/" + resource) != null) {
                File destFile = new File(Karaf.get().config.cacheDirectory, resource);
                destFile.getParentFile().mkdirs();
                copyStream(getClass().getClassLoader().getResourceAsStream("KARAF-REPO/" + resource), new FileOutputStream(destFile));
                return destFile.getAbsolutePath();
            }
        }
        return null;
    }

    private static File extract(JarFile jarFile, ZipEntry zipEntry, File dest) throws Exception {
        if (zipEntry.isDirectory()) {
            dest.mkdirs();
        } else {
            dest.getParentFile().mkdirs();
            FileOutputStream out = new FileOutputStream(dest);
            copyStream(jarFile.getInputStream(zipEntry), out);
            out.close();
        }
        return dest;
    }

    static long copyStream(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[10000];
        long count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

}
