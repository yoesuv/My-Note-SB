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
class RegisterTests {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    @BeforeEach
    fun setup() {
        userRepository.deleteAll()
    }

    @AfterEach
    fun cleanup() {
        userRepository.deleteAll()
    }

    @Nested
    inner class SuccessCases {
        @Test
        fun `should register new user successfully`() {
            val request = mapOf(
                "fullName" to "John Doe",
                "email" to "john@example.com",
                "password" to "securepassword"
            )

            mockMvc.perform(
                post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.fullName").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
        }
    }

    @Nested
    inner class EmailAlreadyRegistered {
        @Test
        fun `should return 409 when email already exists`() {
            val request = mapOf(
                "fullName" to "Test User",
                "email" to "test@example.com",
                "password" to "password123"
            )

            mockMvc.perform(
                post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)

            mockMvc.perform(
                post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isConflict)
                .andExpect(jsonPath("$.error").value("User already exists with email: test@example.com"))
        }
    }

    @Nested
    inner class ValidationErrors {
        @Test
        fun `should return 400 when fullName is empty`() {
            val request = mapOf(
                "fullName" to "",
                "email" to "test@example.com",
                "password" to "password123"
            )

            mockMvc.perform(
                post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.errors.fullName").value("Full name is required"))
        }

        @Test
        fun `should return 400 when fullName is too short`() {
            val request = mapOf(
                "fullName" to "A",
                "email" to "test@example.com",
                "password" to "password123"
            )

            mockMvc.perform(
                post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.errors.fullName").value("Full name must be between 2 and 100 characters"))
        }

        @Test
        fun `should return 400 when email is empty`() {
            val request = mapOf(
                "fullName" to "Test User",
                "email" to "",
                "password" to "password123"
            )

            mockMvc.perform(
                post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.errors.email").value("Email is required"))
        }

        @Test
        fun `should return 400 when email format is invalid`() {
            val request = mapOf(
                "fullName" to "Test User",
                "email" to "invalid-email",
                "password" to "password123"
            )

            mockMvc.perform(
                post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.errors.email").value("Invalid email format"))
        }

        @Test
        fun `should return 400 when password is empty`() {
            val request = mapOf(
                "fullName" to "Test User",
                "email" to "test@example.com",
                "password" to ""
            )

            mockMvc.perform(
                post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.errors.password").value("Password is required"))
        }

        @Test
        fun `should return 400 when password is too short`() {
            val request = mapOf(
                "fullName" to "Test User",
                "email" to "test@example.com",
                "password" to "12345"
            )

            mockMvc.perform(
                post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.errors.password").value("Password must be at least 6 characters"))
        }

        @Test
        fun `should return 400 when all fields are missing`() {
            val request = mapOf<String, String>()

            mockMvc.perform(
                post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.errors.password").exists())
                .andExpect(jsonPath("$.errors.fullName").exists())
                .andExpect(jsonPath("$.errors.email").exists())
        }

        @Test
        fun `should return 400 when JSON format is invalid`() {
            mockMvc.perform(
                post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("not-valid-json")
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.error").value("Invalid JSON format"))
        }
    }
}