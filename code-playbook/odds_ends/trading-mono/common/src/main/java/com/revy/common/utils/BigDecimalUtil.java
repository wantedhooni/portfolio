package com.revy.common.utils;

import java.math.BigDecimal;

public class BigDecimalUtil {
    // 유틸리티 클래스이므로 인스턴스 생성을 방지합니다.
    private BigDecimalUtil() {
    }

    /**
     * 두 BigDecimal 값이 같은지 (값만 비교, 스케일 무시) 확인합니다.
     * null 입력 시 false를 반환합니다.
     */
    public static boolean isEquals(BigDecimal val1, BigDecimal val2) {
        if (val1 == null || val2 == null) {
            return false;
        }
        // compareTo는 값이 같으면 0을 반환합니다.
        return val1.compareTo(val2) == 0;
    }

    /**
     * 첫 번째 BigDecimal이 두 번째 BigDecimal보다 큰지 확인합니다.
     * null 안전성을 위해 Objects.requireNonNullElse를 사용하거나 별도의 null 처리가 필요합니다.
     * 여기서는 null이 아닌 경우를 가정합니다.
     * (var1 > var2) == true
     */
    public static boolean isGreaterThan(BigDecimal val1, BigDecimal val2) {
        if (val1 == null || val2 == null) {
            // 요구사항에 따라 null 처리 방식을 조정할 수 있습니다.
            return false;
        }
        // compareTo 결과가 0보다 크면(1) val1이 더 큰 것입니다.
        return val1.compareTo(val2) > 0;
    }

    /**
     * 첫 번째 BigDecimal이 두 번째 BigDecimal보다 큰거나 같은지 확인합니다.
     * null 안전성을 위해 Objects.requireNonNullElse를 사용하거나 별도의 null 처리가 필요합니다.
     * 여기서는 null이 아닌 경우를 가정합니다.
     * (var1 >= var2) == true
     */
    public static boolean isGreaterThanOrEqualTo(BigDecimal val1, BigDecimal val2) {
        if (val1 == null || val2 == null) {
            // 요구사항에 따라 null 처리 방식을 조정할 수 있습니다.
            return false;
        }
        // compareTo 결과가 0보다 크면(1) val1이 더 큰 것입니다.
        return val1.compareTo(val2) > 0 || val1.compareTo(val2) == 0;
    }

    /**
     * 첫 번째 BigDecimal이 두 번째 BigDecimal보다 작거나 같은지 확인합니다.
     * null이 아닌 경우를 가정합니다.
     */
    public static boolean isLessOrEqual(BigDecimal val1, BigDecimal val2) {
        if (val1 == null || val2 == null) {
            return false;
        }
        // compareTo 결과가 0보다 작거나 같으면(-1 또는 0) val1이 더 작거나 같은 것입니다.
        return val1.compareTo(val2) <= 0;
    }

    /**
     * BigDecimal 값이 0인지 확인합니다. (스케일 무시)
     */
    public static boolean isZero(BigDecimal val) {
        if (val == null) {
            return false;
        }
        // BigDecimal.ZERO와 비교합니다.
        return val.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * BigDecimal 값이 양수인지 확인합니다.
     */
    public static boolean isPositive(BigDecimal val) {
        if (val == null) {
            return false;
        }
        // signum() 메서드는 값이 양수일 때 1, 음수일 때 -1, 0일 때 0을 반환합니다.
        return val.signum() == 1;
    }
}
