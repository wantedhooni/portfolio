package com.revy

class Example2


fun main(args: Array<String>) {
    val name = "revy"
    var count = 0
    count += 1
    count++
    ++count
    println("count:${count}")

    // 형변환
    val i: Int = 1
    val long: Long = i.toLong()
    val short: Short = i.toShort()
    val double: Double = i.toDouble()
    val string: String = i.toString()

    println("${name}, ${long}, ${short}, ${double}, ${string}")

    val parsed:Int = "123".toInt()



}