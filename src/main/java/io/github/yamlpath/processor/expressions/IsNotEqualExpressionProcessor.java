package io.github.yamlpath.processor.expressions;

import java.util.Map;

public class IsNotEqualExpressionProcessor extends IsEqualExpressionProcessor {

    @Override
    public String operator() {
        return "!=";
    }

    @Override
    public boolean evaluate(String left, String right, Map<Object, Object> resource) {
        return !super.evaluate(left, right, resource);
    }
}
