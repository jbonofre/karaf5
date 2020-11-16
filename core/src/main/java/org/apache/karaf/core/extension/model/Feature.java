package org.apache.karaf.core.extension.model;

import lombok.Data;

import java.util.List;

@Data
public class Feature {

    private String name;
    private String version;
    private String description;

    private List<Bundle> bundle;

}
