package com.yoesuv.mynote.controller

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class NoteDeleteTests : NoteTestBase() {

    @Nested
    inner class DeleteNote {
        @Test
        fun `should delete note successfully`() {
            val noteId = createNote(TestConstants.NOTE_TEST_TITLE, TestConstants.NOTE_TEST_CONTENT)

            mockMvc.perform(
                delete(TestConstants.BASE_URL_NOTES + "/" + noteId)
                    .header(TestConstants.HEADER_AUTHORIZATION, TestConstants.BEARER_PREFIX + authToken)
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath(TestConstants.JSON_PATH_MESSAGE).value(TestConstants.NOTE_DELETED_MSG))

            mockMvc.perform(
                get(TestConstants.BASE_URL_NOTES + "/" + noteId)
                    .header(TestConstants.HEADER_AUTHORIZATION, TestConstants.BEARER_PREFIX + authToken)
            )
                .andExpect(status().isNotFound)
        }

        @Test
        fun `should return 404 when deleting non-existent note`() {
            mockMvc.perform(
                delete(TestConstants.BASE_URL_NOTES + "/999")
                    .header(TestConstants.HEADER_AUTHORIZATION, TestConstants.BEARER_PREFIX + authToken)
            )
                .andExpect(status().isNotFound)
                .andExpect(jsonPath(TestConstants.JSON_PATH_ERROR).value(TestConstants.NOTE_NOT_FOUND_MSG + " 999"))
        }

        @Test
        fun `should return 401 when not authenticated`() {
            mockMvc.perform(delete(TestConstants.BASE_URL_NOTES + "/1"))
                .andExpect(status().isForbidden)
        }
    }
}