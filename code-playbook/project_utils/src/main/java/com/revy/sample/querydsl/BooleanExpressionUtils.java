package com.revy.sample.querydsl;

import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.core.types.dsl.DateTimeExpression;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringExpression;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Spring Boot 4.x + Querydsl 5.1.0 기준 BooleanExpression 유틸.
 *
 * 목적:
 * - null, blank, empty 조건은 where 절에서 제외
 * - Repository별 반복되는 동적 조건 제거
 * - LocalDate 입력으로 LocalDateTime 컬럼을 안전하게 범위 검색
 *
 * 전제:
 * - Querydsl where(...)는 null Predicate를 무시한다.
 * - null을 실제 IS NULL 조건으로 검색해야 하면 eqOrIsNull(...) 사용.
 */
public final class BooleanExpressionUtils {

    private BooleanExpressionUtils() {
    }

    /**
     * value가 null이면 조건 제외.
     */
    public static <T> BooleanExpression eq(SimpleExpression<T> path, T value) {
        return value == null ? null : path.eq(value);
    }

    /**
     * value가 null이면 조건 제외.
     */
    public static <T> BooleanExpression ne(SimpleExpression<T> path, T value) {
        return value == null ? null : path.ne(value);
    }

    /**
     * value가 null이면 IS NULL 조건.
     *
     * null 조건을 제외하고 싶으면 eq(...) 사용.
     */
    public static <T> BooleanExpression eqOrIsNull(SimpleExpression<T> path, T value) {
        return value == null ? path.isNull() : path.eq(value);
    }

    /**
     * value가 null이면 IS NOT NULL 조건.
     *
     * null 조건을 제외하고 싶으면 ne(...) 사용.
     */
    public static <T> BooleanExpression neOrIsNotNull(SimpleExpression<T> path, T value) {
        return value == null ? path.isNotNull() : path.ne(value);
    }

    /**
     * keyword가 null 또는 blank이면 조건 제외.
     *
     * SQL:
     * column like '%keyword%'
     */
    public static BooleanExpression like(StringExpression path, String keyword) {
        return isBlank(keyword) ? null : path.like(wrapLikeKeyword(keyword));
    }

    /**
     * keyword가 null 또는 blank이면 조건 제외.
     *
     * SQL:
     * lower(column) like '%keyword%'
     */
    public static BooleanExpression likeIgnoreCase(StringExpression path, String keyword) {
        return isBlank(keyword)
                ? null
                : path.lower().like(wrapLikeKeyword(keyword).toLowerCase());
    }

    /**
     * keyword가 null 또는 blank이면 조건 제외.
     *
     * Querydsl contains 사용.
     */
    public static BooleanExpression contains(StringExpression path, String keyword) {
        return isBlank(keyword) ? null : path.contains(keyword.trim());
    }

    /**
     * keyword가 null 또는 blank이면 조건 제외.
     */
    public static BooleanExpression containsIgnoreCase(StringExpression path, String keyword) {
        return isBlank(keyword) ? null : path.containsIgnoreCase(keyword.trim());
    }

    /**
     * keyword가 null 또는 blank이면 조건 제외.
     *
     * SQL:
     * column like 'keyword%'
     */
    public static BooleanExpression startsWith(StringExpression path, String keyword) {
        return isBlank(keyword) ? null : path.startsWith(keyword.trim());
    }

    /**
     * keyword가 null 또는 blank이면 조건 제외.
     */
    public static BooleanExpression startsWithIgnoreCase(StringExpression path, String keyword) {
        return isBlank(keyword) ? null : path.startsWithIgnoreCase(keyword.trim());
    }

    /**
     * keyword가 null 또는 blank이면 조건 제외.
     *
     * SQL:
     * column like '%keyword'
     */
    public static BooleanExpression endsWith(StringExpression path, String keyword) {
        return isBlank(keyword) ? null : path.endsWith(keyword.trim());
    }

    /**
     * keyword가 null 또는 blank이면 조건 제외.
     */
    public static BooleanExpression endsWithIgnoreCase(StringExpression path, String keyword) {
        return isBlank(keyword) ? null : path.endsWithIgnoreCase(keyword.trim());
    }

    /**
     * values가 null 또는 empty이면 조건 제외.
     * null 원소는 제거.
     */
    public static <T> BooleanExpression in(SimpleExpression<T> path, Collection<T> values) {
        List<T> filteredValues = filterNullValues(values);

        return filteredValues.isEmpty() ? null : path.in(filteredValues);
    }

    /**
     * values가 null 또는 empty이면 조건 제외.
     *
     * 사용 예:
     * in(user.status, ACTIVE, PENDING)
     */
    @SafeVarargs
    public static <T> BooleanExpression in(SimpleExpression<T> path, T... values) {
        if (values == null || values.length == 0) {
            return null;
        }

        return in(path, Arrays.asList(values));
    }

    /**
     * values가 null 또는 empty이면 조건 제외.
     * null 원소는 제거.
     */
    public static <T> BooleanExpression notIn(SimpleExpression<T> path, Collection<T> values) {
        List<T> filteredValues = filterNullValues(values);

        return filteredValues.isEmpty() ? null : path.notIn(filteredValues);
    }

    /**
     * values가 null 또는 empty이면 조건 제외.
     */
    @SafeVarargs
    public static <T> BooleanExpression notIn(SimpleExpression<T> path, T... values) {
        if (values == null || values.length == 0) {
            return null;
        }

        return notIn(path, Arrays.asList(values));
    }

    /**
     * value 이상.
     *
     * NumberPath, DatePath, DateTimePath, ComparablePath 계열에서 사용.
     */
    public static <T extends Comparable<?>> BooleanExpression goe(
            ComparableExpression<T> path,
            T value
    ) {
        return value == null ? null : path.goe(value);
    }

    /**
     * value 초과.
     */
    public static <T extends Comparable<?>> BooleanExpression gt(
            ComparableExpression<T> path,
            T value
    ) {
        return value == null ? null : path.gt(value);
    }

    /**
     * value 이하.
     */
    public static <T extends Comparable<?>> BooleanExpression loe(
            ComparableExpression<T> path,
            T value
    ) {
        return value == null ? null : path.loe(value);
    }

    /**
     * value 미만.
     */
    public static <T extends Comparable<?>> BooleanExpression lt(
            ComparableExpression<T> path,
            T value
    ) {
        return value == null ? null : path.lt(value);
    }

    /**
     * from, to 둘 다 있으면 between.
     * from만 있으면 goe.
     * to만 있으면 loe.
     */
    public static <T extends Comparable<?>> BooleanExpression between(
            ComparableExpression<T> path,
            T from,
            T to
    ) {
        if (from != null && to != null) {
            return path.between(from, to);
        }

        if (from != null) {
            return path.goe(from);
        }

        if (to != null) {
            return path.loe(to);
        }

        return null;
    }

    /**
     * LocalDate eq.
     *
     * QEntity.someDate가 DatePath<LocalDate>인 경우 사용.
     */
    public static BooleanExpression dateEq(
            DateExpression<LocalDate> path,
            LocalDate date
    ) {
        return date == null ? null : path.eq(date);
    }

    /**
     * LocalDate from ~ to.
     *
     * from, to 둘 다 있으면 between.
     * from만 있으면 goe.
     * to만 있으면 loe.
     */
    public static BooleanExpression dateBetween(
            DateExpression<LocalDate> path,
            LocalDate from,
            LocalDate to
    ) {
        if (from != null && to != null) {
            return path.between(from, to);
        }

        if (from != null) {
            return path.goe(from);
        }

        if (to != null) {
            return path.loe(to);
        }

        return null;
    }

    public static BooleanExpression dateGoe(
            DateExpression<LocalDate> path,
            LocalDate date
    ) {
        return date == null ? null : path.goe(date);
    }

    public static BooleanExpression dateGt(
            DateExpression<LocalDate> path,
            LocalDate date
    ) {
        return date == null ? null : path.gt(date);
    }

    public static BooleanExpression dateLoe(
            DateExpression<LocalDate> path,
            LocalDate date
    ) {
        return date == null ? null : path.loe(date);
    }

    public static BooleanExpression dateLt(
            DateExpression<LocalDate> path,
            LocalDate date
    ) {
        return date == null ? null : path.lt(date);
    }

    /**
     * LocalDateTime eq.
     *
     * 정확히 같은 timestamp 검색이 필요한 경우에만 사용.
     * 일반적인 날짜 검색은 dateEqualsDateTimeRange(...) 또는 dateRangeToDateTimeRange(...) 권장.
     */
    public static BooleanExpression dateTimeEq(
            DateTimeExpression<LocalDateTime> path,
            LocalDateTime dateTime
    ) {
        return dateTime == null ? null : path.eq(dateTime);
    }

    /**
     * LocalDateTime from ~ to.
     *
     * from, to 둘 다 있으면 between.
     * from만 있으면 goe.
     * to만 있으면 loe.
     */
    public static BooleanExpression dateTimeBetween(
            DateTimeExpression<LocalDateTime> path,
            LocalDateTime from,
            LocalDateTime to
    ) {
        if (from != null && to != null) {
            return path.between(from, to);
        }

        if (from != null) {
            return path.goe(from);
        }

        if (to != null) {
            return path.loe(to);
        }

        return null;
    }

    public static BooleanExpression dateTimeGoe(
            DateTimeExpression<LocalDateTime> path,
            LocalDateTime dateTime
    ) {
        return dateTime == null ? null : path.goe(dateTime);
    }

    public static BooleanExpression dateTimeGt(
            DateTimeExpression<LocalDateTime> path,
            LocalDateTime dateTime
    ) {
        return dateTime == null ? null : path.gt(dateTime);
    }

    public static BooleanExpression dateTimeLoe(
            DateTimeExpression<LocalDateTime> path,
            LocalDateTime dateTime
    ) {
        return dateTime == null ? null : path.loe(dateTime);
    }

    public static BooleanExpression dateTimeLt(
            DateTimeExpression<LocalDateTime> path,
            LocalDateTime dateTime
    ) {
        return dateTime == null ? null : path.lt(dateTime);
    }

    /**
     * LocalDate 기준으로 LocalDateTime 컬럼의 하루 범위 검색.
     *
     * date = 2026-05-12 이면:
     * createdAt >= 2026-05-12T00:00:00
     * createdAt <  2026-05-13T00:00:00
     *
     * between 대신 반개구간 [start, end) 사용.
     */
    public static BooleanExpression dateEqualsDateTimeRange(
            DateTimeExpression<LocalDateTime> path,
            LocalDate date
    ) {
        if (date == null) {
            return null;
        }

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        return path.goe(start).and(path.lt(end));
    }

    /**
     * LocalDate from ~ to 조건으로 LocalDateTime 컬럼 검색.
     *
     * from = 2026-05-01
     * to   = 2026-05-12
     *
     * createdAt >= 2026-05-01T00:00:00
     * createdAt <  2026-05-13T00:00:00
     *
     * between 대신 반개구간 [start, end) 사용.
     */
    public static BooleanExpression dateRangeToDateTimeRange(
            DateTimeExpression<LocalDateTime> path,
            LocalDate from,
            LocalDate to
    ) {
        if (from == null && to == null) {
            return null;
        }

        if (from != null && to != null) {
            return path.goe(from.atStartOfDay())
                    .and(path.lt(to.plusDays(1).atStartOfDay()));
        }

        if (from != null) {
            return path.goe(from.atStartOfDay());
        }

        return path.lt(to.plusDays(1).atStartOfDay());
    }

    /**
     * 여러 BooleanExpression을 null-safe AND 조합.
     *
     * 모든 조건이 null이면 null 반환.
     */
    public static BooleanExpression and(BooleanExpression... expressions) {
        if (expressions == null || expressions.length == 0) {
            return null;
        }

        BooleanExpression result = null;

        for (BooleanExpression expression : expressions) {
            if (expression == null) {
                continue;
            }

            result = result == null ? expression : result.and(expression);
        }

        return result;
    }

    /**
     * 여러 BooleanExpression을 null-safe OR 조합.
     *
     * 모든 조건이 null이면 null 반환.
     */
    public static BooleanExpression or(BooleanExpression... expressions) {
        if (expressions == null || expressions.length == 0) {
            return null;
        }

        BooleanExpression result = null;

        for (BooleanExpression expression : expressions) {
            if (expression == null) {
                continue;
            }

            result = result == null ? expression : result.or(expression);
        }

        return result;
    }

    /**
     * Predicate 배열을 null-safe AND 조합.
     *
     * BooleanExpression 외 Predicate까지 받을 수 있게 한 버전.
     */
    public static Predicate allOf(Predicate... predicates) {
        if (predicates == null || predicates.length == 0) {
            return null;
        }

        return ExpressionUtils.allOf(Arrays.asList(predicates));
    }

    /**
     * Predicate 배열을 null-safe OR 조합.
     *
     * BooleanExpression 외 Predicate까지 받을 수 있게 한 버전.
     */
    public static Predicate anyOf(Predicate... predicates) {
        if (predicates == null || predicates.length == 0) {
            return null;
        }

        return ExpressionUtils.anyOf(Arrays.asList(predicates));
    }

    private static String wrapLikeKeyword(String keyword) {
        return "%" + keyword.trim() + "%";
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static <T> List<T> filterNullValues(Collection<T> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }

        return values.stream()
                .filter(Objects::nonNull)
                .toList();
    }
}