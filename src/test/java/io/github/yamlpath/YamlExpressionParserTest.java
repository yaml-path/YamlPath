package io.github.yamlpath;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class YamlExpressionParserTest {

    private YamlExpressionParser parser;

    @BeforeEach
    public void setup() throws IOException {
        parser = YamlPath.from(YamlExpressionParserTest.class.getResourceAsStream("/test-kubernetes.yml"));
    }

    @Test
    public void parseSimpleExpression() throws IOException {
        String found = parser.readSingleAndSet("metadata.name", "{{ .Values.app.name }}");
        assertEquals("example", found);
        assertGeneratedYaml("parseSimpleExpression");
    }

    @Test
    public void parseExpressionWithEscape() throws IOException {
        String found = parser.readSingleAndSet("spec.selector.matchLabels.'app.kubernetes.io/name'",
                "{{ .Values.app.label }}");
        assertEquals("example", found);
        assertGeneratedYaml("parseExpressionWithEscape");
    }

    @Test
    public void parseArrayExpression() throws IOException {
        int found = parser.readSingleAndSet("spec.ports.port", "{{ .Values.app.port }}");
        assertEquals(80, found);
        assertGeneratedYaml("parseArrayExpression");
    }

    @Test
    public void parseExpressionWithEqual() throws IOException {
        String found = parser.readSingleAndSet("(kind == Deployment).metadata.name", "{{ .Values.app.name }}");
        assertEquals("example", found);
        assertGeneratedYaml("parseExpressionWithEqual");
    }

    @Test
    public void parseExpressionWithAndOperatorAndNotFound() throws IOException {
        Object found = parser.readSingleAndSet("(kind == Deployment && metadata.name == notFound).metadata.name",
                "{{ .Values.app.name }}");
        assertNull(found);
        assertGeneratedYaml("no-changes");
    }

    @Test
    public void parseExpressionWithAndOperatorAndFound() throws IOException {
        String found = parser.readSingleAndSet("(kind == Deployment && metadata.name == example).metadata.name",
                "{{ .Values.app.name }}");
        assertEquals("example", found);
        assertGeneratedYaml("parseExpressionWithEqual");
    }

    @Test
    public void parseExpressionWithOrOperatorAndNotFound() throws IOException {
        Object found = parser.readSingleAndSet(
                "(metadata.name == notFound1 || metadata.name == notFound2).metadata.name", "{{ .Values.app.name }}");
        assertNull(found);
        assertGeneratedYaml("no-changes");
    }

    @Test
    public void parseExpressionWithOrOperatorAndFound() throws IOException {
        String found = parser.readSingleAndSet("(metadata.name == example || metadata.name == notFound).metadata.name",
                "{{ .Values.app.name }}");
        assertEquals("example", found);
        assertGeneratedYaml("parseExpressionWithOrOperatorAndFound");
    }

    @Test
    public void parseExpressionWithSeveralFilters() throws IOException {
        int found = parser.readSingleAndSet(
                "(kind == Deployment && metadata.name == example).spec.template.spec.containers.(name == example).ports.containerPort",
                "{{ .Values.app.containerPort }}");
        assertEquals(8080, found);
        assertGeneratedYaml("parseExpressionWithSeveralFilters");
    }

    @Test
    public void parseExpressionWithWildcard() throws IOException {
        int found = parser.readSingleAndSet("*.spec.containers.(name == example).ports.containerPort",
                "{{ .Values.app.containerPort }}");
        assertEquals(8080, found);
        assertGeneratedYaml("parseExpressionWithSeveralFilters");
    }

    @Test
    public void parseExpressionWithWildcardAndArrays() throws IOException {
        int found = parser.readSingleAndSet("*.ports.containerPort", "{{ .Values.app.containerPort }}");
        assertEquals(8080, found);
        assertGeneratedYaml("parseExpressionWithSeveralFilters");
    }

    @Test
    public void parseExpressionWithCommandArray() throws IOException {
        List<String> found = parser.readSingleAndSet("*.containers.command", "{{ .Values.app.command }}");
        assertEquals("command1", found.get(0));
        assertEquals("command2", found.get(1));
        assertGeneratedYaml("parseExpressionWithCommandArray");
    }

    private void assertGeneratedYaml(String method) throws IOException {
        String actual = parser.writeAsString();
        String expected = new String(
                getClass().getResourceAsStream("/expected-" + method + "-kubernetes.yml").readAllBytes());
        assertEquals(expected, actual, "Unexpected generated YAML file. Found: " + actual);
    }
}
