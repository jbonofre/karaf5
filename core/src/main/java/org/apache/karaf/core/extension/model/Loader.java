package org.apache.karaf.core.extension.model;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;

public class Loader {

    private final static ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
    }

    public final static Feature read(InputStream inputStream) throws Exception {
        return objectMapper.readValue(inputStream, Feature.class);
    }

}
