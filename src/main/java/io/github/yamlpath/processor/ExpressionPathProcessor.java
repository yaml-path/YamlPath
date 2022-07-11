package io.github.yamlpath.processor;

import static io.github.yamlpath.utils.PathUtils.normalize;

import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import io.github.yamlpath.WorkUnit;
import io.github.yamlpath.processor.expressions.ExpressionProcessor;

public class ExpressionPathProcessor implements PathProcessor {

    private static final String AND = "&&";
    private static final String OR = "||";

    private final List<ExpressionProcessor> supportedExpressions;

    public ExpressionPathProcessor() {
        this.supportedExpressions = StreamSupport
                .stream(ServiceLoader.load(ExpressionProcessor.class, ExpressionPathProcessor.class.getClassLoader())
                        .spliterator(), false)
                .sorted(Comparator.comparingInt(ExpressionProcessor::getPriority)).collect(Collectors.toList());
    }

    @Override
    public boolean canHandle(WorkUnit.Path path) {
        return supportedExpressions.stream().anyMatch(e -> path.getPart().contains(e.operator()));
    }

    @Override
    public Object handle(WorkUnit workUnit, WorkUnit.Path path) {
        boolean applies;
        String expression = path.getPart();
        if (expression.contains(AND)) {
            String[] subExpressions = expression.split(Pattern.quote(AND));
            applies = Stream.of(subExpressions).allMatch(e -> evaluate(path, e));
        } else if (expression.contains(OR)) {
            String[] subExpressions = expression.split(Pattern.quote(OR));
            applies = Stream.of(subExpressions).anyMatch(e -> evaluate(path, e));
        } else {
            applies = evaluate(path, expression);
        }

        return applies ? path.getTree() : null;
    }

    private boolean evaluate(WorkUnit.Path path, String expression) {
        for (ExpressionProcessor supported : supportedExpressions) {
            if (expression.contains(supported.operator())) {
                String[] parts = expression.split(supported.operator());
                String left = normalize(parts[0]);
                String right = normalize(parts[1]);
                return supported.evaluate(left, right, path.getTree());
            }
        }

        return false;
    }
}
