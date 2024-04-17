package io.github.yamlpath.processor.expressions;

import java.math.BigDecimal;
import java.util.Map;

public class LesserOrEqualThanExpressionProcessor extends NumberExpressionProcessor {

    @Override
    public String operator() {
        return "<=";
    }

    @Override
    public int getPriority() {
        // to give it more priority than the LesserThanExpressionProcessor processor.
        return 1;
    }

    @Override
    boolean evaluate(BigDecimal left, BigDecimal right, Map<Object, Object> node) {
        return left.compareTo(right) <= 0;
    }
}
