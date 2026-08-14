package com.revy.controller

import com.revy.entity.domain.BookReader
import com.revy.entity.domain.dto.BookDto
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping

@RestController
@RequestMapping("/book")
class BookController(
    val reader: BookReader
) {

    @GetMapping("/test1")
    fun findAllByProjectionsBean(): ResponseEntity<List<BookDto>> {
        return ResponseEntity.ok(reader.findAllByProjectionsBean())
    }

    @GetMapping("/test2")
    fun findAllByProjectionsConstructor(): ResponseEntity<List<BookDto>> {
        return ResponseEntity.ok(reader.findAllByProjectionsConstructor())
    }
}
