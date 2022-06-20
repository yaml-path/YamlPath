package io.github.yamlpath.processor.expressions;

import java.util.Arrays;
import java.util.Map;

import io.github.yamlpath.YamlExpressionParser;
import io.github.yamlpath.utils.StringUtils;

public class IsEqualExpressionProcessor implements ExpressionProcessor {

    private static final String IS_EQUAL = "==";

    @Override
    public String operator() {
        return IS_EQUAL;
    }

    @Override
    public boolean evaluate(String left, String right, Map<Object, Object> resource) {
        YamlExpressionParser parser = new YamlExpressionParser(Arrays.asList(resource));
        return StringUtils.equals(parser.readSingle(left), right);
    }
}
