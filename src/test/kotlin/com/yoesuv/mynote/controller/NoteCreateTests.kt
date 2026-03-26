package com.yoesuv.mynote.controller

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class NoteCreateTests : NoteTestBase() {

    @Nested
    inner class CreateNote {
        @Test
        fun `should create note successfully`() {
            val request = mapOf(
                TestConstants.KEY_TITLE to TestConstants.NOTE_TEST_TITLE,
                TestConstants.KEY_CONTENT to TestConstants.NOTE_TEST_CONTENT
            )

            mockMvc.perform(
                post(TestConstants.BASE_URL_NOTES)
                    .header(TestConstants.HEADER_AUTHORIZATION, TestConstants.BEARER_PREFIX + authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath(TestConstants.JSON_PATH_ID).exists())
                .andExpect(jsonPath(TestConstants.JSON_PATH_TITLE).value(TestConstants.NOTE_TEST_TITLE))
                .andExpect(jsonPath(TestConstants.JSON_PATH_CONTENT).value(TestConstants.NOTE_TEST_CONTENT))
                .andExpect(jsonPath(TestConstants.JSON_PATH_IS_PINNED).value(false))
                .andExpect(jsonPath(TestConstants.JSON_PATH_USER_ID).exists())
        }

        @Test
        fun `should create note with category`() {
            val categoryId = createCategory(TestConstants.CATEGORY_WORK, TestConstants.COLOR_RED)

            val request = mapOf(
                TestConstants.KEY_TITLE to TestConstants.NOTE_TEST_TITLE,
                TestConstants.KEY_CONTENT to TestConstants.NOTE_TEST_CONTENT,
                TestConstants.KEY_CATEGORY_ID to categoryId
            )

            mockMvc.perform(
                post(TestConstants.BASE_URL_NOTES)
                    .header(TestConstants.HEADER_AUTHORIZATION, TestConstants.BEARER_PREFIX + authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath(TestConstants.JSON_PATH_ID).exists())
                .andExpect(jsonPath(TestConstants.JSON_PATH_TITLE).value(TestConstants.NOTE_TEST_TITLE))
                .andExpect(jsonPath(TestConstants.JSON_PATH_CATEGORY_ID).value(categoryId))
                .andExpect(jsonPath(TestConstants.JSON_PATH_CATEGORY_NAME).value(TestConstants.CATEGORY_WORK))
        }

        @Test
        fun `should create note with isPinned true`() {
            val request = mapOf(
                TestConstants.KEY_TITLE to TestConstants.NOTE_PINNED_TITLE,
                TestConstants.KEY_CONTENT to TestConstants.NOTE_TEST_CONTENT,
                TestConstants.KEY_IS_PINNED to true
            )

            mockMvc.perform(
                post(TestConstants.BASE_URL_NOTES)
                    .header(TestConstants.HEADER_AUTHORIZATION, TestConstants.BEARER_PREFIX + authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath(TestConstants.JSON_PATH_IS_PINNED).value(true))
        }

        @Test
        fun `should create note without content`() {
            val request = mapOf(
                TestConstants.KEY_TITLE to TestConstants.NOTE_TEST_TITLE
            )

            mockMvc.perform(
                post(TestConstants.BASE_URL_NOTES)
                    .header(TestConstants.HEADER_AUTHORIZATION, TestConstants.BEARER_PREFIX + authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath(TestConstants.JSON_PATH_ID).exists())
                .andExpect(jsonPath(TestConstants.JSON_PATH_TITLE).value(TestConstants.NOTE_TEST_TITLE))
                .andExpect(jsonPath(TestConstants.JSON_PATH_CONTENT).doesNotExist())
        }

        @Test
        fun `should return 404 when category not found`() {
            val request = mapOf(
                TestConstants.KEY_TITLE to TestConstants.NOTE_TEST_TITLE,
                TestConstants.KEY_CONTENT to TestConstants.NOTE_TEST_CONTENT,
                TestConstants.KEY_CATEGORY_ID to 999
            )

            mockMvc.perform(
                post(TestConstants.BASE_URL_NOTES)
                    .header(TestConstants.HEADER_AUTHORIZATION, TestConstants.BEARER_PREFIX + authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isNotFound)
                .andExpect(jsonPath(TestConstants.JSON_PATH_ERROR).value(TestConstants.CATEGORY_NOT_FOUND_MSG + " 999"))
        }

        @Test
        fun `should return 400 when title is empty`() {
            val request = mapOf(
                TestConstants.KEY_TITLE to "",
                TestConstants.KEY_CONTENT to TestConstants.NOTE_TEST_CONTENT
            )

            mockMvc.perform(
                post(TestConstants.BASE_URL_NOTES)
                    .header(TestConstants.HEADER_AUTHORIZATION, TestConstants.BEARER_PREFIX + authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath(TestConstants.JSON_PATH_ERRORS_TITLE).value(TestConstants.TITLE_REQUIRED_MSG))
        }

        @Test
        fun `should return 400 when title exceeds max length`() {
            val request = mapOf(
                TestConstants.KEY_TITLE to "A".repeat(201),
                TestConstants.KEY_CONTENT to TestConstants.NOTE_TEST_CONTENT
            )

            mockMvc.perform(
                post(TestConstants.BASE_URL_NOTES)
                    .header(TestConstants.HEADER_AUTHORIZATION, TestConstants.BEARER_PREFIX + authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath(TestConstants.JSON_PATH_ERRORS_TITLE).value(TestConstants.TITLE_LENGTH_MSG))
        }

        @Test
        fun `should return 400 when title is missing`() {
            val request = mapOf(
                TestConstants.KEY_CONTENT to TestConstants.NOTE_TEST_CONTENT
            )

            mockMvc.perform(
                post(TestConstants.BASE_URL_NOTES)
                    .header(TestConstants.HEADER_AUTHORIZATION, TestConstants.BEARER_PREFIX + authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath(TestConstants.JSON_PATH_ERRORS_TITLE).value(TestConstants.TITLE_REQUIRED_MSG))
        }

        @Test
        fun `should return 400 when JSON format is invalid`() {
            mockMvc.perform(
                post(TestConstants.BASE_URL_NOTES)
                    .header(TestConstants.HEADER_AUTHORIZATION, TestConstants.BEARER_PREFIX + authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("not-valid-json")
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath(TestConstants.JSON_PATH_ERROR).value(TestConstants.INVALID_JSON_MSG))
        }

        @Test
        fun `should return 401 when not authenticated`() {
            val request = mapOf(
                TestConstants.KEY_TITLE to TestConstants.NOTE_TEST_TITLE,
                TestConstants.KEY_CONTENT to TestConstants.NOTE_TEST_CONTENT
            )

            mockMvc.perform(
                post(TestConstants.BASE_URL_NOTES)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isForbidden)
        }
    }
}