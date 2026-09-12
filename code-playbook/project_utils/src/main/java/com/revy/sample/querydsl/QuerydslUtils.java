package com.revy.sample.querydsl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringExpression;

public final class QuerydslUtils {

    private QuerydslUtils() {
    }

    public static <T> BooleanExpression eq(SimpleExpression<T> path, T value) {
        if (value == null) {
            return null;
        }
        return path.eq(value);
    }

    public static <T> BooleanExpression ne(SimpleExpression<T> path, T value) {
        if (value == null) {
            return null;
        }
        return path.ne(value);
    }

    public static <T> BooleanExpression noteq(SimpleExpression<T> path, T value) {
        return ne(path, value);
    }

    public static BooleanExpression like(StringExpression path, String value) {
        if (!hasText(value)) {
            return null;
        }
        return path.like("%" + escapeLike(value) + "%");
    }

    public static BooleanExpression contains(StringExpression path, String value) {
        if (!hasText(value)) {
            return null;
        }
        return path.contains(value);
    }

    public static BooleanExpression containsIgnoreCase(StringExpression path, String value) {
        if (!hasText(value)) {
            return null;
        }
        return path.containsIgnoreCase(value);
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static String escapeLike(String value) {
        return value
            .replace("\\", "\\\\")
            .replace("%", "\\%")
            .replace("_", "\\_");
    }
}