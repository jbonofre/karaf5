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
package org.apache.karaf.minho.camel;

import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.karaf.minho.boot.Minho;
import org.apache.karaf.minho.boot.service.ConfigService;
import org.apache.karaf.minho.boot.service.LifeCycleService;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

public class CamelServiceTest {

    @Test
    public void routeBuilderServiceTest() throws Exception {
        CamelService camelService = new CamelService();
        MyRouteBuilder routeBuilder = new MyRouteBuilder();
        try (final var karaf = Minho.builder()
                .loader(() -> Stream.of(new ConfigService(), new LifeCycleService(), routeBuilder, camelService)).build().start()) {
            MockEndpoint mockEndpoint = camelService.getCamelContext().getEndpoint("mock:test", MockEndpoint.class);
            mockEndpoint.expectedMessageCount(1);
            mockEndpoint.expectedBodiesReceived("Hello world!");

            ProducerTemplate producerTemplate = camelService.getCamelContext().createProducerTemplate();
            producerTemplate.sendBody("direct:test", "Hello world!");

            mockEndpoint.assertIsSatisfied();
        }
    }

}
