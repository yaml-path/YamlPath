package io.github.yamlpath.setters;

import java.util.Map;

public class MapAtKeySetter implements Setter {

    private final Map map;
    private final String key;

    public MapAtKeySetter(Map map, String key) {
        this.map = map;
        this.key = key;
    }

    @Override
    public void setValue(Object value) {
        map.put(key, value);
    }
}
