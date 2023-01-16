package io.github.yamlpath.setters;

import io.github.yamlpath.WorkUnit;

public class LastNodeSetter implements Setter {

    private final WorkUnit workUnit;

    public LastNodeSetter(WorkUnit workUnit) {
        this.workUnit = workUnit;
    }

    @Override
    public void setValue(Object value) {
        if (workUnit.getLastVisited() != null) {
            workUnit.getLastVisited().getTree().put(workUnit.getLastVisited().getPart(), value);
        }
    }
}
