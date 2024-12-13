package com.capstone.emoticalm

data class ApiResponse(
    val data: Data,
    val status: Status
)

data class Data(
    val image_url: String,
    val predicted_label: String,
    val predictions: Any,
    val suggestion: Any
)

data class Status(
    val code: Int,
    val message: String
)
