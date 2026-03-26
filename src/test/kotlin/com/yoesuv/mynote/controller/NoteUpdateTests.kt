package com.yoesuv.mynote.controller

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class NoteUpdateTests : NoteTestBase() {

    @Nested
    inner class UpdateNote {
        @Test
        fun `should update note successfully`() {
            val noteId = createNote(TestConstants.NOTE_ORIGINAL_TITLE, TestConstants.NOTE_ORIGINAL_CONTENT)

            val request = mapOf(
                TestConstants.KEY_TITLE to TestConstants.NOTE_UPDATED_TITLE,
                TestConstants.KEY_CONTENT to TestConstants.NOTE_UPDATED_CONTENT
            )

            mockMvc.perform(
                put(TestConstants.BASE_URL_NOTES + "/" + noteId)
                    .header(TestConstants.HEADER_AUTHORIZATION, TestConstants.BEARER_PREFIX + authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath(TestConstants.JSON_PATH_ID).value(noteId))
                .andExpect(jsonPath(TestConstants.JSON_PATH_TITLE).value(TestConstants.NOTE_UPDATED_TITLE))
                .andExpect(jsonPath(TestConstants.JSON_PATH_CONTENT).value(TestConstants.NOTE_UPDATED_CONTENT))
        }

        @Test
        fun `should update note with category`() {
            val categoryId = createCategory(TestConstants.CATEGORY_WORK, TestConstants.COLOR_RED)
            val noteId = createNote(TestConstants.NOTE_ORIGINAL_TITLE, TestConstants.NOTE_ORIGINAL_CONTENT)

            val request = mapOf(
                TestConstants.KEY_TITLE to TestConstants.NOTE_UPDATED_TITLE,
                TestConstants.KEY_CONTENT to TestConstants.NOTE_UPDATED_CONTENT,
                TestConstants.KEY_CATEGORY_ID to categoryId
            )

            mockMvc.perform(
                put(TestConstants.BASE_URL_NOTES + "/" + noteId)
                    .header(TestConstants.HEADER_AUTHORIZATION, TestConstants.BEARER_PREFIX + authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath(TestConstants.JSON_PATH_CATEGORY_ID).value(categoryId))
        }

        @Test
        fun `should update isPinned`() {
            val noteId = createNote(TestConstants.NOTE_TEST_TITLE, TestConstants.NOTE_TEST_CONTENT)

            val request = mapOf(
                TestConstants.KEY_TITLE to TestConstants.NOTE_TEST_TITLE,
                TestConstants.KEY_CONTENT to TestConstants.NOTE_TEST_CONTENT,
                TestConstants.KEY_IS_PINNED to true
            )

            mockMvc.perform(
                put(TestConstants.BASE_URL_NOTES + "/" + noteId)
                    .header(TestConstants.HEADER_AUTHORIZATION, TestConstants.BEARER_PREFIX + authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath(TestConstants.JSON_PATH_IS_PINNED).value(true))
        }

        @Test
        fun `should return 404 when updating non-existent note`() {
            val request = mapOf(
                TestConstants.KEY_TITLE to TestConstants.NOTE_UPDATED_TITLE,
                TestConstants.KEY_CONTENT to TestConstants.NOTE_UPDATED_CONTENT
            )

            mockMvc.perform(
                put(TestConstants.BASE_URL_NOTES + "/999")
                    .header(TestConstants.HEADER_AUTHORIZATION, TestConstants.BEARER_PREFIX + authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isNotFound)
                .andExpect(jsonPath(TestConstants.JSON_PATH_ERROR).value(TestConstants.NOTE_NOT_FOUND_MSG + " 999"))
        }

        @Test
        fun `should return 404 when category not found on update`() {
            val noteId = createNote(TestConstants.NOTE_TEST_TITLE, TestConstants.NOTE_TEST_CONTENT)

            val request = mapOf(
                TestConstants.KEY_TITLE to TestConstants.NOTE_UPDATED_TITLE,
                TestConstants.KEY_CONTENT to TestConstants.NOTE_UPDATED_CONTENT,
                TestConstants.KEY_CATEGORY_ID to 999
            )

            mockMvc.perform(
                put(TestConstants.BASE_URL_NOTES + "/" + noteId)
                    .header(TestConstants.HEADER_AUTHORIZATION, TestConstants.BEARER_PREFIX + authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isNotFound)
                .andExpect(jsonPath(TestConstants.JSON_PATH_ERROR).value(TestConstants.CATEGORY_NOT_FOUND_MSG + " 999"))
        }

        @Test
        fun `should return 400 when title is empty on update`() {
            val noteId = createNote(TestConstants.NOTE_TEST_TITLE, TestConstants.NOTE_TEST_CONTENT)

            val request = mapOf(
                TestConstants.KEY_TITLE to ""
            )

            mockMvc.perform(
                put(TestConstants.BASE_URL_NOTES + "/" + noteId)
                    .header(TestConstants.HEADER_AUTHORIZATION, TestConstants.BEARER_PREFIX + authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath(TestConstants.JSON_PATH_ERRORS_TITLE).value(TestConstants.TITLE_REQUIRED_MSG))
        }

        @Test
        fun `should return 401 when not authenticated`() {
            val request = mapOf(
                TestConstants.KEY_TITLE to TestConstants.NOTE_UPDATED_TITLE
            )

            mockMvc.perform(
                put(TestConstants.BASE_URL_NOTES + "/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isForbidden)
        }
    }
}