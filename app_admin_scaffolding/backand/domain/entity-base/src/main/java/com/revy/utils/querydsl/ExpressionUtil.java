package com.revy.utils.querydsl;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.ComparablePath;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringExpression;
import com.revy.utils.querydsl.enums.LikeOperator;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import static com.revy.utils.querydsl.enums.LikeOperator.CONTAINS;

public final class ExpressionUtil {

    private ExpressionUtil() {

    }

    public static <T> BooleanExpression eq(SimpleExpression<T> path, T value) {
        return value == null ? null : path.eq(value);
    }

    public static <T> BooleanExpression like(StringExpression path, String value) {
        return like(path, value, CONTAINS);
    }

    public static <T> BooleanExpression like(StringExpression path, String value, LikeOperator likeOperator) {
        if (StringUtils.isBlank(value)) {
            return null;
        }

        switch (likeOperator) {
            case STARTS -> {
                return path.startsWith(value);
            }
            case ENDS -> {
                return path.endsWith(value);
            }
            case CONTAINS -> {
                return path.contains(value);
            }
            case DEFAULT -> {
                return path.eq(value);
            }
            default -> {
                throw new IllegalArgumentException("Unsupported LikeOperator: " + likeOperator);
            }
        }

    }

    public static <T> BooleanExpression in(SimpleExpression<T> path, T... values) {
        if (values == null) {
            return null;
        }
        return in(path, Arrays.asList(values));
    }

    public static <T> BooleanExpression in(SimpleExpression<T> path, Collection<? extends T> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        return path.in(values);
    }

    public static <T extends Comparable<?>> BooleanExpression from(ComparableExpression<T> path, T value) {
        if (value == null) {
            return null;
        }
        return path.goe(value);
    }

    public static <T extends Comparable<?>> BooleanExpression to(ComparableExpression<T> path, T value) {
        if (value == null) {
            return null;
        }
        return path.loe(value);
    }
}
