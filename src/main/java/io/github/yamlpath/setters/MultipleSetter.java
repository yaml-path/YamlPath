package io.github.yamlpath.setters;

import java.util.LinkedList;
import java.util.List;

public class MultipleSetter implements Setter {
    private final List<Setter> innerSetters = new LinkedList<>();

    public void add(Setter setter) {
        innerSetters.add(setter);
    }

    @Override
    public void setValue(Object value) {
        innerSetters.forEach(s -> s.setValue(value));
    }
}
