package io.github.yamlpath.processor;

import static io.github.yamlpath.utils.PathUtils.WILDCARD;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.github.yamlpath.WorkUnit;

public class WildcardPartPathProcessor implements PathProcessor {

    @Override
    public boolean canHandle(WorkUnit.Path path) {
        return path.getPart().startsWith(WILDCARD);
    }

    @Override
    public Object handle(WorkUnit workUnit, WorkUnit.Path path) {
        // if we found a wildcard means that we will find the current part at any position
        WorkUnit.Path effectivePath = workUnit.nextPath();

        List<Object> allFound = new ArrayList<>();
        findAll(effectivePath.getPart(), effectivePath.getTree(), allFound);
        return allFound;
    }

    private void findAll(String part, Map<Object, Object> node, List<Object> allFound) {
        Object found = node.get(part);
        if (found != null) {
            if (found instanceof List) {
                allFound.addAll((List) found);
            } else {
                allFound.add(found);
            }
        }

        for (Object child : node.values()) {
            if (child instanceof Map) {
                findAll(part, (Map) child, allFound);
            } else if (child instanceof List) {
                List<Object> items = (List) child;
                for (Object item : items) {
                    if (item instanceof Map) {
                        findAll(part, (Map) item, allFound);
                    }
                }
            }
        }
    }
}
