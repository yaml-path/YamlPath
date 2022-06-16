package org.yamlpath;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.yamlpath.utils.SerializationUtils;

public final class YamlPath {

    private YamlPath() {

    }

    public static YamlExpressionParser from(InputStream... inputStreams) throws IOException {
        List<Map<Object, Object>> resources = new ArrayList<>();
        for (InputStream is : inputStreams) {
            resources.addAll(SerializationUtils.unmarshalAsListOfMaps(is));
        }

        return new YamlExpressionParser(resources);
    }

    public static YamlExpressionParser from(String... yamls) {
        List<Map<Object, Object>> resources = new ArrayList<>();
        for (String yaml : yamls) {
            try {
                resources.addAll(SerializationUtils.unmarshalAsListOfMaps(yaml));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return new YamlExpressionParser(resources);
    }
}
