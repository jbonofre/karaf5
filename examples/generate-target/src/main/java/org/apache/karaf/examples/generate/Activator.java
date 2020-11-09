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
