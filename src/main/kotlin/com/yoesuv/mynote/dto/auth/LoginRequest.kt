package com.yoesuv.mynote.dto.auth

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String? = null,

    @field:NotBlank(message = "Password is required")
    val password: String? = null
)