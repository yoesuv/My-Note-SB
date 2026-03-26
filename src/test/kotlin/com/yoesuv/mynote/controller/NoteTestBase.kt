package com.yoesuv.mynote.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.yoesuv.mynote.repository.CategoryRepository
import com.yoesuv.mynote.repository.NoteRepository
import com.yoesuv.mynote.repository.UserRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
abstract class NoteTestBase {

    @Autowired
    protected lateinit var mockMvc: MockMvc

    @Autowired
    protected lateinit var objectMapper: ObjectMapper

    @Autowired
    protected lateinit var userRepository: UserRepository

    @Autowired
    protected lateinit var categoryRepository: CategoryRepository

    @Autowired
    protected lateinit var noteRepository: NoteRepository

    protected var authToken: String = ""

    @BeforeEach
    fun setup() {
        userRepository.deleteAll()
        categoryRepository.deleteAll()
        noteRepository.deleteAll()

        val registerRequest = mapOf(
            TestConstants.KEY_FULL_NAME to TestConstants.TEST_FULL_NAME,
            TestConstants.KEY_EMAIL to TestConstants.TEST_EMAIL,
            TestConstants.KEY_PASSWORD to TestConstants.TEST_PASSWORD
        )

        val result = mockMvc.perform(
            post(TestConstants.AUTH_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest))
        ).andReturn()

        val response = objectMapper.readTree(result.response.contentAsString)
        authToken = response.get("token").asText()
    }

    @AfterEach
    fun cleanup() {
        noteRepository.deleteAll()
        categoryRepository.deleteAll()
        userRepository.deleteAll()
    }

    protected fun createCategory(name: String, color: String): Long {
        val request = mapOf(
            TestConstants.KEY_NAME to name,
            TestConstants.KEY_COLOR to color
        )

        val result = mockMvc.perform(
            post(TestConstants.BASE_URL_CATEGORIES)
                .header(TestConstants.HEADER_AUTHORIZATION, TestConstants.BEARER_PREFIX + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andReturn()

        val response = objectMapper.readTree(result.response.contentAsString)
        return response.get("id").asLong()
    }

    protected fun createNote(title: String, content: String): Long {
        val request = mapOf(
            TestConstants.KEY_TITLE to title,
            TestConstants.KEY_CONTENT to content
        )

        val result = mockMvc.perform(
            post(TestConstants.BASE_URL_NOTES)
                .header(TestConstants.HEADER_AUTHORIZATION, TestConstants.BEARER_PREFIX + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andReturn()

        val response = objectMapper.readTree(result.response.contentAsString)
        return response.get("id").asLong()
    }

    protected fun createNoteWithCategory(title: String, content: String, categoryId: Long): Long {
        val request = mapOf(
            TestConstants.KEY_TITLE to title,
            TestConstants.KEY_CONTENT to content,
            TestConstants.KEY_CATEGORY_ID to categoryId
        )

        val result = mockMvc.perform(
            post(TestConstants.BASE_URL_NOTES)
                .header(TestConstants.HEADER_AUTHORIZATION, TestConstants.BEARER_PREFIX + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andReturn()

        val response = objectMapper.readTree(result.response.contentAsString)
        return response.get("id").asLong()
    }
}