package com.revy.example.sample_kotlin



class example {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            println("Hello, World!")

            // null safety
            var name: String = "Revy"
            var nickname: String? = null
            println("Name: $name")
            println("Nickname: $nickname")

            var length1 = name.length
            var length2 = nickname?.length ?: 0
            var length3 = nickname ?: "default".length
            println("Length of name: $length1")
            println("Length of nickname: $length2")
            println("Length of default nickname: $length3")
        }
    }
}

