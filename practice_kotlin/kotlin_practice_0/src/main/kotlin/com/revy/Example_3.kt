package com.revy

import java.math.BigDecimal

class Example_3 {}

fun main() {
    // String Sample
    val name = "Kotlin"
    val version = 2.4
    val amount: BigDecimal = BigDecimal(100)
    println("Hello. $name!")
    println("Hello. ${name.length}")
    println("version *2 ${version * 2}")
    println("amount: \$${amount}")


    val longText = """
        첫줄
        두번째 줄
    """.trimIndent()
    println(longText)

    val json = """
           |{
           | "key": "value"
           |}     
    """.trimMargin()
    println(json)

    val s = "Hello, Kotlin World"

    s.length                    // 19
    s[0]                        // 'H'
    s.uppercase()               // "HELLO, KOTLIN WORLD"
    s.lowercase()
    s.trim()                    // 앞뒤 공백 제거
    s.substring(7)              // "Kotlin World"
    s.substring(7, 13)          // "Kotlin"
    s.split(", ")               // ["Hello", "Kotlin World"]
    s.replace("Kotlin", "Java") // "Hello, Java World"
    s.contains("Kotlin")        // true
    s.startsWith("Hello")       // true
    s.endsWith("World")         // true
    s.indexOf("Kotlin")         // 7
    s.reversed()
    s.repeat(2)
    s.isEmpty()                 // 길이 0
    s.isBlank()                 // 공백만 있어도 true
    s.first(); s.last()
    s.take(5)                   // "Hello"
    s.drop(7)                   // "Kotlin World"
    s.padStart(25, '*')
    s.lines()                   // 줄 단위 분리

// 리스트 → 문자열
    listOf(1, 2, 3).joinToString(separator = ", ", prefix = "[", postfix = "]")
// "[1, 2, 3]"

// 문자열 비교
    "abc".equals("ABC", ignoreCase = true)  // true
    "abc".compareTo("abd")                  // 음수

    // #StringBuilder
    val sb = StringBuilder()
    sb.append("Hello")
    sb.append(", ").append("World")
    sb.insert(0, ">> ")
    println("sb: ${sb.toString()}")

    val built = buildString {
        for(i in 1..10) {
            append(">${i}")
        }
    }
    println("built: ${built}")




}