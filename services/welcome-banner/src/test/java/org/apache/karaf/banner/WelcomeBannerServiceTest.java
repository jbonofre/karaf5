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
package org.apache.karaf.banner;

import org.apache.karaf.boot.Karaf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class WelcomeBannerServiceTest {

    @Test
    public void defaultBannerTest() throws Exception {
        TestHandler testHandler = new TestHandler();
        Logger logger = Logger.getLogger("org.apache.karaf.banner");
        logger.addHandler(testHandler);
        Karaf.builder().build().start();

        Assertions.assertTrue(testHandler.getMessages().contains("Apache Karaf 5.x"));
    }

    @Test
    public void systemPropertyBannerTest() throws Exception {
        System.setProperty("karaf.banner", "My Test Banner");

        TestHandler testHandler = new TestHandler();
        Logger logger = Logger.getLogger("org.apache.karaf.banner");
        logger.addHandler(testHandler);
        Karaf.builder().build().start();

        Assertions.assertTrue(testHandler.getMessages().contains("My Test Banner"));
    }

    class TestHandler extends Handler {

        StringBuilder builder = new StringBuilder();

        @Override
        public void publish(LogRecord record) {
            builder.append(record.getMessage()).append("\n");
        }

        @Override
        public void flush() {
            // no-op
        }

        @Override
        public void close() throws SecurityException {
            // no-op
        }

        public String getMessages() {
            return builder.toString();
        }
    }

}
