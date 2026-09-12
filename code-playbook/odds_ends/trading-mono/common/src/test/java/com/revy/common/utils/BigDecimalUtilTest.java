package com.revy.common.utils;


import java.math.BigDecimal;

class BigDecimalUtilTest {
    public static void main(String[] args) {
        System.out.println("BigDecimalUtilTest");
        System.out.println(BigDecimalUtil.isGreaterThanOrEqualTo(BigDecimal.ZERO, BigDecimal.ZERO));
        System.out.println(!BigDecimalUtil.isGreaterThan(BigDecimal.ZERO, BigDecimal.ZERO));
    }
}