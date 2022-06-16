package org.yamlpath;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Set;

import org.junit.jupiter.api.Test;

public class YamlPathTest {

    @Test
    public void testReadAll() throws IOException {
        Set<String> names = YamlPath.from(YamlPathTest.class.getResourceAsStream("/test-kubernetes.yml"))
                .read("metadata.name");
        assertEquals(1, names.size());
    }

    @Test
    public void testReadSingle() throws IOException {
        String name = YamlPath.from(YamlPathTest.class.getResourceAsStream("/test-kubernetes.yml"))
                .readSingle("metadata.name");
        assertEquals("example", name);
    }

    @Test
    public void testReadSingleFromString() {
        String yaml = "apiVersion: v1\n" + "kind: Service\n" + "metadata:\n" + "  name: helm-on-kubernetes-example\n";

        String kind = YamlPath.from(yaml).readSingle("kind");
        assertEquals("Service", kind);
    }
}
