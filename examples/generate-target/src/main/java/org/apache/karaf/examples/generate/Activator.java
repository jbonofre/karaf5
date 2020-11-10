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
package org.apache.karaf.examples.generate;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

// should be generated with karaf-maven-plugin, no code there
public class Activator implements BundleActivator {

    // generated based on Karaf @Service annotation
    ServiceRegistration<SimpleService> simpleServiceRegistration;

    // generated based on Karaf @Lookup annotation
    ServiceTracker<SimpleService, SimpleService> simpleServiceTracker;

    // generated based on Karaf @Service annotation
    ServiceRegistration<InnerService> innerServiceServiceRegistration;

    @Override
    public void start(final BundleContext bundleContext) throws Exception {
        // dictionary can be populated based on Karaf @Service annotation
        simpleServiceRegistration = bundleContext.registerService(SimpleService.class, new SimpleService(), null);

        simpleServiceTracker = new ServiceTracker<SimpleService, SimpleService>(bundleContext, SimpleService.class, null) {
            @Override
            public SimpleService addingService(ServiceReference<SimpleService> reference) {
                SimpleService simpleService = bundleContext.getService(reference);
                InnerService innerService = new InnerService();
                innerService.setSimpleService(simpleService);
                innerServiceServiceRegistration = bundleContext.registerService(InnerService.class, innerService, null);
                return simpleService;
            }
            @Override
            public void removedService(ServiceReference<SimpleService> reference, SimpleService service) {
                innerServiceServiceRegistration.unregister();
            }
        };
        simpleServiceTracker.open();
    }

    @Override
    public void stop(final BundleContext bundleContext)  throws Exception {
        simpleServiceTracker.close();
        simpleServiceRegistration.unregister();
    }


}
