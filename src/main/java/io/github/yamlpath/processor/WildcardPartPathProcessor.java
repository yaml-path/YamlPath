package io.github.yamlpath.processor;

import static io.github.yamlpath.utils.PathUtils.INDEX_CLOSE;
import static io.github.yamlpath.utils.PathUtils.INDEX_OPEN;
import static io.github.yamlpath.utils.PathUtils.WILDCARD;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.github.yamlpath.WorkUnit;
import io.github.yamlpath.setters.ListAtPositionSetter;
import io.github.yamlpath.setters.ListSetter;
import io.github.yamlpath.setters.MapAtKeySetter;
import io.github.yamlpath.setters.MapSetter;
import io.github.yamlpath.setters.MultipleSetter;

public class WildcardPartPathProcessor implements PathProcessor {

    @Override
    public boolean canHandle(WorkUnit.Path path) {
        return path.getPart().startsWith(WILDCARD);
    }

    @Override
    public Object handle(WorkUnit workUnit, WorkUnit.Path path) {
        MultipleSetter setter = new MultipleSetter();
        // if we found a wildcard means that we will find the current part at any position
        WorkUnit.Path effectivePath = workUnit.nextPath();

        List<Object> allFound = new ArrayList<>();
        findAll(setter, effectivePath.getPart(), effectivePath.getTree(), allFound);
        workUnit.setSetter(setter);
        return allFound;
    }

    private void findAll(MultipleSetter setter, String part, Map<Object, Object> node, List<Object> allFound) {
        Integer position = null;
        String actualPart = part;
        if (part.contains(INDEX_OPEN)) {
            int indexOfIndexOpen = part.indexOf(INDEX_OPEN);
            actualPart = part.substring(0, indexOfIndexOpen);

            int indexOfIndexClose = part.indexOf(INDEX_CLOSE);
            position = Integer.parseInt(part.substring(indexOfIndexOpen + 1, indexOfIndexClose));
        }

        Object found = node.get(actualPart);
        if (found != null) {
            if (found instanceof List) {
                List value = (List) found;
                if (position != null) {
                    if (position >= value.size()) {
                        return;
                    }

                    setter.add(new ListAtPositionSetter(value, position));
                    allFound.add(value.get(position));
                } else {
                    setter.add(new ListSetter(value));
                    allFound.addAll(value);
                }

            } else if (position != null) {
                return;
            } else if (found instanceof Map) {
                Map value = (Map) found;
                allFound.add(value);
                setter.add(new MapSetter(value, new MapAtKeySetter(node, part)));

            } else {
                allFound.add(found);
                setter.add(new MapAtKeySetter(node, part));
            }
        }

        for (Object child : node.values()) {
            if (child instanceof Map) {
                findAll(setter, part, (Map) child, allFound);
            } else if (child instanceof List) {
                List<Object> items = (List) child;
                for (Object item : items) {
                    if (item instanceof Map) {
                        findAll(setter, part, (Map) item, allFound);
                    }
                }
            }
        }
    }
}
