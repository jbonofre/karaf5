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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Log
public class ResolverTest {

    @Test
    public void testResolve() throws Exception {
        Resolver resolver = new Resolver(null);
        String resolved = resolver.resolve("mvn:myGroupId/myArtifactId/1.0-SNAPSHOT");
        Assertions.assertEquals("myGroupId/myArtifactId/1.0-SNAPSHOT/myArtifactId-1.0-SNAPSHOT.jar", resolved);
        resolved = resolver.resolve("mvn:myGroupId/myArtifactId/1.0-SNAPSHOT/xml/myClassifier");
        Assertions.assertEquals("myGroupId/myArtifactId/1.0-SNAPSHOT/myArtifactId-1.0-SNAPSHOT-myClassifier.xml", resolved);
        resolved = resolver.resolve("mvn:http://myrepo/foo!myGroupId/myArtifactId/1.0-SNAPSHOT");
        Assertions.assertEquals("http://myrepo/foo/myGroupId/myArtifactId/1.0-SNAPSHOT/myArtifactId-1.0-SNAPSHOT.jar", resolved);
        resolved = resolver.resolve("mvn:file:/myrepo/foo!myGroupId/myArtifactId/1.0-SNAPSHOT");
        Assertions.assertEquals("/myrepo/foo/myGroupId/myArtifactId/1.0-SNAPSHOT/myArtifactId-1.0-SNAPSHOT.jar", resolved);
    }

    @Test
    public void testOpen() throws Exception {
        log.info("Creating local test Maven repository");
        File repository = new File("target/repository");
        repository.mkdirs();

        log.info("Populating local test Maven repository with a artifact");
        File artifact = new File(repository, "/org/example/test/1.0-SNAPSHOT/test-1.0-SNAPSHOT.txt");
        artifact.getParentFile().mkdirs();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(artifact))) {
            writer.write("This a test");
        }

        Resolver resolver = new Resolver("https://repo1.maven.org/maven2,file:target/repository");

        InputStream inputStream = resolver.open("mvn:commons-lang/commons-lang/2.6", null);
        Assertions.assertNotNull(inputStream);

        inputStream = resolver.open("mvn:org.example/test/1.0-SNAPSHOT/txt", null);
        Assertions.assertNotNull(inputStream);

        inputStream = resolver.open("mvn:notfound/notfound/1.0-SNAPSHOT", null);
        Assertions.assertNull(inputStream);

        inputStream = resolver.open("mvn:foo/test-extension/2.0", "file:target/test-classes");
        Assertions.assertNotNull(inputStream);
    }

}
