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
package org.apache.karaf.core.specs;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class SpecsLocatorTest {

    @BeforeAll
    public static void setup() {
        SpecsLocator.register("Factory", new MockCallable());
        SpecsLocator.register("Factory", new MockCallable2());
    }

    @Test
    @Disabled("TODO")
    public void testLocatorWithSystemProperty() {
        System.setProperty(SpecsLocator.TIMEOUT, "0");
        System.setProperty("Factory", "org.apache.karaf.core.specs.MockCallable");
        Class clazz = SpecsLocator.locate(Object.class, "Factory");
        Assertions.assertNotNull(clazz);
        Assertions.assertEquals(MockCallable.class.getName(), clazz.getName());

        System.setProperty("Factory", "org.apache.karaf.core.specs.foo");
        clazz = SpecsLocator.locate(Object.class, "Factory");
        Assertions.assertNull(clazz);
    }

    @Test
    @Disabled("TODO")
    public void testLocatorWithoutSystemProperty() {
        System.setProperty(SpecsLocator.TIMEOUT, "0");
        System.clearProperty("Factory");
        Class clazz = SpecsLocator.locate(Object.class, "Factory");
        Assertions.assertNotNull(clazz);
        Assertions.assertEquals(MockCallable2.class.getName(), clazz.getName());
    }

    @Test
    @Disabled("TODO")
    public void testLocatorWithSystemPropertyAndTimeout() {
        long timeout = 1000;
        System.setProperty(SpecsLocator.TIMEOUT, Long.toString(timeout));
        System.setProperty("Factory", "org.apache.karaf.core.specs.MockCallable");
        Class clazz = SpecsLocator.locate(Object.class, "Factory");
        Assertions.assertNotNull(clazz);
        Assertions.assertEquals(MockCallable.class.getName(), clazz.getName());

        System.setProperty("Factory", "org.apache.karaf.specs.locator");
        long t0 = System.currentTimeMillis();
        clazz = SpecsLocator.locate(Object.class, "Factory");
        long t1 = System.currentTimeMillis();
        Assertions.assertNull(clazz);
        Assertions.assertTrue((t1 - t0) > timeout / 2);
    }

    @Test
    @Disabled("TODO")
    public void testLocatorWithoutSystemPropertyAndTimeout() {
        long timeout = 1000;
        System.setProperty(SpecsLocator.TIMEOUT, Long.toString(timeout));
        System.clearProperty("Factory");
        long t0 = System.currentTimeMillis();
        Class clazz = SpecsLocator.locate(Object.class, "Factory");
        long t1 = System.currentTimeMillis();
        Assertions.assertNotNull(clazz);
        Assertions.assertEquals(MockCallable2.class.getName(), clazz.getName());
        Assertions.assertTrue((t1 - t0) < timeout / 2);
    }

}
