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

    companion object {
        private const val BASE_URL = "/api/auth/register"
        private const val TEST_EMAIL = "test@example.com"
        private const val TEST_PASSWORD = "password123"
        private const val TEST_FULL_NAME = "Test User"
        private const val KEY_EMAIL = "email"
        private const val KEY_PASSWORD = "password"
        private const val KEY_FULL_NAME = "fullName"
        private const val JSON_PATH_ERROR = "$.error"
        private const val JSON_PATH_ERRORS_FULL_NAME = "$.errors.fullName"
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
                KEY_FULL_NAME to "John Doe",
                KEY_EMAIL to "john@example.com",
                KEY_PASSWORD to "securepassword"
            )

            mockMvc.perform(
                post(BASE_URL)
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
                KEY_FULL_NAME to TEST_FULL_NAME,
                KEY_EMAIL to TEST_EMAIL,
                KEY_PASSWORD to TEST_PASSWORD
            )

            mockMvc.perform(
                post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)

            mockMvc.perform(
                post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isConflict)
                .andExpect(jsonPath(JSON_PATH_ERROR).value("User already exists with email: $TEST_EMAIL"))
        }
    }

    @Nested
    inner class ValidationErrors {
        @Test
        fun `should return 400 when fullName is empty`() {
            val request = mapOf(
                KEY_FULL_NAME to "",
                KEY_EMAIL to TEST_EMAIL,
                KEY_PASSWORD to TEST_PASSWORD
            )

            mockMvc.perform(
                post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath(JSON_PATH_ERRORS_FULL_NAME).value("Full name is required"))
        }

        @Test
        fun `should return 400 when fullName is too short`() {
            val request = mapOf(
                KEY_FULL_NAME to "A",
                KEY_EMAIL to TEST_EMAIL,
                KEY_PASSWORD to TEST_PASSWORD
            )

            mockMvc.perform(
                post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath(JSON_PATH_ERRORS_FULL_NAME).value("Full name must be between 2 and 100 characters"))
        }

        @Test
        fun `should return 400 when email is empty`() {
            val request = mapOf(
                KEY_FULL_NAME to TEST_FULL_NAME,
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
                KEY_FULL_NAME to TEST_FULL_NAME,
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
                KEY_FULL_NAME to TEST_FULL_NAME,
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
        fun `should return 400 when password is too short`() {
            val request = mapOf(
                KEY_FULL_NAME to TEST_FULL_NAME,
                KEY_EMAIL to TEST_EMAIL,
                KEY_PASSWORD to "12345"
            )

            mockMvc.perform(
                post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath(JSON_PATH_ERRORS_PASSWORD).value("Password must be at least 6 characters"))
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
                .andExpect(jsonPath(JSON_PATH_ERRORS_PASSWORD).exists())
                .andExpect(jsonPath(JSON_PATH_ERRORS_FULL_NAME).exists())
                .andExpect(jsonPath(JSON_PATH_ERRORS_EMAIL).exists())
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