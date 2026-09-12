package com.revy

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.revy"])
class DemoApplication

fun main(args: Array<String>) {
    runApplication<DemoApplication>(*args)
}
