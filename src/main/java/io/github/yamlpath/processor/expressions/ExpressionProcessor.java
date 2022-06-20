package io.github.yamlpath.processor.expressions;

import java.util.Map;

public interface ExpressionProcessor {
    default int getPriority() {
        return Integer.MIN_VALUE;
    }

    String operator();

    boolean evaluate(String left, String right, Map<Object, Object> node);
}
