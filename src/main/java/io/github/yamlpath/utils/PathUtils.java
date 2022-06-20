package io.github.yamlpath.utils;

public final class PathUtils {

    public static final String NO_REPLACEMENT = "no";
    public static final String WILDCARD = "*";
    public static final String ESCAPE = "'";
    public static final String DOT = ".";
    public static final String PARENTHESIS_OPEN = "(";
    public static final String PARENTHESIS_CLOSE = ")";

    private PathUtils() {

    }

    public static String normalize(String text) {
        if (StringUtils.isNullOrEmpty(text)) {
            return text;
        }

        String normalized = text.trim();
        if (normalized.startsWith(ESCAPE)) {
            normalized = normalized.substring(1, normalized.length() - 1);
        }

        return normalized;
    }
}
