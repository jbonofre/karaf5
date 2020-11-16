package org.apache.karaf.core.extension.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

public class LoaderTest {

    @Test
    public void testRead() throws Exception {
        String json = "{" +
                "\"name\": \"test\"," +
                "\"version\": \"1.0\"," +
                "\"bundle\": [" +
                "{ \"location\": \"url\" }" +
                "]" +
                "}";
        Feature feature = Loader.read(new ByteArrayInputStream(json.getBytes()));
        Assertions.assertEquals("test", feature.getName());
        Assertions.assertEquals("1.0", feature.getVersion());
        Assertions.assertEquals(1, feature.getBundle().size());
        Assertions.assertEquals("url", feature.getBundle().get(0).getLocation());
    }

}
