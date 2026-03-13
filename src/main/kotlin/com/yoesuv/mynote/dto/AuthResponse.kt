package com.yoesuv.mynote.dto

data class AuthResponse(
    val token: String,
    val userId: Long,
    val fullName: String,
    val email: String
)