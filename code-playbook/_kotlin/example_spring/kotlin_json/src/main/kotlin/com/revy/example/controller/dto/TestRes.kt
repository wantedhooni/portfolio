package com.revy.example.controller.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class TestRes(
    @JsonProperty("Id")
    var id: Long,
     @JsonProperty("user_name")
    var nameName: String
)
