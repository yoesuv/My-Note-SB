package com.yoesuv.mynote.service

import com.yoesuv.mynote.database.models.User
import com.yoesuv.mynote.dto.AuthResponse
import com.yoesuv.mynote.dto.LoginRequest
import com.yoesuv.mynote.dto.RegisterRequest
import com.yoesuv.mynote.repository.UserRepository
import com.yoesuv.mynote.security.JwtService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService
) {

    @Transactional
    fun register(request: RegisterRequest): AuthResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("Email already registered")
        }

        val user = User(
            fullName = request.fullName,
            email = request.email,
            passwordHash = passwordEncoder.encode(request.password),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val savedUser = userRepository.save(user)
        val token = jwtService.generateToken(savedUser.email, savedUser.id!!)

        return AuthResponse(
            token = token,
            userId = savedUser.id,
            fullName = savedUser.fullName,
            email = savedUser.email
        )
    }

    fun login(request: LoginRequest): AuthResponse {
        val user = userRepository.findByEmail(request.email)
            ?: throw IllegalArgumentException("Email not registered")

        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            throw IllegalArgumentException("Wrong password")
        }

        val token = jwtService.generateToken(user.email, user.id!!)

        return AuthResponse(
            token = token,
            userId = user.id,
            fullName = user.fullName,
            email = user.email
        )
    }

    fun logout() {
        // Stateless logout - token removal handled client-side
        // Server doesn't store active tokens
    }
}