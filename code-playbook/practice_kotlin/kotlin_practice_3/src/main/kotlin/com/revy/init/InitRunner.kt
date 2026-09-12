package com.revy.init

import com.revy.entity.domain.BookHandler
import org.slf4j.LoggerFactory

import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class InitRunner(val handler: BookHandler) : CommandLineRunner {
    private val log = LoggerFactory.getLogger(javaClass)
    @Transactional
    override fun run(vararg args: String) {
        repeat(50) { i ->
            var result = handler.createBook("title${i}", "author${i}")
            log.info("info: ${result}")
        }
    }



}
