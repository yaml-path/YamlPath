package io.github.yamlpath;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.github.yamlpath.utils.SerializationUtils;
import io.github.yamlpath.utils.StringUtils;

/**
 * Utility to parse expressions in YAML resources that are in the form of Maps.
 */
public class YamlExpressionParser {

    private static final String NO_REPLACEMENT = "no";
    private static final String WILDCARD = "*.";
    private static final String ESCAPE = "'";
    private static final String DOT = ".";

    private final List<Map<Object, Object>> resources;

    public YamlExpressionParser(List<Map<Object, Object>> resources) {
        this.resources = resources;
    }

    /**
     * Write the current resources with the replacements set (if any) into a string.
     *
     * @return the resources in string format.
     */
    public String dumpAsString() {
        try {
            return SerializationUtils.yamlMapper().writeValueAsString(resources);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Read the value at `path`.
     *
     * @param path
     *            The path expression where to read and replace the value.
     *
     * @return a single result found at `path`. It throws an exception if more than one value is found.
     */
    public <T> T readSingle(String path) {
        return (T) readSingleAndReplace(path, NO_REPLACEMENT);
    }

    /**
     * Read the value at `path`.
     *
     * @param path
     *            The path expression where to read and replace the value.
     *
     * @return the list of values that have been found at `path`.
     */
    public <T> Set<T> read(String path) {
        return readAndReplace(path, NO_REPLACEMENT);
    }

    /**
     * Overwrite the value at `path`.
     *
     * @param path
     *            The path expression where to read and replace the value.
     * @param replacement
     *            The value to replace.
     */
    public void write(String path, String replacement) {
        readAndReplace(path, replacement);
    }

    /**
     * Read and set the value at `path` with the replacement value.
     *
     * @param path
     *            The path expression where to read and replace the value.
     * @param replacement
     *            The value to replace.
     *
     * @return a single result found at `path`. It throws an exception if more than one value is found.
     */
    public <T> T readSingleAndReplace(String path, String replacement) {
        Set<T> values = readAndReplace(path, replacement);
        return uniqueResult(values, path);
    }

    /**
     * Read and set the value at `path` with the replacement value.
     *
     * @param path
     *            The path expression where to read and replace the value.
     * @param replacement
     *            The value to replace.
     *
     * @return the list of distinct values that have been found at `path`.
     */
    public <T> Set<T> readAndReplace(String path, String replacement) {
        Set<T> values = new HashSet<>();
        for (Map<Object, Object> resource : resources) {
            Object value = readValuesForResource(resource, path, replacement);
            if (value != null) {
                values.add((T) value);
            }
        }

        return values;
    }

    private static <T> T readValuesForResource(Map<Object, Object> resource, String path, String replacement) {

        String currentPart;
        int nextIndex;
        PathProcessor processor;

        // if we have several dots, for example: "..spec.rest", we move the pointer to "spec.rest"
        while (path.indexOf(DOT) == 0) {
            path = path.substring(1);
        }

        if (path.startsWith("(")) {
            // Handle expressions where path is "(kind == Service).rest":
            // increase pointer to ".rest":
            nextIndex = path.indexOf(")");
            // current part is now "kind == Service":
            currentPart = path.substring(1, nextIndex);
            processor = locateExpressionPathProcessor(currentPart);
        } else if (path.startsWith(ESCAPE)) {
            // Handle escaped parts where path is "'a.b.c'.rest"
            String nextPart = path.substring(1);
            // increase pointer to ".rest":
            nextIndex = nextPart.indexOf(ESCAPE);
            // current part is now "a.b.c":
            currentPart = nextPart.substring(0, nextIndex);
            processor = new PartPathProcessor(currentPart);
        } else if (path.startsWith(WILDCARD)) {
            // if we found a wildcard means that we will find the current part at any position
            path = path.substring(WILDCARD.length());
            // increase pointer to ".rest":
            nextIndex = path.indexOf(DOT);
            // current part is now "spec":
            currentPart = normalize(nextIndex > 0 ? path.substring(0, nextIndex) : path);
            processor = new WildcardPartPathProcessor(currentPart);
        } else {
            // increase pointer to ".rest":
            nextIndex = path.indexOf(DOT);
            // current part is now "spec":
            currentPart = normalize(nextIndex > 0 ? path.substring(0, nextIndex) : path);
            processor = new PartPathProcessor(currentPart);
        }

        T result = null;
        if (processor.canHandle(resource)) {
            result = (T) processor.handle(resource);
            if (nextIndex >= 0) {
                if (result instanceof Map) {
                    return (T) readValuesForResource((Map) result, path.substring(nextIndex + 1), replacement);
                } else if (result instanceof List) {
                    String nextPath = path.substring(nextIndex + 1);
                    Set<T> values = new HashSet<>();
                    for (Object inner : (List) result) {
                        if (inner instanceof Map) {
                            T value = (T) readValuesForResource((Map) inner, nextPath, replacement);
                            if (value != null) {
                                values.add(value);
                            }
                        }
                    }

                    return uniqueResult(values, nextPath);
                }
            }

            if (!NO_REPLACEMENT.equals(replacement)) {
                resource.put(currentPart, replacement);
            }
        }

        return result;
    }

    private static <T> T uniqueResult(Set<T> set, String path) {
        if (set.size() > 1) {
            throw new IllegalStateException("Several matches for the path: " + path + ". Found: " + set);
        } else if (set.isEmpty()) {
            return null;
        }

        return set.iterator().next();
    }

    private static PathProcessor locateExpressionPathProcessor(String expression) {
        for (PathExpression type : PathExpression.values()) {
            if (expression.contains(type.getOperator())) {
                return type.getPathProcessor(expression);
            }
        }

        throw new IllegalStateException("Unrecognised expression: " + expression);
    }

    private static String normalize(String text) {
        if (StringUtils.isNullOrEmpty(text)) {
            return text;
        }

        String normalized = text.trim();
        if (normalized.startsWith(ESCAPE)) {
            normalized = normalized.substring(1, normalized.length() - 1);
        }

        return normalized;
    }

    static class WildcardPartPathProcessor implements PathProcessor {

        private final String part;
        private final List<Object> allFound = new ArrayList<>();

        WildcardPartPathProcessor(String part) {
            this.part = part;
        }

        @Override
        public boolean canHandle(Map<Object, Object> resource) {
            Object found = resource.get(part);
            if (found != null) {
                if (found instanceof List) {
                    allFound.addAll((List) found);
                } else {
                    allFound.add(found);
                }
            }

            for (Object child : resource.values()) {
                if (child instanceof Map) {
                    canHandle((Map) child);
                } else if (child instanceof List) {
                    List<Object> items = (List) child;
                    for (Object item : items) {
                        if (item instanceof Map) {
                            canHandle((Map) item);
                        }
                    }
                }
            }

            return !allFound.isEmpty();
        }

        @Override
        public Object handle(Map<Object, Object> resource) {
            return allFound;
        }
    }

    static class PartPathProcessor implements PathProcessor {

        private final String part;

        PartPathProcessor(String part) {
            this.part = part;
        }

        @Override
        public boolean canHandle(Map<Object, Object> resource) {
            return resource.containsKey(part);
        }

        @Override
        public Object handle(Map<Object, Object> resource) {
            return resource.get(part);
        }
    }

    static class AndPathExpression extends CompositeFilterPathExpression {

        AndPathExpression(String condition) {
            super(PathExpression.AND.getOperator(), condition);
        }

        @Override
        public boolean canHandle(Map<Object, Object> resource) {
            return conditions.stream().allMatch(c -> c.canHandle(resource));
        }
    }

    static class OrPathExpression extends CompositeFilterPathExpression {

        OrPathExpression(String condition) {
            super(PathExpression.OR.getOperator(), condition);
        }

        @Override
        public boolean canHandle(Map<Object, Object> resource) {
            return conditions.stream().anyMatch(c -> c.canHandle(resource));
        }
    }

    abstract static class CompositeFilterPathExpression implements PathProcessor {

        protected final List<PathProcessor> conditions;

        CompositeFilterPathExpression(String operator, String condition) {
            this.conditions = new ArrayList<>();
            String[] subConditions = condition.split(Pattern.quote(operator));
            for (String subCondition : subConditions) {
                this.conditions.add(locateExpressionPathProcessor(subCondition));
            }
        }

        @Override
        public Object handle(Map<Object, Object> resource) {
            return resource;
        }
    }

    static class IsEqualPathExpression implements PathProcessor {

        private final String left;
        private final String right;

        IsEqualPathExpression(String condition) {
            String[] parts = condition.split(PathExpression.IS_EQUAL.getOperator());
            this.left = normalize(parts[0]);
            this.right = normalize(parts[1]);
        }

        @Override
        public boolean canHandle(Map<Object, Object> resource) {
            Object value = readValuesForResource(resource, left, NO_REPLACEMENT);
            return value != null && value instanceof String && StringUtils.equals(value, right);
        }

        @Override
        public Object handle(Map<Object, Object> resource) {
            return resource;
        }
    }

    interface PathProcessor {
        boolean canHandle(Map<Object, Object> resource);

        Object handle(Map<Object, Object> resource);
    }

    enum PathExpression {
        // @formatter:off
        AND("&&", AndPathExpression::new), OR("||", OrPathExpression::new), IS_EQUAL("==", IsEqualPathExpression::new);
        // @formatter:on

        String operator;
        Function<String, PathProcessor> supplier;

        PathExpression(String operator, Function<String, PathProcessor> supplier) {
            this.operator = operator;
            this.supplier = supplier;
        }

        public String getOperator() {
            return operator;
        }

        public PathProcessor getPathProcessor(String condition) {
            return supplier.apply(condition);
        }
    }
}
