package io.github.yamlpath.processor;

import io.github.yamlpath.WorkUnit;

public class PartPathProcessor implements PathProcessor {

    @Override
    public boolean canHandle(WorkUnit.Path path) {
        return path.getTree().containsKey(path.getPart());
    }

    @Override
    public Object handle(WorkUnit workUnit, WorkUnit.Path path) {
        return path.getTree().get(path.getPart());
    }
}
