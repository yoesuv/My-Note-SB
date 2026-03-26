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
            "fullName" to "Test User",
            "email" to "test@example.com",
            "password" to "password123"
        )

        val result = mockMvc.perform(
            post("/api/auth/register")
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
                post("/api/auth/logout")
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
            mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isForbidden)
        }

        @Test
        fun `should return 403 when token is invalid`() {
            mockMvc.perform(
                post("/api/auth/logout")
                    .header("Authorization", "Bearer invalid-token")
            )
                .andExpect(status().isForbidden)
        }
    }
}