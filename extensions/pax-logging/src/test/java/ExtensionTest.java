import org.apache.karaf.core.Karaf;
import org.apache.karaf.core.KarafConfig;
import org.junit.jupiter.api.Test;

public class ExtensionTest {

    @Test
    public void test() throws Exception {
        Karaf karaf = Karaf.build(KarafConfig.builder()
                .homeDirectory("target/karaf")
                .cacheDirectory("target/karaf/cache")
                .clearCache(true)
                .build());
        karaf.init();
        karaf.addExtension("file:src/main/resources/KARAF-INF/extension.json");
        karaf.start();
    }

}
