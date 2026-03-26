package com.yoesuv.mynote.controller

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class NoteGetByIdTests : NoteTestBase() {

    @Nested
    inner class GetNoteById {
        @Test
        fun `should return note by id`() {
            val noteId = createNote(TestConstants.NOTE_TEST_TITLE, TestConstants.NOTE_TEST_CONTENT)

            mockMvc.perform(
                get(TestConstants.BASE_URL_NOTES + "/" + noteId)
                    .header(TestConstants.HEADER_AUTHORIZATION, TestConstants.BEARER_PREFIX + authToken)
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath(TestConstants.JSON_PATH_ID).value(noteId))
                .andExpect(jsonPath(TestConstants.JSON_PATH_TITLE).value(TestConstants.NOTE_TEST_TITLE))
                .andExpect(jsonPath(TestConstants.JSON_PATH_CONTENT).value(TestConstants.NOTE_TEST_CONTENT))
                .andExpect(jsonPath(TestConstants.JSON_PATH_IS_PINNED).value(false))
        }

        @Test
        fun `should return note with category`() {
            val categoryId = createCategory(TestConstants.CATEGORY_WORK, TestConstants.COLOR_RED)
            val noteId = createNoteWithCategory(TestConstants.NOTE_TEST_TITLE, TestConstants.NOTE_TEST_CONTENT, categoryId)

            mockMvc.perform(
                get(TestConstants.BASE_URL_NOTES + "/" + noteId)
                    .header(TestConstants.HEADER_AUTHORIZATION, TestConstants.BEARER_PREFIX + authToken)
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath(TestConstants.JSON_PATH_ID).value(noteId))
                .andExpect(jsonPath(TestConstants.JSON_PATH_CATEGORY_ID).value(categoryId))
                .andExpect(jsonPath(TestConstants.JSON_PATH_CATEGORY_NAME).value(TestConstants.CATEGORY_WORK))
        }

        @Test
        fun `should return 404 when note not found`() {
            mockMvc.perform(
                get(TestConstants.BASE_URL_NOTES + "/999")
                    .header(TestConstants.HEADER_AUTHORIZATION, TestConstants.BEARER_PREFIX + authToken)
            )
                .andExpect(status().isNotFound)
                .andExpect(jsonPath(TestConstants.JSON_PATH_ERROR).value(TestConstants.NOTE_NOT_FOUND_MSG + " 999"))
        }

        @Test
        fun `should return 401 when not authenticated`() {
            mockMvc.perform(get(TestConstants.BASE_URL_NOTES + "/1"))
                .andExpect(status().isForbidden)
        }
    }
}