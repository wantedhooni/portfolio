package com.revy.batch.chapter_04.service

import org.springframework.stereotype.Service

@Service
class CustomService {
    fun serviceMethod(message: String) {
        println(message)
    }
}