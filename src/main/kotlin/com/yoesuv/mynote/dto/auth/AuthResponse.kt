package com.yoesuv.mynote.dto.auth

data class AuthResponse(
    val token: String,
    val userId: Long,
    val fullName: String,
    val email: String
)