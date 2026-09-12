package com.revy.example.utils

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

object DateTimeUtils {

    val DATE_FORMATTER: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd")

    val DATE_TIME_FORMATTER: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    val COMPACT_DATE_FORMATTER: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyyMMdd")

    val COMPACT_DATE_TIME_FORMATTER: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyyMMddHHmmss")

    // ---------------------------------------------------------------------
    // Now / Today
    // ---------------------------------------------------------------------

    fun today(): LocalDate {
        return LocalDate.now()
    }

    fun today(zoneId: ZoneId): LocalDate {
        return LocalDate.now(zoneId)
    }

    fun now(): LocalDateTime {
        return LocalDateTime.now()
    }

    fun now(zoneId: ZoneId): LocalDateTime {
        return LocalDateTime.now(zoneId)
    }

    // ---------------------------------------------------------------------
    // Format
    // ---------------------------------------------------------------------

    @JvmOverloads
    fun format(
        date: LocalDate?,
        formatter: DateTimeFormatter = DATE_FORMATTER
    ): String? {
        if (date == null) {
            return null
        }

        return date.format(formatter)
    }

    @JvmOverloads
    fun format(
        dateTime: LocalDateTime?,
        formatter: DateTimeFormatter = DATE_TIME_FORMATTER
    ): String? {
        if (dateTime == null) {
            return null
        }

        return dateTime.format(formatter)
    }

    // ---------------------------------------------------------------------
    // Parse
    // ---------------------------------------------------------------------

    @JvmOverloads
    fun parseDate(
        value: String?,
        formatter: DateTimeFormatter = DATE_FORMATTER
    ): LocalDate? {
        if (isBlank(value)) {
            return null
        }

        return try {
            LocalDate.parse(value, formatter)
        } catch (e: DateTimeParseException) {
            throw IllegalArgumentException("Invalid LocalDate value: $value", e)
        }
    }

    @JvmOverloads
    fun parseDateTime(
        value: String?,
        formatter: DateTimeFormatter = DATE_TIME_FORMATTER
    ): LocalDateTime? {
        if (isBlank(value)) {
            return null
        }

        return try {
            LocalDateTime.parse(value, formatter)
        } catch (e: DateTimeParseException) {
            throw IllegalArgumentException("Invalid LocalDateTime value: $value", e)
        }
    }

    // ---------------------------------------------------------------------
    // Start / End
    // ---------------------------------------------------------------------

    fun startOfDay(date: LocalDate?): LocalDateTime? {
        if (date == null) {
            return null
        }

        return date.atStartOfDay()
    }

    fun endOfDay(date: LocalDate?): LocalDateTime? {
        if (date == null) {
            return null
        }

        return date.atTime(LocalTime.MAX)
    }

    fun firstDayOfMonth(date: LocalDate?): LocalDate? {
        if (date == null) {
            return null
        }

        return date.withDayOfMonth(1)
    }

    fun lastDayOfMonth(date: LocalDate?): LocalDate? {
        if (date == null) {
            return null
        }

        return date.withDayOfMonth(date.lengthOfMonth())
    }

    fun firstDayOfYear(date: LocalDate?): LocalDate? {
        if (date == null) {
            return null
        }

        return date.withDayOfYear(1)
    }

    fun lastDayOfYear(date: LocalDate?): LocalDate? {
        if (date == null) {
            return null
        }

        return date.withDayOfYear(date.lengthOfYear())
    }

    // ---------------------------------------------------------------------
    // Compare - LocalDate
    // ---------------------------------------------------------------------

    fun isBefore(source: LocalDate?, target: LocalDate?): Boolean {
        return source != null &&
                target != null &&
                source.isBefore(target)
    }

    fun isAfter(source: LocalDate?, target: LocalDate?): Boolean {
        return source != null &&
                target != null &&
                source.isAfter(target)
    }

    fun isEqual(source: LocalDate?, target: LocalDate?): Boolean {
        return source != null &&
                target != null &&
                source.isEqual(target)
    }

    fun isBetween(
        target: LocalDate?,
        startInclusive: LocalDate?,
        endInclusive: LocalDate?
    ): Boolean {
        if (target == null || startInclusive == null || endInclusive == null) {
            return false
        }

        return !target.isBefore(startInclusive) &&
                !target.isAfter(endInclusive)
    }

    // ---------------------------------------------------------------------
    // Compare - LocalDateTime
    // ---------------------------------------------------------------------

    fun isBefore(source: LocalDateTime?, target: LocalDateTime?): Boolean {
        return source != null &&
                target != null &&
                source.isBefore(target)
    }

    fun isAfter(source: LocalDateTime?, target: LocalDateTime?): Boolean {
        return source != null &&
                target != null &&
                source.isAfter(target)
    }

    fun isEqual(source: LocalDateTime?, target: LocalDateTime?): Boolean {
        return source != null &&
                target != null &&
                source.isEqual(target)
    }

    fun isBetween(
        target: LocalDateTime?,
        startInclusive: LocalDateTime?,
        endInclusive: LocalDateTime?
    ): Boolean {
        if (target == null || startInclusive == null || endInclusive == null) {
            return false
        }

        return !target.isBefore(startInclusive) &&
                !target.isAfter(endInclusive)
    }

    // ---------------------------------------------------------------------
    // Difference - LocalDate
    // ---------------------------------------------------------------------

    fun daysBetween(
        startInclusive: LocalDate,
        endExclusive: LocalDate
    ): Long {
        return ChronoUnit.DAYS.between(startInclusive, endExclusive)
    }

    fun monthsBetween(
        startInclusive: LocalDate,
        endExclusive: LocalDate
    ): Long {
        return ChronoUnit.MONTHS.between(startInclusive, endExclusive)
    }

    fun yearsBetween(
        startInclusive: LocalDate,
        endExclusive: LocalDate
    ): Long {
        return ChronoUnit.YEARS.between(startInclusive, endExclusive)
    }

    // ---------------------------------------------------------------------
    // Difference - LocalDateTime
    // ---------------------------------------------------------------------

    fun secondsBetween(
        startInclusive: LocalDateTime,
        endExclusive: LocalDateTime
    ): Long {
        return ChronoUnit.SECONDS.between(startInclusive, endExclusive)
    }

    fun minutesBetween(
        startInclusive: LocalDateTime,
        endExclusive: LocalDateTime
    ): Long {
        return ChronoUnit.MINUTES.between(startInclusive, endExclusive)
    }

    fun hoursBetween(
        startInclusive: LocalDateTime,
        endExclusive: LocalDateTime
    ): Long {
        return ChronoUnit.HOURS.between(startInclusive, endExclusive)
    }

    // ---------------------------------------------------------------------
    // Convert
    // ---------------------------------------------------------------------

    fun toLocalDate(dateTime: LocalDateTime?): LocalDate? {
        if (dateTime == null) {
            return null
        }

        return dateTime.toLocalDate()
    }

    fun toLocalDateTime(date: LocalDate?): LocalDateTime? {
        if (date == null) {
            return null
        }

        return date.atStartOfDay()
    }

    fun toInstant(
        dateTime: LocalDateTime?,
        zoneId: ZoneId
    ): Instant? {
        if (dateTime == null) {
            return null
        }

        return dateTime
            .atZone(zoneId)
            .toInstant()
    }

    fun toLocalDateTime(
        instant: Instant?,
        zoneId: ZoneId
    ): LocalDateTime? {
        if (instant == null) {
            return null
        }

        return LocalDateTime.ofInstant(instant, zoneId)
    }

    fun toLocalDate(
        instant: Instant?,
        zoneId: ZoneId
    ): LocalDate? {
        if (instant == null) {
            return null
        }

        return LocalDate.ofInstant(instant, zoneId)
    }

    // ---------------------------------------------------------------------
    // Add / Minus
    // ---------------------------------------------------------------------

    fun plusDays(date: LocalDate?, days: Long): LocalDate? {
        if (date == null) {
            return null
        }

        return date.plusDays(days)
    }

    fun minusDays(date: LocalDate?, days: Long): LocalDate? {
        if (date == null) {
            return null
        }

        return date.minusDays(days)
    }

    fun plusHours(dateTime: LocalDateTime?, hours: Long): LocalDateTime? {
        if (dateTime == null) {
            return null
        }

        return dateTime.plusHours(hours)
    }

    fun minusHours(dateTime: LocalDateTime?, hours: Long): LocalDateTime? {
        if (dateTime == null) {
            return null
        }

        return dateTime.minusHours(hours)
    }

    // ---------------------------------------------------------------------
    // Validation
    // ---------------------------------------------------------------------

    fun isValidDate(value: String?): Boolean {
        return isValidDate(value, DATE_FORMATTER)
    }

    fun isValidDate(
        value: String?,
        formatter: DateTimeFormatter
    ): Boolean {
        if (isBlank(value)) {
            return false
        }

        return try {
            LocalDate.parse(value, formatter)
            true
        } catch (e: DateTimeParseException) {
            false
        }
    }

    fun isValidDateTime(value: String?): Boolean {
        return isValidDateTime(value, DATE_TIME_FORMATTER)
    }

    fun isValidDateTime(
        value: String?,
        formatter: DateTimeFormatter
    ): Boolean {
        if (isBlank(value)) {
            return false
        }

        return try {
            LocalDateTime.parse(value, formatter)
            true
        } catch (e: DateTimeParseException) {
            false
        }
    }

    // ---------------------------------------------------------------------
    // Private
    // ---------------------------------------------------------------------

    private fun isBlank(value: String?): Boolean {
        return value == null || value.isBlank()
    }
}