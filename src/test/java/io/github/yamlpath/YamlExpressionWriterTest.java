package io.github.yamlpath;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.yamlpath.utils.StringUtils;

public class YamlExpressionWriterTest {

    private YamlExpressionParser parser;

    @BeforeEach
    public void setup() throws IOException {
        parser = YamlPath.from(YamlExpressionParserTest.class.getResourceAsStream("/test-applications.yml"));
    }

    @Test
    public void writeYamlWithLists() throws IOException {
        parser.write("applications.routes[0].route", "writeYamlWithLists");

        assertGeneratedFile(parser, "writeYamlWithLists");
    }

    @Test
    public void writeYamlWithMaps() throws IOException {
        parser.write("applications.routes[0]", Collections.singletonMap("route", "writeYamlWithMaps"));

        assertGeneratedFile(parser, "writeYamlWithMaps");
    }

    @Test
    public void replaceYamlListsWithDescendants() throws IOException {
        List<java.util.Map<String, String>> replacement = new java.util.ArrayList<>();
        replacement.add(Collections.singletonMap("route", "replaceYamlListsWithDescendants"));
        parser.write(".*.routes", replacement);

        assertGeneratedFile(parser, "replaceYamlListsWithDescendants");
    }

    @Test
    public void writeAtPositionYamlLists() throws IOException {
        Map<String, String> replacement = Collections.singletonMap("route", "writeAtPositionYamlLists");
        parser.write(".*.routes[1]", replacement);

        assertGeneratedFile(parser, "writeAtPositionYamlLists");
    }

    private void assertGeneratedFile(YamlExpressionParser parser, String method) throws IOException {
        String expected = StringUtils
                .readAllBytes(getClass().getResourceAsStream("/expected-" + method + "-applications.yml"));
        assertEquals(expected, parser.dumpAsString());
    }
}
