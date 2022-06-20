package io.github.yamlpath.utils;

public final class StringUtils {

    public static final String EMPTY = "";

    private StringUtils() {

    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static boolean equals(Object left, Object right) {
        if (left == null && right == null) {
            return true;
        } else if (left == null) {
            return false;
        } else if (right == null) {
            return false;
        } else if (left instanceof String && right instanceof String) {
            return left.equals(right);
        }

        return false;
    }
}
