package io.github.yamlpath;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.yamlpath.utils.StringUtils;

public class YamlExpressionParserTest {

    private YamlExpressionParser parser;

    @BeforeEach
    public void setup() throws IOException {
        parser = YamlPath.from(YamlExpressionParserTest.class.getResourceAsStream("/test-kubernetes.yml"));
    }

    @Test
    public void parseSimpleExpression() throws IOException {
        Object found = parser.readSingleAndReplace("metadata.name", "{{ .Values.app.name }}");
        assertEquals("example", found);
        assertGeneratedYaml("parseSimpleExpression");
    }

    @Test
    public void parseSimpleExpressionReplicas() {
        Object found = parser.readSingleAndReplace("(kind == Deployment && metadata.name == example).spec.replicas",
                "{{ .Values.app.replicas }}");
        assertEquals(3, found);
    }

    @Test
    public void parseExpressionUsingFilterOfTypeBoolean() throws IOException {
        parser = YamlPath.from(YamlExpressionParserTest.class.getResourceAsStream("/test-routes.yml"));
        String found = parser.readSingle("applications.(name == my-app).routes.(filterBoolean == true).route");
        assertEquals("example.com", found);
    }

    @Test
    public void parseExpressionUsingFilterOfTypeNumber() throws IOException {
        parser = YamlPath.from(YamlExpressionParserTest.class.getResourceAsStream("/test-routes.yml"));
        String found = parser.readSingle("applications.(name == my-app).routes.(filterDouble == 2.2).route");
        assertEquals("example.com", found);
    }

    @Test
    public void parseExpressionUsingGreaterFilter() throws IOException {
        parser = YamlPath.from(YamlExpressionParserTest.class.getResourceAsStream("/test-routes.yml"));
        String found = parser.readSingle("applications.(name == my-app).routes.(filterDouble > 3).route");
        assertEquals("www.example.com/foo", found);
    }

    @Test
    public void parseExpressionUsingGreaterOrEqualFilter() throws IOException {
        parser = YamlPath.from(YamlExpressionParserTest.class.getResourceAsStream("/test-routes.yml"));
        String found = parser.readSingle("applications.(name == my-app).routes.(filterDouble >= 3.5).route");
        assertEquals("www.example.com/foo", found);
    }

    @Test
    public void parseExpressionUsingLesserFilter() throws IOException {
        parser = YamlPath.from(YamlExpressionParserTest.class.getResourceAsStream("/test-routes.yml"));
        String found = parser.readSingle("applications.(name == my-app).routes.(filterDouble < 3).route");
        assertEquals("example.com", found);
    }

    @Test
    public void parseExpressionUsingLesserThanFilter() throws IOException {
        parser = YamlPath.from(YamlExpressionParserTest.class.getResourceAsStream("/test-routes.yml"));
        String found = parser.readSingle("applications.(name == my-app).routes.(filterDouble <= 2.2).route");
        assertEquals("example.com", found);
    }

    @Test
    public void parseExpressionWithEscape() throws IOException {
        String found = parser.readSingleAndReplace("spec.selector.matchLabels.'app.kubernetes.io/name'",
                "{{ .Values.app.label }}");
        assertEquals("example", found);
        assertGeneratedYaml("parseExpressionWithEscape");
    }

    @Test
    public void parseArrayExpression() throws IOException {
        int found = parser.readSingleAndReplace("spec.ports.port", "{{ .Values.app.port }}");
        assertEquals(80, found);
        assertGeneratedYaml("parseArrayExpression");
    }

    @Test
    public void parseExpressionWithEqual() throws IOException {
        String found = parser.readSingleAndReplace("(kind == Deployment).metadata.name", "{{ .Values.app.name }}");
        assertEquals("example", found);
        assertGeneratedYaml("parseExpressionWithEqual");
    }

    @Test
    public void parseExpressionWithAndOperatorAndNotFound() throws IOException {
        Object found = parser.readSingleAndReplace("(kind == Deployment && metadata.name == notFound).metadata.name",
                "{{ .Values.app.name }}");
        assertNull(found);
        assertGeneratedYaml("no-changes");
    }

    @Test
    public void parseExpressionWithAndOperatorAndFound() throws IOException {
        String found = parser.readSingleAndReplace("(kind == Deployment && metadata.name == example).metadata.name",
                "{{ .Values.app.name }}");
        assertEquals("example", found);
        assertGeneratedYaml("parseExpressionWithEqual");
    }

    @Test
    public void parseExpressionWithOrOperatorAndNotFound() throws IOException {
        Object found = parser.readSingleAndReplace(
                "(metadata.name == notFound1 || metadata.name == notFound2).metadata.name", "{{ .Values.app.name }}");
        assertNull(found);
        assertGeneratedYaml("no-changes");
    }

    @Test
    public void parseExpressionWithOrOperatorAndFound() throws IOException {
        String found = parser.readSingleAndReplace(
                "(metadata.name == example || metadata.name == notFound).metadata.name", "{{ .Values.app.name }}");
        assertEquals("example", found);
        assertGeneratedYaml("parseExpressionWithOrOperatorAndFound");
    }

    @Test
    public void parseExpressionWithSeveralFilters() throws IOException {
        int found = parser.readSingleAndReplace(
                "(kind == Deployment && metadata.name == example).spec.template.spec.containers.(name == example).ports.containerPort",
                "{{ .Values.app.containerPort }}");
        assertEquals(8080, found);
        assertGeneratedYaml("parseExpressionWithSeveralFilters");
    }

    @Test
    public void parseExpressionWithSeveralFiltersUsingNumber() throws IOException {
        int found = parser.readSingleAndReplace(
                "spec.template.spec.containers.ports.(containerPort == 8080).containerPort",
                "{{ .Values.app.containerPort }}");
        assertEquals(8080, found);
        assertGeneratedYaml("parseExpressionWithSeveralFilters");
    }

    @Test
    public void parseExpressionWithWildcard() throws IOException {
        int found = parser.readSingleAndReplace("*.spec.containers.(name == example).ports.containerPort",
                "{{ .Values.app.containerPort }}");
        assertEquals(8080, found);
        assertGeneratedYaml("parseExpressionWithSeveralFilters");
    }

    @Test
    public void parseExpressionWithWildcardAndArrays() throws IOException {
        int found = parser.readSingleAndReplace("*.ports.containerPort", "{{ .Values.app.containerPort }}");
        assertEquals(8080, found);
        assertGeneratedYaml("parseExpressionWithSeveralFilters");
    }

    @Test
    public void parseExpressionWithCommandArray() throws IOException {
        List<String> found = parser.readSingleAndReplace("*.containers.command", "{{ .Values.app.command }}");
        assertEquals("command1", found.get(0));
        assertEquals("command2", found.get(1));
        assertGeneratedYaml("parseExpressionWithCommandArray");
    }

    @Test
    public void parseExpressionWithTags() throws IOException {
        parser = YamlPath.from(YamlExpressionParserTest.class.getResourceAsStream("/test-tags.yml"));
        List<String> found = parser.readSingle("tags.name");
        assertCollection("name1", found, 0);
        assertCollection("name2", found, 1);
        assertCollection("name3", found, 2);
    }

    @Test
    public void parseExpressionWithCommandAndPosition() throws IOException {
        String found = parser.readSingleAndReplace("*.containers.command[1]", "{{ .Values.app.the-command }}");
        assertEquals("command2", found);
        assertGeneratedYaml("parseExpressionWithCommandAndPosition");
    }

    @Test
    public void readSeveralDifferentValues() {
        Map<String, Object> found = parser.read(Arrays.asList("*.containers.command[0]", "*.containers.command[1]"));
        assertEquals(2, found.size());
        assertEquals("command1", found.get("*.containers.command[0]"));
        assertEquals("command2", found.get("*.containers.command[1]"));
    }

    @Test
    public void replaceLists() throws IOException {
        parser.write("*.containers.command", Arrays.asList("newCommand1", "newCommand2"));
        assertGeneratedYaml("replaceLists");
    }

    @Test
    public void replaceListsResetCommand() throws IOException {
        parser.write("*.containers.command", "newCommand3");
        assertGeneratedYaml("replaceListsResetCommand");
    }

    @Test
    public void replaceMaps() throws IOException {
        parser.write("*.containers[0]",
                Collections.singletonMap("command", Arrays.asList("newCommand1", "newCommand2")));
        assertGeneratedYaml("replaceMaps");
    }

    private void assertGeneratedYaml(String method) throws IOException {
        String actual = parser.dumpAsString();
        String expected = StringUtils
                .readAllBytes(getClass().getResourceAsStream("/expected-" + method + "-kubernetes.yml"));
        assertEquals(expected, actual, "Unexpected generated YAML file. Found: " + actual);
    }

    private <T> void assertCollection(T expected, Collection<T> collection, int index) {
        Iterator<T> it = collection.iterator();
        int curr = 0;
        while (curr < index) {
            if (!it.hasNext()) {
                fail("Collection has fewer elements than expected!");
            }

            it.next();
            curr++;
        }

        assertEquals(expected, it.next());
    }
}
