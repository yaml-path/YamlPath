package io.github.yamlpath.processor.expressions;

import java.math.BigDecimal;
import java.util.Map;

public class GreaterThanExpressionProcessor extends NumberExpressionProcessor {

    @Override
    public String operator() {
        return ">";
    }

    @Override
    boolean evaluate(BigDecimal left, BigDecimal right, Map<Object, Object> node) {
        return left.compareTo(right) > 0;
    }
}
