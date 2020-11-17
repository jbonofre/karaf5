package org.apache.karaf.core.extension;

import org.easymock.EasyMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;

public class LoaderTest {

    @Test
    public void testLoadValidExtension() throws Exception {
        Loader loader = new Loader();
        BundleContext bundleContext = EasyMock.mock(BundleContext.class);
        org.osgi.framework.Bundle bundle = EasyMock.mock(org.osgi.framework.Bundle.class);
        EasyMock.expect(bundle.getSymbolicName()).andReturn("foo").anyTimes();
        EasyMock.expect(bundle.getVersion()).andReturn(Version.parseVersion("1.0")).anyTimes();
        bundle.start();
        EasyMock.expectLastCall().anyTimes();
        EasyMock.replay(bundle);
        EasyMock.expect(bundleContext.installBundle(EasyMock.anyObject())).andReturn(bundle).anyTimes();
        EasyMock.replay(bundleContext);
        loader.load("src/test/resources/test-extension.jar", bundleContext);
    }

    @Test
    public void testLoadInvalidExtension() throws Exception {
        Loader loader = new Loader();
        BundleContext bundleContext = EasyMock.mock(BundleContext.class);
        try {
            loader.load("src/test/resources/bad-extension.jar", bundleContext);
            Assertions.fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException illegalArgumentException) {
            // no-op
        }
    }

}
