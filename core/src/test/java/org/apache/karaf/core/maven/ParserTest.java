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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ParserTest {

    @Test
    public void testPath() throws Exception {
        String uri = "mvn:org.mygroup/myartifact/1.0-SNAPSHOT";
        String parsed = Parser.pathFromMaven(uri);
        Assertions.assertEquals("org/mygroup/myartifact/1.0-SNAPSHOT/myartifact-1.0-SNAPSHOT.jar", parsed);
    }

    @Test
    public void testWithRepository() throws Exception {
        String uri = "mvn:https://repo1.maven.org/maven2!org.mygroup/myartifact/1.0-SNAPSHOT";
        String parsed = Parser.pathFromMaven(uri);
        Assertions.assertEquals("https://repo1.maven.org/maven2/org/mygroup/myartifact/1.0-SNAPSHOT/myartifact-1.0-SNAPSHOT.jar", parsed);
    }

    @Test
    public void testFileName() throws Exception {
        String uri = "mvn:org.mygroup/myartifact/1.0-SNAPSHOT";
        String parsed = Parser.fileNameFromMaven(uri, false);
        Assertions.assertEquals("myartifact-1.0-SNAPSHOT.jar", parsed);
    }

}
