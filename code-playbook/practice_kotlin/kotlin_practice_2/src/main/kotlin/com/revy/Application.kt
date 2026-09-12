package com.revy

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.revy"])
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
