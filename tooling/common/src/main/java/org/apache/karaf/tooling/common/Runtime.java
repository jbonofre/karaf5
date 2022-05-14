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
package org.apache.karaf.tooling.common;

import lombok.Data;
import lombok.extern.java.Log;
import org.apache.karaf.boot.config.KarafConfig;
import org.apache.karaf.tooling.common.maven.Parser;
import org.apache.karaf.tooling.common.model.KarafBuild;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Data
@Log
public class Runtime {

    private String name;
    private String version;
    private Map<String, Object> properties = new HashMap<>();
    private List<String> dependencies = new LinkedList<>();
    private KarafConfig karafConfig;

    private Path baseFolder;
    private Path workingFolder;

    public Runtime(String name, String version, Map<String, Object> properties) throws Exception {
        this.name = (name != null) ? name : UUID.randomUUID().toString();
        this.version = (version != null) ? version : "5.0-SNAPSHOT";
        this.properties = properties;
        String baseDirectory = (properties != null && properties.get("base.directory") != null) ? properties.get("base.directory").toString() : name;
        log.info("Creating Karaf runtime package in folder " + baseDirectory);
        baseFolder = Paths.get(baseDirectory);
        Files.createDirectories(baseFolder);
        workingFolder = Files.createTempDirectory("temp_" + name);
    }

    public void createPackage() throws Exception {
        dependencies.forEach(dependency -> {
            try {
                copyArtifact(dependency);
            } catch (Exception e) {
                throw new IllegalArgumentException("Can't copy dependency " + dependency, e);
            }
        });
    }

    public static void createPackage(InputStream karafBuild) throws Exception {
        KarafBuild build = KarafBuild.load(karafBuild);
        Runtime runtime = new Runtime(build.getName(), build.getVersion(), build.getProperties());
        build.getDependencies().forEach(dependency -> {
            runtime.getDependencies().add(dependency);
        });
        runtime.createPackage();
    }

    public void createJar() throws Exception {
        log.info("Creating Karaf runtime uber jar");
        // exploded all jar
        Path exploded = workingFolder.resolve("exploded");
        Files.createDirectories(exploded);
        String karafLib = (properties != null && properties.get("karaf.lib") != null) ? properties.get("karaf.lib").toString() : "";
        Path libFolder = baseFolder.resolve(karafLib);
        Files.list(libFolder).forEach(artifact -> {
            if (artifact.toString().endsWith(".jar")) {

                // test
                try (JarFile jarFile = new JarFile(artifact.toFile())) {
                    for (Enumeration<JarEntry> j = jarFile.entries(); j.hasMoreElements(); ) {
                        JarEntry entry = j.nextElement();
                        String name = entry.getName();
                        if ("META-INF/INDEX.LIST".equals(name)) {
                            continue;
                        }
                        if ("module-info.class".equals(name)) {
                            continue;
                        }
                        if (entry.isDirectory()) {
                            Files.createDirectories(exploded.resolve(name));
                        } else {
                            if (name.contains("META-INF/services")) {
                                log.info("services " + name);
                                Path service = exploded.resolve(name);
                                try (BufferedWriter writer = new BufferedWriter(new FileWriter(service.toFile(), true))) {
                                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(jarFile.getInputStream(entry)))) {
                                        String line = null;
                                        while ((line = reader.readLine()) != null) {
                                            if (!line.startsWith("#")) {
                                                writer.write(line + "\n");
                                            }
                                        }
                                    }
                                }
                            } else {
                                InputStream is = null;
                                try {
                                    is = jarFile.getInputStream(entry);
                                    try (FileOutputStream os = new FileOutputStream(new File(exploded.toFile(), name))) {
                                        byte[] buffer = new byte[1024];
                                        int readCount = 0;
                                        while ((readCount = is.read(buffer)) >= 0) {
                                            os.write(buffer, 0, readCount);
                                        }
                                        os.flush();
                                    }
                                } catch (Exception e) {
                                    log.warning("Can't extract " + entry + ": " + e.getMessage());
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warning("Can't extract jar " + artifact);
                }
            } else {
                try {
                    Files.copy(artifact, exploded.resolve(artifact.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception e) {
                    log.warning("Can't copy " + artifact + ": " + e.getMessage());
                }
            }
        });
        // package all as a uber jar
        Path uberJarPath = baseFolder.resolve(name + ".jar");
        // add Main-Class in the manifest
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        if (properties == null || properties.get("MainClass.disabled") == null || !properties.get("MainClass.disabled").toString().equalsIgnoreCase("true")) {
            String mainClass = (properties.get("MainClass") != null) ? properties.get("MainClass").toString() : "org.apache.karaf.boot.Main";
            manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, mainClass);
        }
        try (JarOutputStream uberJar = new JarOutputStream(new FileOutputStream(uberJarPath.toFile()), manifest)) {
            Files.list(exploded).forEach(file -> {
                log.info("Adding jar entry " + file);
                try {
                    addJarContent(file, exploded.toString(), uberJar);
                } catch (Exception e) {
                    log.warning("Can't add jar content: " + e.getMessage());
                }
            });
        }

        // cleanup
        if (properties != null && properties.get("karaf.cleanup") != null && properties.get("karaf.cleanup").toString().equalsIgnoreCase("true")) {
            Files.walk(libFolder).map(Path::toFile).forEach(File::delete);
            Files.delete(libFolder);
        }
    }

    private void addJarContent(Path source, String base, JarOutputStream target) throws Exception {
        String name = source.toString().substring(base.length() + 1);
        name = name.replace("\\", "/");
        if (Files.isDirectory(source)) {
            if (!name.endsWith("/")) {
                name += "/";
            }
            JarEntry entry = new JarEntry(name);
            entry.setTime(Files.getLastModifiedTime(source).toMillis());
            target.putNextEntry(entry);
            target.closeEntry();
            Files.list(source).forEach(child -> {
                try {
                    addJarContent(child, base, target);
                } catch (Exception e) {
                    log.warning("Can't add jar content " + e.getMessage());
                }
            });
        } else {
            JarEntry entry = new JarEntry(name);
            entry.setTime(Files.getLastModifiedTime(source).toMillis());
            target.putNextEntry(entry);
            try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(source.toFile()))) {
                byte[] buffer = new byte[1024];
                while (true) {
                    int count = in.read(buffer);
                    if (count == -1) {
                        break;
                    }
                    target.write(buffer, 0, count);
                }
                target.closeEntry();
            }
        }
    }

    private void copyArtifact(String location) throws Exception {
        String libFolder = (properties != null && properties.get("karaf.lib") != null) ? properties.get("karaf.lib").toString() : "";
        Path runtimeDependenciesFolder = baseFolder.resolve(libFolder);
        Files.createDirectories(runtimeDependenciesFolder);

        if (!location.startsWith("file:") && !location.startsWith("http:") && !location.startsWith("https:") && !location.startsWith("mvn:")) {
            if (location.startsWith("k5:")) {
                location = location.substring("k5:".length());
            }
            location = "mvn:org.apache.karaf/" + location + "/" + version;
        }
        // copy service jar in working dir
        if (location.startsWith("mvn")) {
            if (properties != null && properties.get("include.transitive") != null && properties.get("include.transitive").toString().equalsIgnoreCase("true")) {
                // resolving dependencies
                String pomLocation = location + "/pom";
                File pomFile = Parser.resolve(pomLocation);
                if (pomFile.exists()) {
                    List<String> dependencies = new ArrayList<>();
                    Parser.getDependencies(pomFile, dependencies, true);
                    dependencies.forEach(dependency -> {
                        try {
                            File dependencyFile = Parser.resolve(dependency);
                            Path targetPath = runtimeDependenciesFolder.resolve(dependencyFile.toPath().getFileName());
                            Files.copy(dependencyFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                        } catch (Exception e) {
                            log.warning("Can't include transitive " + dependency + ": " + e);
                        }
                    });
                }
            }
            // resolve artifact
            File file = Parser.resolve(location);
            location = "file:" + file.getAbsolutePath();
        }

        if (location.startsWith("http:") || location.startsWith("https:")) {
            InputStream in = new URL(location).openStream();
            String fileName = UUID.randomUUID() + ".jar";
            if (location.lastIndexOf("/") != -1) {
                fileName = location.substring(location.lastIndexOf("/") + 1);
            }
            Files.copy(in, runtimeDependenciesFolder.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        } else {
            if (location.startsWith("file:")) {
                location = location.substring("file:".length());
            }
            Path servicePath = Paths.get(location);
            Path targetPath = runtimeDependenciesFolder.resolve(servicePath.getFileName());
            Files.copy(servicePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static void createJar(InputStream karafBuild) throws Exception {
        KarafBuild build = KarafBuild.load(karafBuild);
        Runtime runtime = new Runtime(build.getName(), build.getVersion(), build.getProperties());
        build.getDependencies().forEach(dependency -> {
            runtime.getDependencies().add(dependency);
        });
        runtime.createPackage();
        runtime.createJar();
    }

    public void createArchive() throws Exception {
        log.info("Creating Karaf runtime archive");
        // create script
        createStartScript();
        // create zip archive
        createZipArchive();
    }

    private void createStartScript() throws Exception {
        Path binFolder = baseFolder.resolve("bin");
        Files.createDirectories(binFolder);

        String libFolder = (properties != null && properties.get("karaf.lib") != null) ? properties.get("karaf.lib").toString() : "";

        File karafSh = new File(binFolder.toFile(), "karaf.sh");
        try (PrintWriter writer = new PrintWriter(new FileOutputStream(karafSh))) {
            writer.println("#!/bin/sh");
            writer.println("");
            writer.println("cd ../" + libFolder);
            writer.println("java -jar karaf-boot-5.0-SNAPSHOT.jar");
        }

        karafSh.setExecutable(true);
    }

    private void createZipArchive() throws Exception {
        Path zipArchive = baseFolder.resolve(name + ".zip");
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipArchive.toFile()))) {
            Files.list(baseFolder).forEach(file -> {
                try {
                    if (!file.equals(zipArchive)) {
                        addZipContent(file, baseFolder.toString(), zipOutputStream);
                    }
                } catch (Exception e) {
                    log.warning("Can't zip jar content " + e.getMessage());
                }
            });
        }
    }

    private void addZipContent(Path source, String base, ZipOutputStream target) throws Exception {
        String name = source.toString().substring(base.length() + 1);
        name = name.replace("\\", "/");
        if (Files.isDirectory(source)) {
            if (!name.endsWith("/")) {
                name += "/";
            }
            ZipEntry entry = new JarEntry(name);
            entry.setTime(Files.getLastModifiedTime(source).toMillis());
            target.putNextEntry(entry);
            target.closeEntry();
            Files.list(source).forEach(child -> {
                try {
                    addZipContent(child, base, target);
                } catch (Exception e) {
                    log.warning("Can't add zip content " + e.getMessage());
                }
            });
        } else {
            ZipEntry entry = new ZipEntry(name);
            entry.setTime(Files.getLastModifiedTime(source).toMillis());
            target.putNextEntry(entry);
            try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(source.toFile()))) {
                byte[] buffer = new byte[1024];
                while (true) {
                    int count = in.read(buffer);
                    if (count == -1) {
                        break;
                    }
                    target.write(buffer, 0, count);
                }
                target.closeEntry();
            }
        }
    }

    public static void createArchive(InputStream karafBuild) throws Exception {
        KarafBuild build = KarafBuild.load(karafBuild);
        Runtime runtime = new Runtime(build.getName(), build.getVersion(), build.getProperties());
        build.getDependencies().forEach(dependency -> {
            runtime.getDependencies().add(dependency);
        });
        runtime.createPackage();
        runtime.createArchive();
    }

    // create Docker image is done by JIB or similar

}
