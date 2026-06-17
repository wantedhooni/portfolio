package com.revy.example.utils

class BigdecimalUtils {
    companion object {
        fun isZero(value: String): Boolean {
            return value.toBigDecimalOrNull()?.compareTo(0.toBigDecimal()) == 0
        }
    }
}