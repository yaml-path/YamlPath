package io.github.yamlpath;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Collections;

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

    private void assertGeneratedFile(YamlExpressionParser parser, String method) throws IOException {
        String expected = StringUtils
                .readAllBytes(getClass().getResourceAsStream("/expected-" + method + "-applications.yml"));
        assertEquals(expected, parser.dumpAsString());
    }
}
