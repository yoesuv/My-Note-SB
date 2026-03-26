package com.yoesuv.mynote.controller

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class NoteGetAllTests : NoteTestBase() {

    @Nested
    inner class GetAllNotes {
        @Test
        fun `should return empty list when no notes exist`() {
            mockMvc.perform(
                get(TestConstants.BASE_URL_NOTES)
                    .header(TestConstants.HEADER_AUTHORIZATION, TestConstants.BEARER_PREFIX + authToken)
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath(TestConstants.JSON_PATH_ROOT).isArray)
                .andExpect(jsonPath(TestConstants.JSON_PATH_ROOT).isEmpty)
        }

        @Test
        fun `should return list of notes`() {
            createNote(TestConstants.NOTE_1_TITLE, TestConstants.NOTE_1_CONTENT)
            createNote(TestConstants.NOTE_2_TITLE, TestConstants.NOTE_2_CONTENT)

            mockMvc.perform(
                get(TestConstants.BASE_URL_NOTES)
                    .header(TestConstants.HEADER_AUTHORIZATION, TestConstants.BEARER_PREFIX + authToken)
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath(TestConstants.JSON_PATH_ROOT).isArray)
                .andExpect(jsonPath(TestConstants.JSON_PATH_ROOT + ".length()").value(2))
                .andExpect(jsonPath(TestConstants.JSON_PATH_FIRST_TITLE).value(TestConstants.NOTE_1_TITLE))
                .andExpect(jsonPath(TestConstants.JSON_PATH_SECOND_TITLE).value(TestConstants.NOTE_2_TITLE))
        }

        @Test
        fun `should filter notes by categoryId`() {
            val categoryId = createCategory(TestConstants.CATEGORY_WORK, TestConstants.COLOR_RED)
            createNote(TestConstants.NOTE_WITHOUT_CATEGORY, TestConstants.NOTE_1_CONTENT)
            createNoteWithCategory(TestConstants.NOTE_WITH_CATEGORY, TestConstants.NOTE_2_CONTENT, categoryId)

            mockMvc.perform(
                get(TestConstants.BASE_URL_NOTES + "?categoryId=" + categoryId)
                    .header(TestConstants.HEADER_AUTHORIZATION, TestConstants.BEARER_PREFIX + authToken)
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath(TestConstants.JSON_PATH_ROOT).isArray)
                .andExpect(jsonPath(TestConstants.JSON_PATH_ROOT + ".length()").value(1))
                .andExpect(jsonPath(TestConstants.JSON_PATH_FIRST_TITLE).value(TestConstants.NOTE_WITH_CATEGORY))
                .andExpect(jsonPath(TestConstants.JSON_PATH_FIRST_CATEGORY_ID).value(categoryId))
        }

        @Test
        fun `should return empty list when category filter has no notes`() {
            val categoryId = createCategory(TestConstants.CATEGORY_WORK, TestConstants.COLOR_RED)
            createNote(TestConstants.NOTE_WITHOUT_CATEGORY, TestConstants.NOTE_1_CONTENT)

            mockMvc.perform(
                get(TestConstants.BASE_URL_NOTES + "?categoryId=" + categoryId)
                    .header(TestConstants.HEADER_AUTHORIZATION, TestConstants.BEARER_PREFIX + authToken)
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath(TestConstants.JSON_PATH_ROOT).isArray)
                .andExpect(jsonPath(TestConstants.JSON_PATH_ROOT).isEmpty)
        }

        @Test
        fun `should return 401 when not authenticated`() {
            mockMvc.perform(get(TestConstants.BASE_URL_NOTES))
                .andExpect(status().isForbidden)
        }
    }
}