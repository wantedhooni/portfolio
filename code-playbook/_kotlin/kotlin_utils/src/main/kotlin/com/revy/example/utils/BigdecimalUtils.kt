package com.revy.example.utils

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

// Final class to prevent extension
object BigDecimalUtils {
    // --- COMMON CONSTANTS ---
    val zero: BigDecimal = BigDecimal.ZERO
    val one: BigDecimal = BigDecimal.ONE

    // Default scale for division operations to maintain consistency
    private const val d_scale = 4 // Matches your numeric(10,4)
    private val DRM = RoundingMode.HALF_UP


    // --- COMPARISON METHODS ---
    /**
     * Checks if the first value is less than the second. Handles nulls gracefully.
     * A null value is treated as zero for comparison.
     */
    fun lessThan(a: BigDecimal?, b: BigDecimal?): Boolean {
        val valA = Objects.requireNonNullElse<BigDecimal>(a, zero)
        val valB = Objects.requireNonNullElse<BigDecimal>(b, zero)
        return valA.compareTo(valB) < 0
    }

    /**
     * Checks if the first value is greater than the second. Handles nulls gracefully.
     */
    fun greaterThan(a: BigDecimal?, b: BigDecimal?): Boolean {
        val valA = Objects.requireNonNullElse<BigDecimal>(a, zero)
        val valB = Objects.requireNonNullElse<BigDecimal>(b, zero)
        return valA.compareTo(valB) > 0
    }

    /**
     * Checks if two values are numerically equal. Handles nulls and different scales.
     * This is the correct way to check for equality, NOT .equals().
     */
    fun isEqual(a: BigDecimal?, b: BigDecimal?): Boolean {
        val valA = Objects.requireNonNullElse<BigDecimal>(a, zero)
        val valB = Objects.requireNonNullElse<BigDecimal>(b, zero)
        return valA.compareTo(valB) == 0
    }


    // --- ZERO CHECKS ---
    /**
     * Checks if a value is numerically equal to zero.
     */
    fun isZero(value: BigDecimal?): Boolean {
        return isEqual(value, zero)
    }

    /**
     * Checks if a value is greater than zero.
     */
    fun isPositive(value: BigDecimal?): Boolean {
        return greaterThan(value, zero)
    }

    /**
     * Checks if a value is less than zero.
     */
    fun isNegative(value: BigDecimal?): Boolean {
        return lessThan(value, zero)
    }


    // --- SAFE ARITHMETIC ---
    // These methods can be expanded to handle nulls as well
    fun add(a: BigDecimal?, b: BigDecimal?): BigDecimal {
        val valA = Objects.requireNonNullElse<BigDecimal>(a, zero)
        val valB = Objects.requireNonNullElse<BigDecimal>(b, zero)
        return valA.add(valB)
    }

    fun mult(a: BigDecimal?, b: BigDecimal?): BigDecimal {
        val valA = Objects.requireNonNullElse<BigDecimal>(a, zero)
        val valB = Objects.requireNonNullElse<BigDecimal>(b, zero)
        return valA.multiply(valB)
    }

    fun mult(ingredientQuantity: BigDecimal?, quantity: Int): BigDecimal {
        return mult(ingredientQuantity, BigDecimal.valueOf(quantity.toLong()))
    }

    fun subtract(a: BigDecimal?, b: BigDecimal?): BigDecimal {
        val valA = Objects.requireNonNullElse<BigDecimal>(a, zero)
        val valB = Objects.requireNonNullElse<BigDecimal>(b, zero)
        return valA.subtract(valB)
    }

    @JvmOverloads
    fun divide(a: BigDecimal?, b: BigDecimal?, scale: Int = d_scale, roundingMode: RoundingMode? = DRM): BigDecimal {
        val valA = Objects.requireNonNullElse<BigDecimal>(a, zero)
        val valB = Objects.requireNonNullElse<BigDecimal>(b, zero)
        require(!isZero(valB)) { "Division by zero is not allowed." }
        return valA.divide(valB, scale, roundingMode)
    }
}