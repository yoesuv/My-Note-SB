package com.yoesuv.mynote.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.yoesuv.mynote.repository.CategoryRepository
import com.yoesuv.mynote.repository.NoteRepository
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NoteTests {

    companion object {
        private const val BASE_URL = "/api/notes"
        private const val AUTH_URL = "/api/auth/register"
        private const val CATEGORY_URL = "/api/categories"
        private const val TEST_EMAIL = "test@example.com"
        private const val TEST_PASSWORD = "password123"
        private const val TEST_FULL_NAME = "Test User"
        private const val KEY_TITLE = "title"
        private const val KEY_CONTENT = "content"
        private const val KEY_IS_PINNED = "isPinned"
        private const val KEY_CATEGORY_ID = "categoryId"
        private const val KEY_NAME = "name"
        private const val KEY_COLOR = "color"
        private const val KEY_EMAIL = "email"
        private const val KEY_PASSWORD = "password"
        private const val KEY_FULL_NAME = "fullName"
        private const val JSON_PATH_ERROR = "$.error"
        private const val JSON_PATH_ERRORS_TITLE = "$.errors.title"
        private const val NOTE_TEST_TITLE = "Test Note"
        private const val NOTE_TEST_CONTENT = "Test Content"
        private const val NOTE_1_TITLE = "Note 1"
        private const val NOTE_2_TITLE = "Note 2"
        private const val NOTE_1_CONTENT = "Content 1"
        private const val NOTE_2_CONTENT = "Content 2"
        private const val NOTE_UPDATED_TITLE = "Updated Title"
        private const val NOTE_UPDATED_CONTENT = "Updated Content"
        private const val NOTE_ORIGINAL_TITLE = "Original Title"
        private const val NOTE_ORIGINAL_CONTENT = "Original Content"
        private const val NOTE_PINNED_TITLE = "Pinned Note"
        private const val NOTE_WITHOUT_CATEGORY = "Note without category"
        private const val NOTE_WITH_CATEGORY = "Note with category"
        private const val CATEGORY_WORK = "Work"
        private const val COLOR_RED = "#FF0000"
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var categoryRepository: CategoryRepository

    @Autowired
    private lateinit var noteRepository: NoteRepository

    private var authToken: String = ""

    @BeforeEach
    fun setup() {
        userRepository.deleteAll()
        categoryRepository.deleteAll()
        noteRepository.deleteAll()

        val registerRequest = mapOf(
            KEY_FULL_NAME to TEST_FULL_NAME,
            KEY_EMAIL to TEST_EMAIL,
            KEY_PASSWORD to TEST_PASSWORD
        )

        val result = mockMvc.perform(
            post(AUTH_URL)
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

    @Nested
    inner class GetAllNotes {
        @Test
        fun `should return empty list when no notes exist`() {
            mockMvc.perform(
                get(BASE_URL)
                    .header("Authorization", "Bearer $authToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath("$").isEmpty)
        }

        @Test
        fun `should return list of notes`() {
            createNote(NOTE_1_TITLE, NOTE_1_CONTENT)
            createNote(NOTE_2_TITLE, NOTE_2_CONTENT)

            mockMvc.perform(
                get(BASE_URL)
                    .header("Authorization", "Bearer $authToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value(NOTE_1_TITLE))
                .andExpect(jsonPath("$[1].title").value(NOTE_2_TITLE))
        }

        @Test
        fun `should filter notes by categoryId`() {
            val categoryId = createCategory(CATEGORY_WORK, COLOR_RED)
            createNote(NOTE_WITHOUT_CATEGORY, NOTE_1_CONTENT)
            createNoteWithCategory(NOTE_WITH_CATEGORY, NOTE_2_CONTENT, categoryId)

            mockMvc.perform(
                get("$BASE_URL?categoryId=$categoryId")
                    .header("Authorization", "Bearer $authToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value(NOTE_WITH_CATEGORY))
                .andExpect(jsonPath("$[0].category.id").value(categoryId))
        }

        @Test
        fun `should return empty list when category filter has no notes`() {
            val categoryId = createCategory(CATEGORY_WORK, COLOR_RED)
            createNote(NOTE_WITHOUT_CATEGORY, NOTE_1_CONTENT)

            mockMvc.perform(
                get("$BASE_URL?categoryId=$categoryId")
                    .header("Authorization", "Bearer $authToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath("$").isEmpty)
        }

        @Test
        fun `should return 401 when not authenticated`() {
            mockMvc.perform(get(BASE_URL))
                .andExpect(status().isForbidden)
        }
    }

    @Nested
    inner class GetNoteById {
        @Test
        fun `should return note by id`() {
            val noteId = createNote(NOTE_TEST_TITLE, NOTE_TEST_CONTENT)

            mockMvc.perform(
                get("$BASE_URL/$noteId")
                    .header("Authorization", "Bearer $authToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(noteId))
                .andExpect(jsonPath("$.title").value(NOTE_TEST_TITLE))
                .andExpect(jsonPath("$.content").value(NOTE_TEST_CONTENT))
                .andExpect(jsonPath("$.isPinned").value(false))
        }

        @Test
        fun `should return note with category`() {
            val categoryId = createCategory(CATEGORY_WORK, COLOR_RED)
            val noteId = createNoteWithCategory(NOTE_TEST_TITLE, NOTE_TEST_CONTENT, categoryId)

            mockMvc.perform(
                get("$BASE_URL/$noteId")
                    .header("Authorization", "Bearer $authToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(noteId))
                .andExpect(jsonPath("$.category.id").value(categoryId))
                .andExpect(jsonPath("$.category.name").value(CATEGORY_WORK))
        }

        @Test
        fun `should return 404 when note not found`() {
            mockMvc.perform(
                get("$BASE_URL/999")
                    .header("Authorization", "Bearer $authToken")
            )
                .andExpect(status().isNotFound)
                .andExpect(jsonPath(JSON_PATH_ERROR).value("Note not found with id: 999"))
        }

        @Test
        fun `should return 401 when not authenticated`() {
            mockMvc.perform(get("$BASE_URL/1"))
                .andExpect(status().isForbidden)
        }
    }

    @Nested
    inner class CreateNote {
        @Test
        fun `should create note successfully`() {
            val request = mapOf(
                KEY_TITLE to NOTE_TEST_TITLE,
                KEY_CONTENT to NOTE_TEST_CONTENT
            )

            mockMvc.perform(
                post(BASE_URL)
                    .header("Authorization", "Bearer $authToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value(NOTE_TEST_TITLE))
                .andExpect(jsonPath("$.content").value(NOTE_TEST_CONTENT))
                .andExpect(jsonPath("$.isPinned").value(false))
                .andExpect(jsonPath("$.userId").exists())
        }

        @Test
        fun `should create note with category`() {
            val categoryId = createCategory(CATEGORY_WORK, COLOR_RED)

            val request = mapOf(
                KEY_TITLE to NOTE_TEST_TITLE,
                KEY_CONTENT to NOTE_TEST_CONTENT,
                KEY_CATEGORY_ID to categoryId
            )

            mockMvc.perform(
                post(BASE_URL)
                    .header("Authorization", "Bearer $authToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value(NOTE_TEST_TITLE))
                .andExpect(jsonPath("$.category.id").value(categoryId))
                .andExpect(jsonPath("$.category.name").value(CATEGORY_WORK))
        }

        @Test
        fun `should create note with isPinned true`() {
            val request = mapOf(
                KEY_TITLE to NOTE_PINNED_TITLE,
                KEY_CONTENT to NOTE_TEST_CONTENT,
                KEY_IS_PINNED to true
            )

            mockMvc.perform(
                post(BASE_URL)
                    .header("Authorization", "Bearer $authToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.isPinned").value(true))
        }

        @Test
        fun `should create note without content`() {
            val request = mapOf(
                KEY_TITLE to NOTE_TEST_TITLE
            )

            mockMvc.perform(
                post(BASE_URL)
                    .header("Authorization", "Bearer $authToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value(NOTE_TEST_TITLE))
                .andExpect(jsonPath("$.content").doesNotExist())
        }

        @Test
        fun `should return 404 when category not found`() {
            val request = mapOf(
                KEY_TITLE to NOTE_TEST_TITLE,
                KEY_CONTENT to NOTE_TEST_CONTENT,
                KEY_CATEGORY_ID to 999
            )

            mockMvc.perform(
                post(BASE_URL)
                    .header("Authorization", "Bearer $authToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isNotFound)
                .andExpect(jsonPath(JSON_PATH_ERROR).value("Category not found with id: 999"))
        }

        @Test
        fun `should return 400 when title is empty`() {
            val request = mapOf(
                KEY_TITLE to "",
                KEY_CONTENT to NOTE_TEST_CONTENT
            )

            mockMvc.perform(
                post(BASE_URL)
                    .header("Authorization", "Bearer $authToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath(JSON_PATH_ERRORS_TITLE).value("Title is required"))
        }

        @Test
        fun `should return 400 when title exceeds max length`() {
            val request = mapOf(
                KEY_TITLE to "A".repeat(201),
                KEY_CONTENT to NOTE_TEST_CONTENT
            )

            mockMvc.perform(
                post(BASE_URL)
                    .header("Authorization", "Bearer $authToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath(JSON_PATH_ERRORS_TITLE).value("Title must be between 1 and 200 characters"))
        }

        @Test
        fun `should return 400 when title is missing`() {
            val request = mapOf(
                KEY_CONTENT to NOTE_TEST_CONTENT
            )

            mockMvc.perform(
                post(BASE_URL)
                    .header("Authorization", "Bearer $authToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath(JSON_PATH_ERRORS_TITLE).value("Title is required"))
        }

        @Test
        fun `should return 400 when JSON format is invalid`() {
            mockMvc.perform(
                post(BASE_URL)
                    .header("Authorization", "Bearer $authToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("not-valid-json")
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath(JSON_PATH_ERROR).value("Invalid JSON format"))
        }

        @Test
        fun `should return 401 when not authenticated`() {
            val request = mapOf(
                KEY_TITLE to NOTE_TEST_TITLE,
                KEY_CONTENT to NOTE_TEST_CONTENT
            )

            mockMvc.perform(
                post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isForbidden)
        }
    }

    @Nested
    inner class UpdateNote {
        @Test
        fun `should update note successfully`() {
            val noteId = createNote(NOTE_ORIGINAL_TITLE, NOTE_ORIGINAL_CONTENT)

            val request = mapOf(
                KEY_TITLE to NOTE_UPDATED_TITLE,
                KEY_CONTENT to NOTE_UPDATED_CONTENT
            )

            mockMvc.perform(
                put("$BASE_URL/$noteId")
                    .header("Authorization", "Bearer $authToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(noteId))
                .andExpect(jsonPath("$.title").value(NOTE_UPDATED_TITLE))
                .andExpect(jsonPath("$.content").value(NOTE_UPDATED_CONTENT))
        }

        @Test
        fun `should update note with category`() {
            val categoryId = createCategory(CATEGORY_WORK, COLOR_RED)
            val noteId = createNote(NOTE_ORIGINAL_TITLE, NOTE_ORIGINAL_CONTENT)

            val request = mapOf(
                KEY_TITLE to NOTE_UPDATED_TITLE,
                KEY_CONTENT to NOTE_UPDATED_CONTENT,
                KEY_CATEGORY_ID to categoryId
            )

            mockMvc.perform(
                put("$BASE_URL/$noteId")
                    .header("Authorization", "Bearer $authToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.category.id").value(categoryId))
        }

        @Test
        fun `should update isPinned`() {
            val noteId = createNote(NOTE_TEST_TITLE, NOTE_TEST_CONTENT)

            val request = mapOf(
                KEY_TITLE to NOTE_TEST_TITLE,
                KEY_CONTENT to NOTE_TEST_CONTENT,
                KEY_IS_PINNED to true
            )

            mockMvc.perform(
                put("$BASE_URL/$noteId")
                    .header("Authorization", "Bearer $authToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.isPinned").value(true))
        }

        @Test
        fun `should return 404 when updating non-existent note`() {
            val request = mapOf(
                KEY_TITLE to NOTE_UPDATED_TITLE,
                KEY_CONTENT to NOTE_UPDATED_CONTENT
            )

            mockMvc.perform(
                put("$BASE_URL/999")
                    .header("Authorization", "Bearer $authToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isNotFound)
                .andExpect(jsonPath(JSON_PATH_ERROR).value("Note not found with id: 999"))
        }

        @Test
        fun `should return 404 when category not found on update`() {
            val noteId = createNote(NOTE_TEST_TITLE, NOTE_TEST_CONTENT)

            val request = mapOf(
                KEY_TITLE to NOTE_UPDATED_TITLE,
                KEY_CONTENT to NOTE_UPDATED_CONTENT,
                KEY_CATEGORY_ID to 999
            )

            mockMvc.perform(
                put("$BASE_URL/$noteId")
                    .header("Authorization", "Bearer $authToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isNotFound)
                .andExpect(jsonPath(JSON_PATH_ERROR).value("Category not found with id: 999"))
        }

        @Test
        fun `should return 400 when title is empty on update`() {
            val noteId = createNote(NOTE_TEST_TITLE, NOTE_TEST_CONTENT)

            val request = mapOf(
                KEY_TITLE to ""
            )

            mockMvc.perform(
                put("$BASE_URL/$noteId")
                    .header("Authorization", "Bearer $authToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath(JSON_PATH_ERRORS_TITLE).value("Title is required"))
        }

        @Test
        fun `should return 401 when not authenticated`() {
            val request = mapOf(
                KEY_TITLE to NOTE_UPDATED_TITLE
            )

            mockMvc.perform(
                put("$BASE_URL/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isForbidden)
        }
    }

    @Nested
    inner class DeleteNote {
        @Test
        fun `should delete note successfully`() {
            val noteId = createNote(NOTE_TEST_TITLE, NOTE_TEST_CONTENT)

            mockMvc.perform(
                delete("$BASE_URL/$noteId")
                    .header("Authorization", "Bearer $authToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.message").value("Note deleted successfully"))

            mockMvc.perform(
                get("$BASE_URL/$noteId")
                    .header("Authorization", "Bearer $authToken")
            )
                .andExpect(status().isNotFound)
        }

        @Test
        fun `should return 404 when deleting non-existent note`() {
            mockMvc.perform(
                delete("$BASE_URL/999")
                    .header("Authorization", "Bearer $authToken")
            )
                .andExpect(status().isNotFound)
                .andExpect(jsonPath(JSON_PATH_ERROR).value("Note not found with id: 999"))
        }

        @Test
        fun `should return 401 when not authenticated`() {
            mockMvc.perform(delete("$BASE_URL/1"))
                .andExpect(status().isForbidden)
        }
    }

    private fun createCategory(name: String, color: String): Long {
        val request = mapOf(
            KEY_NAME to name,
            KEY_COLOR to color
        )

        val result = mockMvc.perform(
            post(CATEGORY_URL)
                .header("Authorization", "Bearer $authToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andReturn()

        val response = objectMapper.readTree(result.response.contentAsString)
        return response.get("id").asLong()
    }

    private fun createNote(title: String, content: String): Long {
        val request = mapOf(
            KEY_TITLE to title,
            KEY_CONTENT to content
        )

        val result = mockMvc.perform(
            post(BASE_URL)
                .header("Authorization", "Bearer $authToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andReturn()

        val response = objectMapper.readTree(result.response.contentAsString)
        return response.get("id").asLong()
    }

    private fun createNoteWithCategory(title: String, content: String, categoryId: Long): Long {
        val request = mapOf(
            KEY_TITLE to title,
            KEY_CONTENT to content,
            KEY_CATEGORY_ID to categoryId
        )

        val result = mockMvc.perform(
            post(BASE_URL)
                .header("Authorization", "Bearer $authToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andReturn()

        val response = objectMapper.readTree(result.response.contentAsString)
        return response.get("id").asLong()
    }
}