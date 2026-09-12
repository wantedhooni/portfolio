package com.revy

class Example_1
    fun main(){
        println("Hello World!")

        val name = "kotlin"
        var age = 10
        age++

        val count:Int = 10
        val message:String = "hellp"
        val inference = 100
        val inferenceLong = 100L


        var name1: String? = null
        // val name1Length = name1?.length -> null
        val name1Length = name1?.length ?:0
        println("name1: ${name1}")
        // println("name1.Length: ${name1!!.length}")
        println("name1Length: ${name1Length}")

        //if
        val a = 10
        val b = 20

        val max = if (a > b) {
            a
        } else {
            b
        }
        println("max: ${max}")

        val max2 = if (a > b) a else b
        println("max2: ${max2}")





    }
