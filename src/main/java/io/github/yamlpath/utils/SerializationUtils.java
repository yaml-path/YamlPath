package io.github.yamlpath.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

public final class SerializationUtils {

    private static final String DOCUMENT_DELIMITER = "---";
    private static final ObjectMapper YAML_MAPPER = createYamlMapper(
            new YAMLGenerator.Feature[] { YAMLGenerator.Feature.MINIMIZE_QUOTES,
                    YAMLGenerator.Feature.ALWAYS_QUOTE_NUMBERS_AS_STRINGS,
                    YAMLGenerator.Feature.INDENT_ARRAYS_WITH_INDICATOR },
            new SerializationFeature[] { SerializationFeature.INDENT_OUTPUT }, new SerializationFeature[] {
                    SerializationFeature.WRITE_NULL_MAP_VALUES, SerializationFeature.WRITE_EMPTY_JSON_ARRAYS });

    private SerializationUtils() {

    }

    public static ObjectMapper yamlMapper() {
        return YAML_MAPPER;
    }

    /**
     * Unmarshals a file into a list of maps.
     *
     * @param file
     *            The {@link InputStream}.
     *
     * @return
     */
    public static List<Map<Object, Object>> unmarshalAsListOfMaps(InputStream file) throws IOException {
        return unmarshalAsListOfMaps(new String(file.readAllBytes()));
    }

    /**
     * Unmarshals a file into a list of maps.
     *
     * @param content
     *            The YAML content.
     *
     * @return
     */
    public static List<Map<Object, Object>> unmarshalAsListOfMaps(String content) throws IOException {
        String[] parts = splitDocument(content);

        List<Map<Object, Object>> list = new ArrayList<>();
        for (String part : parts) {
            if (part.trim().isEmpty()) {
                continue;
            }

            list.add(yamlMapper().readValue(part, new TypeReference<Map<Object, Object>>() {
            }));
        }

        return list;
    }

    private static String[] splitDocument(String aSpecFile) {
        List<String> documents = new ArrayList<>();
        String[] lines = aSpecFile.split("\\r?\\n");
        int nLine = 0;
        StringBuilder builder = new StringBuilder();

        while (nLine < lines.length) {
            if ((lines[nLine].length() >= DOCUMENT_DELIMITER.length()
                    && !lines[nLine].substring(0, DOCUMENT_DELIMITER.length()).equals(DOCUMENT_DELIMITER))
                    || (lines[nLine].length() < DOCUMENT_DELIMITER.length())) {
                builder.append(lines[nLine] + System.lineSeparator());
            } else {
                documents.add(builder.toString());
                builder.setLength(0);
                // To have meaningful line numbers, in jackson error messages, we need each resource
                // to retain its original position in the document.
                for (int i = 0; i <= nLine; i++) {
                    builder.append(System.lineSeparator());
                }
            }
            nLine++;
        }

        if (!builder.toString().isEmpty()) {
            documents.add(builder.toString());
        }

        return documents.toArray(new String[documents.size()]);
    }

    private static YAMLFactory createYamlFactory(YAMLGenerator.Feature[] features) {
        YAMLFactory result = new YAMLFactory();
        for (YAMLGenerator.Feature feature : features) {
            result = result.enable(feature);
        }
        return result;
    }

    private static ObjectMapper createYamlMapper(YAMLGenerator.Feature[] generatorFeatures,
            SerializationFeature[] enabledFeatures, SerializationFeature[] disabledFeatures) {
        return new ObjectMapper(createYamlFactory(generatorFeatures)) {
            {

                for (SerializationFeature feature : enabledFeatures) {
                    configure(feature, true);
                }
                for (SerializationFeature feature : disabledFeatures) {
                    configure(feature, false);
                }

            }
        };
    }
}
