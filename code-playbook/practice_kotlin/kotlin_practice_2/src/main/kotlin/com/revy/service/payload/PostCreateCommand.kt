package com.revy.service.payload

import jakarta.validation.constraints.NotEmpty

data class PostCreateCommand(
    @NotEmpty
    val title: String,
    @NotEmpty
    val content: String
)
