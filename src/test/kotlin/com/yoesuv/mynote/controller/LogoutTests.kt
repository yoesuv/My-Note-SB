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
class LogoutTests {

    companion object {
        private const val LOGOUT_URL = "/api/auth/logout"
        private const val REGISTER_URL = "/api/auth/register"
        private const val TEST_EMAIL = "test@example.com"
        private const val TEST_PASSWORD = "password123"
        private const val KEY_EMAIL = "email"
        private const val KEY_PASSWORD = "password"
        private const val KEY_FULL_NAME = "fullName"
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    private var authToken: String = ""

    @BeforeEach
    fun setup() {
        userRepository.deleteAll()

        val registerRequest = mapOf(
            KEY_FULL_NAME to "Test User",
            KEY_EMAIL to TEST_EMAIL,
            KEY_PASSWORD to TEST_PASSWORD
        )

        val result = mockMvc.perform(
            post(REGISTER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest))
        ).andReturn()

        val response = objectMapper.readTree(result.response.contentAsString)
        authToken = response.get("token").asText()
    }

    @AfterEach
    fun cleanup() {
        userRepository.deleteAll()
    }

    @Nested
    inner class SuccessCases {
        @Test
        fun `should logout successfully with valid token`() {
            mockMvc.perform(
                post(LOGOUT_URL)
                    .header("Authorization", "Bearer $authToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.message").value("Logged out successfully"))
        }
    }

    @Nested
    inner class AuthenticationErrors {
        @Test
        fun `should return 403 when no token provided`() {
            mockMvc.perform(post(LOGOUT_URL))
                .andExpect(status().isForbidden)
        }

        @Test
        fun `should return 403 when token is invalid`() {
            mockMvc.perform(
                post(LOGOUT_URL)
                    .header("Authorization", "Bearer invalid-token")
            )
                .andExpect(status().isForbidden)
        }
    }
}