package io.github.yamlpath;

import static io.github.yamlpath.utils.PathUtils.NO_REPLACEMENT;
import static io.github.yamlpath.utils.SetUtils.uniqueResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.github.yamlpath.processor.PathProcessor;
import io.github.yamlpath.utils.SerializationUtils;

/**
 * Utility to parse expressions in YAML resources that are in the form of Maps.
 */
public class YamlExpressionParser {

    private final List<Map<Object, Object>> resources;
    private final List<PathProcessor> processors;

    public YamlExpressionParser(List<Map<Object, Object>> resources) {
        this.resources = resources;
        this.processors = StreamSupport.stream(
                ServiceLoader.load(PathProcessor.class, YamlExpressionParser.class.getClassLoader()).spliterator(),
                false).collect(Collectors.toList());
    }

    /**
     * @return copy of the current state of the resources.
     */
    public List<Map<Object, Object>> getResources() {
        return Collections.unmodifiableList(resources);
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
     * Read the values at `paths`.
     *
     * @param paths
     *            list of path to check
     *
     * @return the list of values that have been found at `paths`.
     */
    public List<Object> read(List<String> paths) {
        List<Object> result = new ArrayList<>();
        for (String path : paths) {
            Set<Object> found = read(path);
            if (found.isEmpty()) {
                continue;
            }

            if (found.size() == 1) {
                result.add(found.iterator().next());
            } else {
                result.add(found);
            }
        }

        return result;
    }

    /**
     * Overwrite the value at `path`.
     *
     * @param path
     *            The path expression where to read and replace the value.
     * @param replacement
     *            The value to replace.
     */
    public YamlExpressionParser write(String path, Object replacement) {
        readAndReplace(path, replacement);
        return this;
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
    public <T> T readSingleAndReplace(String path, Object replacement) {
        Set<T> values = readAndReplace(path, replacement);
        return uniqueResult(values);
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
    public <T> Set<T> readAndReplace(String path, Object replacement) {
        Set<T> values = new HashSet<>();
        for (Map<Object, Object> resource : resources) {
            Object value = doReadAndReplace(new WorkUnit(resource, path), replacement);
            if (value != null) {
                values.add((T) value);
            }
        }

        return values;
    }

    private Object doReadAndReplace(WorkUnit workUnit, Object replacement) {
        if (!workUnit.hasNextPath()) {
            workUnit.replaceResourceWith(replacement);

            return workUnit.getResult();
        }

        WorkUnit.Path nextPath = workUnit.nextPath();
        for (PathProcessor processor : processors) {
            if (processor.canHandle(nextPath)) {
                workUnit.setResult(processor.handle(workUnit, nextPath));
                return processResult(workUnit, replacement);
            }
        }

        return null;
    }

    private Object processResult(WorkUnit workUnit, Object replacement) {
        if (workUnit.getResult() != null) {

            if (workUnit.hasNextPath()) {
                if (workUnit.getResult() instanceof Map) {
                    workUnit.setNode((Map<Object, Object>) workUnit.getResult());
                    return doReadAndReplace(workUnit, replacement);
                } else if (workUnit.getResult() instanceof List) {
                    Set<Object> values = new HashSet<>();
                    for (Object inner : (List) workUnit.getResult()) {
                        if (inner instanceof Map) {
                            workUnit.setNode((Map<Object, Object>) inner);
                            Object value = doReadAndReplace(workUnit.clone(), replacement);
                            if (value != null) {
                                values.add(value);
                            }
                        }
                    }

                    return uniqueResult(values);
                }
            } else {
                workUnit.replaceResourceWith(replacement);
            }
        }

        return workUnit.getResult();
    }
}
