package com.revy.entity.domain.dto

import java.util.UUID

data class BookDto(
    var id: UUID? = null,
    var title: String? = null,
    var author: String? = null,
)
