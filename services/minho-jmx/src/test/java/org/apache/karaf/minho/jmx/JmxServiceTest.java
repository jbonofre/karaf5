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
package org.apache.karaf.minho.jmx;

import org.apache.karaf.minho.boot.Minho;
import org.apache.karaf.minho.boot.service.ConfigService;
import org.apache.karaf.minho.boot.service.LifeCycleService;
import org.apache.karaf.minho.boot.service.ServiceRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.util.stream.Stream;

public class JmxServiceTest {

    @Test
    public void simpleRun() throws Exception {
        try (final Minho minho = Minho.builder().loader(() -> Stream.of(new ConfigService(), new LifeCycleService(), new JmxService())).build().start()) {

            ServiceRegistry serviceRegistry = minho.getServiceRegistry();

            JmxService jmxService = serviceRegistry.get(JmxService.class);

            jmxService.registerMBean(new TestMBeanImpl(), "org.apache.karaf.minho:type=test");

            MBeanServer mBeanServer = jmxService.getmBeanServer();

            String echo = (String) mBeanServer.invoke(new ObjectName("org.apache.karaf.minho:type=test"), "echo", new Object[]{"test"}, new String[]{"java.lang.String"});

            Assertions.assertEquals("test", echo);
        }
    }

}
