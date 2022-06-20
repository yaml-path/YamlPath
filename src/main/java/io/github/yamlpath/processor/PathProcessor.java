package io.github.yamlpath.processor;

import io.github.yamlpath.WorkUnit;

public interface PathProcessor {
    boolean canHandle(WorkUnit.Path path);

    Object handle(WorkUnit workUnit, WorkUnit.Path path);
}
