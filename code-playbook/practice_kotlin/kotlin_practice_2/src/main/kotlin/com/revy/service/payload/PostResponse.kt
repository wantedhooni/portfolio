package com.revy.service.payload

import java.util.*

data class PostResponse(
    val id: UUID,
    val title: String,
    val content: String
)
