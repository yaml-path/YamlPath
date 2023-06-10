package io.github.yamlpath.setters;

import java.util.Collection;
import java.util.List;

public class ListSetter implements Setter {

    private final List list;

    public ListSetter(List list) {
        this.list = list;
    }

    @Override
    public void setValue(Object value) {
        if (value instanceof Collection) {
            // append values
            list.clear();
            list.addAll((Collection) value);
        } else {
            // add value to item
            list.clear();
            list.add(value);
        }
    }
}
