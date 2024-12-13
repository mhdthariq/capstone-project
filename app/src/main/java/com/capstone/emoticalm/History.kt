package com.capstone.emoticalm

import java.util.Date

data class History(
    val userId: String = "",
    val fullname: String = "",
    val email: String = "",
    val predictedLabel: String = "",
    val image: String = "",
    val timestamp: String = ""//Long = 0L, // Change timestamp to Long
)
