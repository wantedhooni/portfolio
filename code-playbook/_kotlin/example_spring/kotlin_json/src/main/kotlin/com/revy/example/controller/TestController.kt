package com.revy.example.controller

import com.revy.example.controller.dto.TestRes
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping

@RestController
@RequestMapping("/")
class TestController {

    @PostMapping
    fun test():  TestRes {
        return TestRes(1L, "Test");
    }
}
