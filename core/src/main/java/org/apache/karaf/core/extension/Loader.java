package org.apache.karaf.core.extension;

import lombok.extern.java.Log;
import org.apache.karaf.core.extension.model.Bundle;
import org.apache.karaf.core.extension.model.Feature;
import org.osgi.framework.BundleContext;

import java.io.File;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

@Log
public class Loader {

    public static void load(String url, BundleContext bundleContext) throws Exception {
        JarFile jarFile = new JarFile(new File(url));
        ZipEntry entry = jarFile.getEntry("KARAF-INF/extension.json");
        if (entry == null) {
            throw new IllegalArgumentException(url + " is not a Karaf extension");
        }
        Feature feature = org.apache.karaf.core.extension.model.Loader.read(jarFile.getInputStream(entry));
        log.info("Loading " + feature.getName() + "/" + feature.getVersion() + " extension");
        for (Bundle bundle : feature.getBundle()) {
            org.osgi.framework.Bundle concreteBundle = bundleContext.installBundle(bundle.getLocation());
            log.info("Installing " + concreteBundle.getSymbolicName() + "/" + concreteBundle.getVersion());
            concreteBundle.start();
            log.info("Starting " + concreteBundle.getSymbolicName() + "/" + concreteBundle.getVersion());
        }
    }

}
