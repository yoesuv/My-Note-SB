package com.yoesuv.mynote.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.yoesuv.mynote.repository.UserRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LoginTests {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    @BeforeEach
    fun setup() {
        userRepository.deleteAll()

        val registerRequest = mapOf(
            "fullName" to "Test User",
            "email" to "test@example.com",
            "password" to "password123"
        )

        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest))
        )
    }

    @AfterEach
    fun cleanup() {
        userRepository.deleteAll()
    }

    @Nested
    inner class SuccessCases {
        @Test
        fun `should login successfully with valid credentials`() {
            val request = mapOf(
                "email" to "test@example.com",
                "password" to "password123"
            )

            mockMvc.perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.fullName").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
        }
    }

    @Nested
    inner class AuthenticationErrors {
        @Test
        fun `should return 401 when email is not registered`() {
            val request = mapOf(
                "email" to "nonexistent@example.com",
                "password" to "password123"
            )

            mockMvc.perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isUnauthorized)
                .andExpect(jsonPath("$.error").value("Email not registered"))
        }

        @Test
        fun `should return 401 when password is wrong`() {
            val request = mapOf(
                "email" to "test@example.com",
                "password" to "wrongpassword"
            )

            mockMvc.perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isUnauthorized)
                .andExpect(jsonPath("$.error").value("Wrong password"))
        }
    }

    @Nested
    inner class ValidationErrors {
        @Test
        fun `should return 400 when email is empty`() {
            val request = mapOf(
                "email" to "",
                "password" to "password123"
            )

            mockMvc.perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.errors.email").value("Email is required"))
        }

        @Test
        fun `should return 400 when email format is invalid`() {
            val request = mapOf(
                "email" to "invalid-email",
                "password" to "password123"
            )

            mockMvc.perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.errors.email").value("Invalid email format"))
        }

        @Test
        fun `should return 400 when password is empty`() {
            val request = mapOf(
                "email" to "test@example.com",
                "password" to ""
            )

            mockMvc.perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.errors.password").value("Password is required"))
        }

        @Test
        fun `should return 400 when all fields are missing`() {
            val request = mapOf<String, String>()

            mockMvc.perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists())
        }

        @Test
        fun `should return 400 when JSON format is invalid`() {
            mockMvc.perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("not-valid-json")
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.error").value("Invalid JSON format"))
        }
    }
}