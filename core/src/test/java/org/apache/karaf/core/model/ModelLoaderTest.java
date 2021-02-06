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
package org.apache.karaf.core.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

public class ModelLoaderTest {

    @Test
    public void testRead() throws Exception {
        String json = "{" +
                "\"name\": \"test\"," +
                "\"version\": \"1.0\"," +
                "\"config\": [ " +
                "]," +
                "\"extension\": [" +
                " \"mvn:other/other/1.0\", \"http://host/remote\"" +
                "]," +
                "\"module\": [" +
                "{ \"location\": \"url\" }" +
                "]" +
                "}";
        Extension extension = ModelLoader.read(new ByteArrayInputStream(json.getBytes()));
        Assertions.assertEquals("test", extension.getName());
        Assertions.assertEquals("1.0", extension.getVersion());
        Assertions.assertEquals(2, extension.getExtension().size());
        Assertions.assertEquals("mvn:other/other/1.0", extension.getExtension().get(0));
        Assertions.assertEquals("http://host/remote", extension.getExtension().get(1));
        Assertions.assertEquals(1, extension.getModule().size());
        Assertions.assertEquals("url", extension.getModule().get(0).getLocation());
    }

}
