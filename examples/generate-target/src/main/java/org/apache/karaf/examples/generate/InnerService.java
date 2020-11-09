package org.apache.karaf.examples.generate;

// should be annotated with Karaf @Service annotation
// could have Karaf @Configuration annotation to generated with configuration (configmap, ...)
public class InnerService {

    // should be annotated with Karaf @Lookup annotation (using the Karaf Service Gateway service)
    private SimpleService simpleService;

    public void setSimpleService(SimpleService simpleService) {
        this.simpleService = simpleService;
    }

    public String exposed(String message) {
        return simpleService.greeting(message);
    }

}
