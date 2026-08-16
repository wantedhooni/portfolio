package com.revy.trading

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.revy.trading"])
class AppApplication

fun main(args: Array<String>) {
    runApplication<AppApplication>(*args)
}
