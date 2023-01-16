package io.github.yamlpath.processor;

import static io.github.yamlpath.utils.PathUtils.INDEX_CLOSE;
import static io.github.yamlpath.utils.PathUtils.INDEX_OPEN;

import java.util.List;

import io.github.yamlpath.WorkUnit;
import io.github.yamlpath.setters.ListAtPositionSetter;

public class IndexPartPathProcessor implements PathProcessor {

    @Override
    public boolean canHandle(WorkUnit.Path path) {
        return path.getPart().contains(INDEX_OPEN) && path.getPart().endsWith(INDEX_CLOSE);
    }

    @Override
    public Object handle(WorkUnit workUnit, WorkUnit.Path path) {
        int indexOfIndexOpen = path.getPart().indexOf(INDEX_OPEN);
        String actualPart = path.getPart().substring(0, indexOfIndexOpen);

        Object value = path.getTree().get(actualPart);
        if (value instanceof List) {
            int indexOfIndexClose = path.getPart().indexOf(INDEX_CLOSE);
            int position = Integer.parseInt(path.getPart().substring(indexOfIndexOpen + 1, indexOfIndexClose));
            workUnit.setSetter(new ListAtPositionSetter((List) value, position));

            return ((List) value).get(position);
        } else {
            throw new IllegalStateException("Expected a collection at " + path.getPart());
        }
    }
}
