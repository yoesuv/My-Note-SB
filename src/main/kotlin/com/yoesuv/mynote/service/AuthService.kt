package com.yoesuv.mynote.service

import com.yoesuv.mynote.domain.User
import com.yoesuv.mynote.dto.auth.AuthResponse
import com.yoesuv.mynote.dto.auth.LoginRequest
import com.yoesuv.mynote.dto.auth.RegisterRequest
import com.yoesuv.mynote.exception.errors.InvalidCredentialsException
import com.yoesuv.mynote.exception.errors.UserAlreadyExistsException
import com.yoesuv.mynote.repository.UserRepository
import com.yoesuv.mynote.security.JwtService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService
) {

    @Transactional
    fun register(request: RegisterRequest): AuthResponse {
        val email = request.email!!
        if (userRepository.existsByEmail(email)) {
            throw UserAlreadyExistsException(email)
        }

        val user = User(
            fullName = request.fullName!!,
            email = email,
            passwordHash = passwordEncoder.encode(request.password!!)
        )

        val savedUser = userRepository.save(user)
        val token = jwtService.generateToken(savedUser.email, savedUser.id!!)

        return AuthResponse(
            token = token,
            userId = savedUser.id!!,
            fullName = savedUser.fullName,
            email = savedUser.email
        )
    }

    fun login(request: LoginRequest): AuthResponse {
        val user = userRepository.findByEmail(request.email!!)
            ?: throw InvalidCredentialsException("Email not registered")

        if (!passwordEncoder.matches(request.password!!, user.passwordHash)) {
            throw InvalidCredentialsException("Wrong password")
        }

        val token = jwtService.generateToken(user.email, user.id!!)

        return AuthResponse(
            token = token,
            userId = user.id!!,
            fullName = user.fullName,
            email = user.email
        )
    }

    fun logout() {
    }
}