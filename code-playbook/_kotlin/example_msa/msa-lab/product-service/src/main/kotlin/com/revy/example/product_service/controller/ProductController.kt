package com.revy.example.product_service.controller

import org.springframework.web.bind.annotation.*
import java.util.concurrent.ConcurrentHashMap

@RestController
@RequestMapping("/products")
class ProductController {

    private val store = ConcurrentHashMap(
        mapOf(
            1L to Product(1, "Keyboard", 35_000),
            2L to Product(2, "Mouse", 18_000),
        )
    )

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): Product {
        return store[id] ?: throw NoSuchElementException("product $id not found")
    }


}