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
package org.apache.karaf.tooling.common.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;

public class KarafBuildTest {

    @Test
    public void unmarshalTest() throws Exception {
        KarafBuild karafBuild = KarafBuild.load(new FileInputStream("target/test-classes/karaf-build.json"));

        Assertions.assertEquals("test-build", karafBuild.getName());
        Assertions.assertEquals(2, karafBuild.getProperties().size());
        Assertions.assertEquals(true, karafBuild.getProperties().get("include.transitive"));
        Assertions.assertEquals(3, karafBuild.getDependencies().size());
        Assertions.assertEquals("k5:karaf-boot", karafBuild.getDependencies().get(0));
        Assertions.assertEquals("k5:karaf-http", karafBuild.getDependencies().get(1));
        Assertions.assertEquals("mvn:commons-lang/commons-lang/2.6", karafBuild.getDependencies().get(2));
    }

}
