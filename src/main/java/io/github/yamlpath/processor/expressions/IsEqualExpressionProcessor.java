package io.github.yamlpath.processor.expressions;

import java.util.Collections;
import java.util.Map;

import io.github.yamlpath.YamlExpressionParser;
import io.github.yamlpath.utils.StringUtils;

public class IsEqualExpressionProcessor implements ExpressionProcessor {

    @Override
    public String operator() {
        return "==";
    }

    @Override
    public boolean evaluate(String left, String right, Map<Object, Object> resource) {
        YamlExpressionParser parser = new YamlExpressionParser(Collections.singletonList(resource));
        Object value = parser.readSingle(left);
        if (value instanceof Number) {
            return String.valueOf(value).equals(right);
        } else if (value instanceof Boolean) {
            return Boolean.valueOf(right).equals(value);
        }

        return StringUtils.equals(value, right);
    }
}
