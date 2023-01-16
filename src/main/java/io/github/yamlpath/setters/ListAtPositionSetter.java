package io.github.yamlpath.setters;

import java.util.List;
import java.util.Map;

public class ListAtPositionSetter implements Setter {

    private final List list;
    private final int position;

    public ListAtPositionSetter(List list, int position) {
        this.list = list;
        this.position = position;
    }

    @Override
    public void setValue(Object value) {
        Object item = list.get(position);
        if (item instanceof List) {
            ListSetter listSetter = new ListSetter((List) item);
            listSetter.setValue(value);
        } else if (item instanceof Map) {
            if (value instanceof Map) {
                // append values to map
                ((Map) item).putAll((Map) value);
            } else {
                list.set(position, value);
            }
        } else {
            list.set(position, value);
        }
    }
}
