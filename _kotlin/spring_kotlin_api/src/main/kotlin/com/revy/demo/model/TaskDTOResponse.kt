package com.revy.demo.model


data class TaskDtoResponse(
    var id: Long,
    var name: String,
    var description: String,
    var done: Boolean
)