package io.github.yamlpath.utils;

import java.util.Set;

public final class SetUtils {
    private SetUtils() {

    }

    public static <T> T uniqueResult(Set<T> set) {
        if (set.size() > 1) {
            throw new IllegalStateException("Several results found: " + set);
        } else if (set.isEmpty()) {
            return null;
        }

        return set.iterator().next();
    }
}
