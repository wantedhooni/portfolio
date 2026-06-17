import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public final class DateTimeUtils {

    private DateTimeUtils() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    public static final DateTimeFormatter DATE_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static final DateTimeFormatter DATE_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static final DateTimeFormatter COMPACT_DATE_FORMATTER =
        DateTimeFormatter.ofPattern("yyyyMMdd");

    public static final DateTimeFormatter COMPACT_DATE_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    // ---------------------------------------------------------------------
    // Now / Today
    // ---------------------------------------------------------------------

    public static LocalDate today() {
        return LocalDate.now();
    }

    public static LocalDate today(ZoneId zoneId) {
        return LocalDate.now(Objects.requireNonNull(zoneId));
    }

    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    public static LocalDateTime now(ZoneId zoneId) {
        return LocalDateTime.now(Objects.requireNonNull(zoneId));
    }

    // ---------------------------------------------------------------------
    // Format
    // ---------------------------------------------------------------------

    public static String format(LocalDate date) {
        return format(date, DATE_FORMATTER);
    }

    public static String format(LocalDate date, DateTimeFormatter formatter) {
        if (date == null) {
            return null;
        }
        return date.format(Objects.requireNonNull(formatter));
    }

    public static String format(LocalDateTime dateTime) {
        return format(dateTime, DATE_TIME_FORMATTER);
    }

    public static String format(LocalDateTime dateTime, DateTimeFormatter formatter) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(Objects.requireNonNull(formatter));
    }

    // ---------------------------------------------------------------------
    // Parse
    // ---------------------------------------------------------------------

    public static LocalDate parseDate(String value) {
        return parseDate(value, DATE_FORMATTER);
    }

    public static LocalDate parseDate(String value, DateTimeFormatter formatter) {
        if (isBlank(value)) {
            return null;
        }

        try {
            return LocalDate.parse(value, Objects.requireNonNull(formatter));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid LocalDate value: " + value, e);
        }
    }

    public static LocalDateTime parseDateTime(String value) {
        return parseDateTime(value, DATE_TIME_FORMATTER);
    }

    public static LocalDateTime parseDateTime(String value, DateTimeFormatter formatter) {
        if (isBlank(value)) {
            return null;
        }

        try {
            return LocalDateTime.parse(value, Objects.requireNonNull(formatter));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid LocalDateTime value: " + value, e);
        }
    }

    // ---------------------------------------------------------------------
    // Start / End
    // ---------------------------------------------------------------------

    public static LocalDateTime startOfDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atStartOfDay();
    }

    public static LocalDateTime endOfDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atTime(LocalTime.MAX);
    }

    public static LocalDate firstDayOfMonth(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.withDayOfMonth(1);
    }

    public static LocalDate lastDayOfMonth(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.withDayOfMonth(date.lengthOfMonth());
    }

    public static LocalDate firstDayOfYear(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.withDayOfYear(1);
    }

    public static LocalDate lastDayOfYear(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.withDayOfYear(date.lengthOfYear());
    }

    // ---------------------------------------------------------------------
    // Compare
    // ---------------------------------------------------------------------

    public static boolean isBefore(LocalDate source, LocalDate target) {
        return source != null && target != null && source.isBefore(target);
    }

    public static boolean isAfter(LocalDate source, LocalDate target) {
        return source != null && target != null && source.isAfter(target);
    }

    public static boolean isEqual(LocalDate source, LocalDate target) {
        return source != null && target != null && source.isEqual(target);
    }

    public static boolean isBefore(LocalDateTime source, LocalDateTime target) {
        return source != null && target != null && source.isBefore(target);
    }

    public static boolean isAfter(LocalDateTime source, LocalDateTime target) {
        return source != null && target != null && source.isAfter(target);
    }

    public static boolean isEqual(LocalDateTime source, LocalDateTime target) {
        return source != null && target != null && source.isEqual(target);
    }

    public static boolean isBetween(
        LocalDate target,
        LocalDate startInclusive,
        LocalDate endInclusive
                                   ) {
        if (target == null || startInclusive == null || endInclusive == null) {
            return false;
        }

        return !target.isBefore(startInclusive)
            && !target.isAfter(endInclusive);
    }

    public static boolean isBetween(
        LocalDateTime target,
        LocalDateTime startInclusive,
        LocalDateTime endInclusive
                                   ) {
        if (target == null || startInclusive == null || endInclusive == null) {
            return false;
        }

        return !target.isBefore(startInclusive)
            && !target.isAfter(endInclusive);
    }

    // ---------------------------------------------------------------------
    // Difference
    // ---------------------------------------------------------------------

    public static long daysBetween(LocalDate startInclusive, LocalDate endExclusive) {
        Objects.requireNonNull(startInclusive, "startInclusive must not be null");
        Objects.requireNonNull(endExclusive, "endExclusive must not be null");

        return ChronoUnit.DAYS.between(startInclusive, endExclusive);
    }

    public static long monthsBetween(LocalDate startInclusive, LocalDate endExclusive) {
        Objects.requireNonNull(startInclusive, "startInclusive must not be null");
        Objects.requireNonNull(endExclusive, "endExclusive must not be null");

        return ChronoUnit.MONTHS.between(startInclusive, endExclusive);
    }

    public static long yearsBetween(LocalDate startInclusive, LocalDate endExclusive) {
        Objects.requireNonNull(startInclusive, "startInclusive must not be null");
        Objects.requireNonNull(endExclusive, "endExclusive must not be null");

        return ChronoUnit.YEARS.between(startInclusive, endExclusive);
    }

    public static long secondsBetween(LocalDateTime startInclusive, LocalDateTime endExclusive) {
        Objects.requireNonNull(startInclusive, "startInclusive must not be null");
        Objects.requireNonNull(endExclusive, "endExclusive must not be null");

        return ChronoUnit.SECONDS.between(startInclusive, endExclusive);
    }

    public static long minutesBetween(LocalDateTime startInclusive, LocalDateTime endExclusive) {
        Objects.requireNonNull(startInclusive, "startInclusive must not be null");
        Objects.requireNonNull(endExclusive, "endExclusive must not be null");

        return ChronoUnit.MINUTES.between(startInclusive, endExclusive);
    }

    public static long hoursBetween(LocalDateTime startInclusive, LocalDateTime endExclusive) {
        Objects.requireNonNull(startInclusive, "startInclusive must not be null");
        Objects.requireNonNull(endExclusive, "endExclusive must not be null");

        return ChronoUnit.HOURS.between(startInclusive, endExclusive);
    }

    // ---------------------------------------------------------------------
    // Convert
    // ---------------------------------------------------------------------

    public static LocalDate toLocalDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.toLocalDate();
    }

    public static LocalDateTime toLocalDateTime(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atStartOfDay();
    }

    public static Instant toInstant(LocalDateTime dateTime, ZoneId zoneId) {
        if (dateTime == null) {
            return null;
        }

        return dateTime
            .atZone(Objects.requireNonNull(zoneId))
            .toInstant();
    }

    public static LocalDateTime toLocalDateTime(Instant instant, ZoneId zoneId) {
        if (instant == null) {
            return null;
        }

        return LocalDateTime.ofInstant(
            instant,
            Objects.requireNonNull(zoneId)
                                      );
    }

    public static LocalDate toLocalDate(Instant instant, ZoneId zoneId) {
        if (instant == null) {
            return null;
        }

        return LocalDate.ofInstant(
            instant,
            Objects.requireNonNull(zoneId)
                                  );
    }

    // ---------------------------------------------------------------------
    // Add / Minus
    // ---------------------------------------------------------------------

    public static LocalDate plusDays(LocalDate date, long days) {
        if (date == null) {
            return null;
        }
        return date.plusDays(days);
    }

    public static LocalDate minusDays(LocalDate date, long days) {
        if (date == null) {
            return null;
        }
        return date.minusDays(days);
    }

    public static LocalDateTime plusHours(LocalDateTime dateTime, long hours) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.plusHours(hours);
    }

    public static LocalDateTime minusHours(LocalDateTime dateTime, long hours) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.minusHours(hours);
    }

    // ---------------------------------------------------------------------
    // Validation
    // ---------------------------------------------------------------------

    public static boolean isValidDate(String value) {
        return isValidDate(value, DATE_FORMATTER);
    }

    public static boolean isValidDate(String value, DateTimeFormatter formatter) {
        if (isBlank(value)) {
            return false;
        }

        try {
            LocalDate.parse(value, Objects.requireNonNull(formatter));
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public static boolean isValidDateTime(String value) {
        return isValidDateTime(value, DATE_TIME_FORMATTER);
    }

    public static boolean isValidDateTime(String value, DateTimeFormatter formatter) {
        if (isBlank(value)) {
            return false;
        }

        try {
            LocalDateTime.parse(value, Objects.requireNonNull(formatter));
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    // ---------------------------------------------------------------------
    // Private
    // ---------------------------------------------------------------------

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}