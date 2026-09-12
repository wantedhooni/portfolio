package com.revy.example.querydsl.utils

import com.querydsl.core.types.ExpressionUtils
import com.querydsl.core.types.Predicate
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.ComparableExpression
import com.querydsl.core.types.dsl.DateExpression
import com.querydsl.core.types.dsl.DateTimeExpression
import com.querydsl.core.types.dsl.SimpleExpression
import com.querydsl.core.types.dsl.StringExpression
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

object BooleanExpressionUtils {
    /**
     * value가 null이면 조건 제외.
     */
    fun <T> eq(path: SimpleExpression<T?>, value: T?): BooleanExpression? {
        return if (value == null) null else path.eq(value)
    }

    /**
     * value가 null이면 조건 제외.
     */
    fun <T> ne(path: SimpleExpression<T?>, value: T?): BooleanExpression? {
        return if (value == null) null else path.ne(value)
    }

    /**
     * value가 null이면 IS NULL 조건.
     *
     * null 조건을 제외하고 싶으면 eq(...) 사용.
     */
    fun <T> eqOrIsNull(path: SimpleExpression<T?>, value: T?): BooleanExpression? {
        return if (value == null) path.isNull() else path.eq(value)
    }

    /**
     * value가 null이면 IS NOT NULL 조건.
     *
     * null 조건을 제외하고 싶으면 ne(...) 사용.
     */
    fun <T> neOrIsNotNull(path: SimpleExpression<T?>, value: T?): BooleanExpression? {
        return if (value == null) path.isNotNull() else path.ne(value)
    }

    /**
     * keyword가 null 또는 blank이면 조건 제외.
     *
     * SQL:
     * column like '%keyword%'
     */
    fun like(path: StringExpression, keyword: String?): BooleanExpression? {
        return if (isBlank(keyword)) null else path.like(wrapLikeKeyword(keyword!!))
    }

    /**
     * keyword가 null 또는 blank이면 조건 제외.
     *
     * SQL:
     * lower(column) like '%keyword%'
     */
    fun likeIgnoreCase(path: StringExpression, keyword: String?): BooleanExpression? {
        return if (isBlank(keyword))
            null
        else
            path.lower().like(wrapLikeKeyword(keyword!!).lowercase(Locale.getDefault()))
    }

    /**
     * keyword가 null 또는 blank이면 조건 제외.
     *
     * Querydsl contains 사용.
     */
    fun contains(path: StringExpression, keyword: String?): BooleanExpression? {
        return if (isBlank(keyword)) null else path.contains(keyword!!.trim { it <= ' ' })
    }

    /**
     * keyword가 null 또는 blank이면 조건 제외.
     */
    fun containsIgnoreCase(path: StringExpression, keyword: String?): BooleanExpression? {
        return if (isBlank(keyword)) null else path.containsIgnoreCase(keyword!!.trim { it <= ' ' })
    }

    /**
     * keyword가 null 또는 blank이면 조건 제외.
     *
     * SQL:
     * column like 'keyword%'
     */
    fun startsWith(path: StringExpression, keyword: String?): BooleanExpression? {
        return if (isBlank(keyword)) null else path.startsWith(keyword!!.trim { it <= ' ' })
    }

    /**
     * keyword가 null 또는 blank이면 조건 제외.
     */
    fun startsWithIgnoreCase(path: StringExpression, keyword: String?): BooleanExpression? {
        return if (isBlank(keyword)) null else path.startsWithIgnoreCase(keyword!!.trim { it <= ' ' })
    }

    /**
     * keyword가 null 또는 blank이면 조건 제외.
     *
     * SQL:
     * column like '%keyword'
     */
    fun endsWith(path: StringExpression, keyword: String?): BooleanExpression? {
        return if (isBlank(keyword)) null else path.endsWith(keyword!!.trim { it <= ' ' })
    }

    /**
     * keyword가 null 또는 blank이면 조건 제외.
     */
    fun endsWithIgnoreCase(path: StringExpression, keyword: String?): BooleanExpression? {
        return if (isBlank(keyword)) null else path.endsWithIgnoreCase(keyword!!.trim { it <= ' ' })
    }

    /**
     * values가 null 또는 empty이면 조건 제외.
     * null 원소는 제거.
     */
    fun <T> `in`(path: SimpleExpression<T?>, values: MutableCollection<T?>?): BooleanExpression? {
        val filteredValues = filterNullValues<T?>(values)

        return if (filteredValues.isEmpty()) null else path.`in`(filteredValues)
    }

    /**
     * values가 null 또는 empty이면 조건 제외.
     *
     * 사용 예:
     * in(user.status, ACTIVE, PENDING)
     */
    @SafeVarargs
    fun <T> `in`(path: SimpleExpression<T?>, vararg values: T?): BooleanExpression? {
        if (values == null || values.size == 0) {
            return null
        }

        return `in`<T?>(path, Arrays.asList<T?>(*values))
    }

    /**
     * values가 null 또는 empty이면 조건 제외.
     * null 원소는 제거.
     */
    fun <T> notIn(path: SimpleExpression<T?>, values: MutableCollection<T?>?): BooleanExpression? {
        val filteredValues = filterNullValues<T?>(values)

        return if (filteredValues.isEmpty()) null else path.notIn(filteredValues)
    }

    /**
     * values가 null 또는 empty이면 조건 제외.
     */
    @SafeVarargs
    fun <T> notIn(path: SimpleExpression<T?>, vararg values: T?): BooleanExpression? {
        if (values == null || values.size == 0) {
            return null
        }

        return notIn<T?>(path, Arrays.asList<T?>(*values))
    }

    /**
     * value 이상.
     *
     * NumberPath, DatePath, DateTimePath, ComparablePath 계열에서 사용.
     */
    fun <T : Comparable<*>?> goe(
        path: ComparableExpression<T?>,
        value: T?
    ): BooleanExpression? {
        return if (value == null) null else path.goe(value)
    }

    /**
     * value 초과.
     */
    fun <T : Comparable<*>?> gt(
        path: ComparableExpression<T?>,
        value: T?
    ): BooleanExpression? {
        return if (value == null) null else path.gt(value)
    }

    /**
     * value 이하.
     */
    fun <T : Comparable<*>?> loe(
        path: ComparableExpression<T?>,
        value: T?
    ): BooleanExpression? {
        return if (value == null) null else path.loe(value)
    }

    /**
     * value 미만.
     */
    fun <T : Comparable<*>?> lt(
        path: ComparableExpression<T?>,
        value: T?
    ): BooleanExpression? {
        return if (value == null) null else path.lt(value)
    }

    /**
     * from, to 둘 다 있으면 between.
     * from만 있으면 goe.
     * to만 있으면 loe.
     */
    fun <T : Comparable<*>?> between(
        path: ComparableExpression<T?>,
        from: T?,
        to: T?
    ): BooleanExpression? {
        if (from != null && to != null) {
            return path.between(from, to)
        }

        if (from != null) {
            return path.goe(from)
        }

        if (to != null) {
            return path.loe(to)
        }

        return null
    }

    /**
     * LocalDate eq.
     *
     * QEntity.someDate가 DatePath<LocalDate>인 경우 사용.
    </LocalDate> */
    fun dateEq(
        path: DateExpression<LocalDate?>,
        date: LocalDate?
    ): BooleanExpression? {
        return if (date == null) null else path.eq(date)
    }

    /**
     * LocalDate from ~ to.
     *
     * from, to 둘 다 있으면 between.
     * from만 있으면 goe.
     * to만 있으면 loe.
     */
    fun dateBetween(
        path: DateExpression<LocalDate?>,
        from: LocalDate?,
        to: LocalDate?
    ): BooleanExpression? {
        if (from != null && to != null) {
            return path.between(from, to)
        }

        if (from != null) {
            return path.goe(from)
        }

        if (to != null) {
            return path.loe(to)
        }

        return null
    }

    fun dateGoe(
        path: DateExpression<LocalDate?>,
        date: LocalDate?
    ): BooleanExpression? {
        return if (date == null) null else path.goe(date)
    }

    fun dateGt(
        path: DateExpression<LocalDate?>,
        date: LocalDate?
    ): BooleanExpression? {
        return if (date == null) null else path.gt(date)
    }

    fun dateLoe(
        path: DateExpression<LocalDate?>,
        date: LocalDate?
    ): BooleanExpression? {
        return if (date == null) null else path.loe(date)
    }

    fun dateLt(
        path: DateExpression<LocalDate?>,
        date: LocalDate?
    ): BooleanExpression? {
        return if (date == null) null else path.lt(date)
    }

    /**
     * LocalDateTime eq.
     *
     * 정확히 같은 timestamp 검색이 필요한 경우에만 사용.
     * 일반적인 날짜 검색은 dateEqualsDateTimeRange(...) 또는 dateRangeToDateTimeRange(...) 권장.
     */
    fun dateTimeEq(
        path: DateTimeExpression<LocalDateTime?>,
        dateTime: LocalDateTime?
    ): BooleanExpression? {
        return if (dateTime == null) null else path.eq(dateTime)
    }

    /**
     * LocalDateTime from ~ to.
     *
     * from, to 둘 다 있으면 between.
     * from만 있으면 goe.
     * to만 있으면 loe.
     */
    fun dateTimeBetween(
        path: DateTimeExpression<LocalDateTime?>,
        from: LocalDateTime?,
        to: LocalDateTime?
    ): BooleanExpression? {
        if (from != null && to != null) {
            return path.between(from, to)
        }

        if (from != null) {
            return path.goe(from)
        }

        if (to != null) {
            return path.loe(to)
        }

        return null
    }

    fun dateTimeGoe(
        path: DateTimeExpression<LocalDateTime?>,
        dateTime: LocalDateTime?
    ): BooleanExpression? {
        return if (dateTime == null) null else path.goe(dateTime)
    }

    fun dateTimeGt(
        path: DateTimeExpression<LocalDateTime?>,
        dateTime: LocalDateTime?
    ): BooleanExpression? {
        return if (dateTime == null) null else path.gt(dateTime)
    }

    fun dateTimeLoe(
        path: DateTimeExpression<LocalDateTime?>,
        dateTime: LocalDateTime?
    ): BooleanExpression? {
        return if (dateTime == null) null else path.loe(dateTime)
    }

    fun dateTimeLt(
        path: DateTimeExpression<LocalDateTime?>,
        dateTime: LocalDateTime?
    ): BooleanExpression? {
        return if (dateTime == null) null else path.lt(dateTime)
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
    fun dateEqualsDateTimeRange(
        path: DateTimeExpression<LocalDateTime?>,
        date: LocalDate?
    ): BooleanExpression? {
        if (date == null) {
            return null
        }

        val start = date.atStartOfDay()
        val end = date.plusDays(1).atStartOfDay()

        return path.goe(start).and(path.lt(end))
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
    fun dateRangeToDateTimeRange(
        path: DateTimeExpression<LocalDateTime?>,
        from: LocalDate?,
        to: LocalDate?
    ): BooleanExpression? {
        if (from == null && to == null) {
            return null
        }

        if (from != null && to != null) {
            return path.goe(from.atStartOfDay())
                .and(path.lt(to.plusDays(1).atStartOfDay()))
        }

        if (from != null) {
            return path.goe(from.atStartOfDay())
        }

        return path.lt(to!!.plusDays(1).atStartOfDay())
    }

    /**
     * 여러 BooleanExpression을 null-safe AND 조합.
     *
     * 모든 조건이 null이면 null 반환.
     */
    fun and(vararg expressions: BooleanExpression?): BooleanExpression? {
        if (expressions == null || expressions.size == 0) {
            return null
        }

        var result: BooleanExpression? = null

        for (expression in expressions) {
            if (expression == null) {
                continue
            }

            result = if (result == null) expression else result.and(expression)
        }

        return result
    }

    /**
     * 여러 BooleanExpression을 null-safe OR 조합.
     *
     * 모든 조건이 null이면 null 반환.
     */
    fun or(vararg expressions: BooleanExpression?): BooleanExpression? {
        if (expressions == null || expressions.size == 0) {
            return null
        }

        var result: BooleanExpression? = null

        for (expression in expressions) {
            if (expression == null) {
                continue
            }

            result = if (result == null) expression else result.or(expression)
        }

        return result
    }

    /**
     * Predicate 배열을 null-safe AND 조합.
     *
     * BooleanExpression 외 Predicate까지 받을 수 있게 한 버전.
     */
    fun allOf(vararg predicates: Predicate?): Predicate? {
        if (predicates == null || predicates.size == 0) {
            return null
        }

        return ExpressionUtils.allOf(Arrays.asList<Predicate?>(*predicates))
    }

    /**
     * Predicate 배열을 null-safe OR 조합.
     *
     * BooleanExpression 외 Predicate까지 받을 수 있게 한 버전.
     */
    fun anyOf(vararg predicates: Predicate?): Predicate? {
        if (predicates == null || predicates.size == 0) {
            return null
        }

        return ExpressionUtils.anyOf(Arrays.asList<Predicate?>(*predicates))
    }

    private fun wrapLikeKeyword(keyword: String): String {
        return "%" + keyword.trim { it <= ' ' } + "%"
    }

    private fun isBlank(value: String?): Boolean {
        return value == null || value.trim { it <= ' ' }.isEmpty()
    }

    private fun <T> filterNullValues(values: MutableCollection<T?>?): MutableList<T?> {
        if (values == null || values.isEmpty()) {
            return mutableListOf<T?>()
        }

        return values.stream()
            .filter { obj: T? -> Objects.nonNull(obj) }
            .toList()
    }
}