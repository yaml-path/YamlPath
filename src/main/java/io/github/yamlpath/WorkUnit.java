package io.github.yamlpath;

import static io.github.yamlpath.utils.PathUtils.DOT;
import static io.github.yamlpath.utils.PathUtils.ESCAPE;
import static io.github.yamlpath.utils.PathUtils.NO_REPLACEMENT;
import static io.github.yamlpath.utils.PathUtils.PARENTHESIS_CLOSE;
import static io.github.yamlpath.utils.PathUtils.PARENTHESIS_OPEN;
import static io.github.yamlpath.utils.PathUtils.normalize;

import java.util.Map;

import io.github.yamlpath.setters.LastNodeSetter;
import io.github.yamlpath.setters.Setter;
import io.github.yamlpath.utils.StringUtils;

public class WorkUnit {
    private String expression;
    private Map<Object, Object> node;
    private Path lastVisited;
    private Object result;
    private Setter setter = new LastNodeSetter(this);

    public WorkUnit(Map<Object, Object> node, String expression) {
        this.node = node;
        this.expression = expression;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public boolean hasNextPath() {
        return !StringUtils.isNullOrEmpty(expression);
    }

    public Path getLastVisited() {
        return lastVisited;
    }

    public void setSetter(Setter setter) {
        this.setter = setter;
    }

    public Path nextPath() {
        // if we have several dots, for example: "..spec.rest", we move the pointer to "spec.rest"
        while (expression.indexOf(DOT) == 0) {
            expression = expression.substring(1);
        }

        int nextIndex = -1;
        String part;
        if (expression.startsWith(ESCAPE)) {
            String nextPart = expression.substring(1);
            nextIndex = nextPart.indexOf(ESCAPE) + 1;
            part = nextPart.substring(0, nextIndex - 1);
        } else if (expression.startsWith(PARENTHESIS_OPEN)) {
            String nextPart = expression.substring(1);
            nextIndex = nextPart.indexOf(PARENTHESIS_CLOSE) + 1;
            part = nextPart.substring(0, nextIndex - 1);
        } else {
            nextIndex = expression.indexOf(DOT);
            part = normalize(nextIndex > 0 ? expression.substring(0, nextIndex) : expression);
        }

        if (nextIndex > 0) {
            expression = expression.substring(nextIndex + 1);
        } else {
            expression = StringUtils.EMPTY;
        }

        lastVisited = new Path(part, node);
        return lastVisited;
    }

    public WorkUnit clone() {
        WorkUnit workUnit = new WorkUnit(this.node, this.expression);
        workUnit.lastVisited = this.lastVisited;
        workUnit.result = this.result;
        return workUnit;
    }

    protected void setNode(Map<Object, Object> node) {
        this.node = node;
    }

    protected void replaceResourceWith(Object replacement) {
        if (!NO_REPLACEMENT.equals(replacement)) {
            setter.setValue(replacement);
        }
    }

    public static class Path {
        private final String part;
        private final Map<Object, Object> tree;

        public Path(String part, Map<Object, Object> tree) {
            this.part = part;
            this.tree = tree;
        }

        public String getPart() {
            return part;
        }

        public Map<Object, Object> getTree() {
            return tree;
        }
    }
}
