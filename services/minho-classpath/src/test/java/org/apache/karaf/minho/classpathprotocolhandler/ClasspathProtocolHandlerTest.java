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
package org.apache.karaf.minho.classpathprotocolhandler;

import org.apache.karaf.minho.boot.Minho;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class ClasspathProtocolHandlerTest {

    private static Minho minho;

    @BeforeAll
    public static void setup() {
        minho = Minho.builder().build().start();
    }

    @AfterAll
    public static void teardown() {
        minho.close();
    }

    @Test
    public void loading() throws Exception {
        URL url = new URL("classpath:foo/bar.jar");
    }

    @Test
    public void reading() throws Exception {
        URL url = new URL("classpath:foo/bar.txt");
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
            builder.append(reader.readLine());
        }
        Assertions.assertEquals("This is a test!", builder.toString());
    }

    @Test
    public void nonManagedURL() {
        try {
            URL url = new URL("foo:bar");
        } catch (MalformedURLException e) {
            return;
        }
        Assertions.fail("MalformedURLException expected");
    }

}
