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

    companion object {
        private const val BASE_URL = "/api/auth/login"
        private const val REGISTER_URL = "/api/auth/register"
        private const val TEST_EMAIL = "test@example.com"
        private const val TEST_PASSWORD = "password123"
        private const val KEY_EMAIL = "email"
        private const val KEY_PASSWORD = "password"
        private const val KEY_FULL_NAME = "fullName"
        private const val JSON_PATH_ERROR = "$.error"
        private const val JSON_PATH_ERRORS_EMAIL = "$.errors.email"
        private const val JSON_PATH_ERRORS_PASSWORD = "$.errors.password"
    }

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
            KEY_FULL_NAME to "Test User",
            KEY_EMAIL to TEST_EMAIL,
            KEY_PASSWORD to TEST_PASSWORD
        )

        mockMvc.perform(
            post(REGISTER_URL)
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
                KEY_EMAIL to TEST_EMAIL,
                KEY_PASSWORD to TEST_PASSWORD
            )

            mockMvc.perform(
                post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.fullName").value("Test User"))
                .andExpect(jsonPath("$.email").value(TEST_EMAIL))
        }
    }

    @Nested
    inner class AuthenticationErrors {
        @Test
        fun `should return 401 when email is not registered`() {
            val request = mapOf(
                KEY_EMAIL to "nonexistent@example.com",
                KEY_PASSWORD to TEST_PASSWORD
            )

            mockMvc.perform(
                post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isUnauthorized)
                .andExpect(jsonPath(JSON_PATH_ERROR).value("Email not registered"))
        }

        @Test
        fun `should return 401 when password is wrong`() {
            val request = mapOf(
                KEY_EMAIL to TEST_EMAIL,
                KEY_PASSWORD to "wrongpassword"
            )

            mockMvc.perform(
                post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isUnauthorized)
                .andExpect(jsonPath(JSON_PATH_ERROR).value("Wrong password"))
        }
    }

    @Nested
    inner class ValidationErrors {
        @Test
        fun `should return 400 when email is empty`() {
            val request = mapOf(
                KEY_EMAIL to "",
                KEY_PASSWORD to TEST_PASSWORD
            )

            mockMvc.perform(
                post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath(JSON_PATH_ERRORS_EMAIL).value("Email is required"))
        }

        @Test
        fun `should return 400 when email format is invalid`() {
            val request = mapOf(
                KEY_EMAIL to "invalid-email",
                KEY_PASSWORD to TEST_PASSWORD
            )

            mockMvc.perform(
                post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath(JSON_PATH_ERRORS_EMAIL).value("Invalid email format"))
        }

        @Test
        fun `should return 400 when password is empty`() {
            val request = mapOf(
                KEY_EMAIL to TEST_EMAIL,
                KEY_PASSWORD to ""
            )

            mockMvc.perform(
                post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath(JSON_PATH_ERRORS_PASSWORD).value("Password is required"))
        }

        @Test
        fun `should return 400 when all fields are missing`() {
            val request = mapOf<String, String>()

            mockMvc.perform(
                post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath(JSON_PATH_ERRORS_EMAIL).exists())
                .andExpect(jsonPath(JSON_PATH_ERRORS_PASSWORD).exists())
        }

        @Test
        fun `should return 400 when JSON format is invalid`() {
            mockMvc.perform(
                post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("not-valid-json")
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath(JSON_PATH_ERROR).value("Invalid JSON format"))
        }
    }
}