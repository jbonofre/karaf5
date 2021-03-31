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
package org.apache.karaf.boot;

import org.apache.karaf.boot.config.KarafConfig;
import org.junit.jupiter.api.Test;

public class KarafTest {

    @Test
    public void emptyRunProgrammaticallyTest() throws Exception {
        KarafConfig karafConfig = KarafConfig.build();
        try (final var karaf = Karaf.builder().config(karafConfig).build().start()) {

        }
    }

    @Test
    public void emptyPropertyJsonTest() throws Exception {
        System.setProperty("karaf.config", "target/test-classes/emptyrun.json");
        try (final var karaf = Karaf.builder().build().start()) {

        }
    }

}
