package org.apache.karaf.examples.generate;

// should be annotated with Karaf @Service annotation
public class SimpleService {

    public String greeting(String message) {
        return "greeting " + message;
    }

}
