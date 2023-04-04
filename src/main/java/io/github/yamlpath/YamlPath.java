package io.github.yamlpath;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

import io.github.yamlpath.utils.SerializationUtils;
import io.github.yamlpath.utils.StringUtils;

public final class YamlPath {

    private YamlPath() {

    }

    public static YamlExpressionParser from(InputStream... inputStreams) throws IOException {
        List<Map<Object, Object>> resources = new ArrayList<>();
        for (InputStream is : inputStreams) {
            String yaml = StringUtils.readAllBytes(is);
            resources.addAll(fromContent(yaml));
        }

        return new YamlExpressionParser(resources);
    }

    public static YamlExpressionParser from(String... yamls) {
        List<Map<Object, Object>> resources = new ArrayList<>();
        for (String yaml : yamls) {
            try {
                resources.addAll(fromContent(yaml));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return new YamlExpressionParser(resources);
    }

    private static Collection<Map<Object, Object>> fromContent(String content) throws JsonProcessingException {
        try {
            return SerializationUtils.unmarshalAsListOfMaps(content);
        } catch (IOException e) {
            return SerializationUtils.yamlMapper().readValue(content, new TypeReference<List<Map<Object, Object>>>() {
            });
        }
    }
}
