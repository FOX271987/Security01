package com.example.security01.models

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val user: User? = null
)