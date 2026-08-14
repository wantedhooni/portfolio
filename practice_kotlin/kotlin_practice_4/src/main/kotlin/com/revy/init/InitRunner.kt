package com.revy.init

import com.revy.entity.domain.BookBulkHandler
import com.revy.entity.domain.BookHandler
import org.slf4j.LoggerFactory

import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class InitRunner(
    val handler: BookHandler, val bookBulkHandler: BookBulkHandler
) : CommandLineRunner {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    override fun run(vararg args: String) {
        log.info("=== createBook START ===")
        repeat(50) { i ->
            var result = handler.createBook("title${i}", "author${i}")
            log.info("info: ${result}")
        }
        log.info("=== createBook END ===")

        log.info("=== insertBulkBookOther START ===")
        // bookBulkHandler.insertBulkBookOther()
        log.info("=== insertBulkBookOther END ===")


        log.info("=== insertBulkBookOtherV2 START ===")
        bookBulkHandler.insertBulkBookOtherV2()
        log.info("=== insertBulkBookOtherV2 END ===")
    }


}
