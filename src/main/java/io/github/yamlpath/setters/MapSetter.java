package io.github.yamlpath.setters;

import java.util.Map;

public class MapSetter implements Setter {

    private final Map map;
    private final Setter fallback;

    public MapSetter(Map map, Setter fallback) {
        this.map = map;
        this.fallback = fallback;
    }

    @Override
    public void setValue(Object value) {
        if (value instanceof Map) {
            // append values
            map.putAll((Map) value);
        } else {
            fallback.setValue(value);
        }
    }
}
