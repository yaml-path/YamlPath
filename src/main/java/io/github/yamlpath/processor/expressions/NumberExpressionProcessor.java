package io.github.yamlpath.processor.expressions;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

import io.github.yamlpath.YamlExpressionParser;

public abstract class NumberExpressionProcessor implements ExpressionProcessor {

    abstract boolean evaluate(BigDecimal left, BigDecimal right, Map<Object, Object> node);

    public final boolean evaluate(String left, String right, Map<Object, Object> node) {
        YamlExpressionParser parser = new YamlExpressionParser(Collections.singletonList(node));
        Object value = parser.readSingle(left);
        if (value instanceof Number) {
            return evaluate(new BigDecimal(value.toString()), new BigDecimal(right), node);
        }

        return false;
    }
}
