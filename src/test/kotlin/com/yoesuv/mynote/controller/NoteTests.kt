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
            createNote("Note 1", "Content 1")
            createNote("Note 2", "Content 2")

            mockMvc.perform(
                get(BASE_URL)
                    .header("Authorization", "Bearer $authToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Note 1"))
                .andExpect(jsonPath("$[1].title").value("Note 2"))
        }

        @Test
        fun `should filter notes by categoryId`() {
            val categoryId = createCategory("Work", "#FF0000")
            createNote("Note without category", "Content 1")
            createNoteWithCategory("Note with category", "Content 2", categoryId)

            mockMvc.perform(
                get("$BASE_URL?categoryId=$categoryId")
                    .header("Authorization", "Bearer $authToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Note with category"))
                .andExpect(jsonPath("$[0].category.id").value(categoryId))
        }

        @Test
        fun `should return empty list when category filter has no notes`() {
            val categoryId = createCategory("Work", "#FF0000")
            createNote("Note without category", "Content 1")

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
            val noteId = createNote("Test Note", "Test Content")

            mockMvc.perform(
                get("$BASE_URL/$noteId")
                    .header("Authorization", "Bearer $authToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(noteId))
                .andExpect(jsonPath("$.title").value("Test Note"))
                .andExpect(jsonPath("$.content").value("Test Content"))
                .andExpect(jsonPath("$.isPinned").value(false))
        }

        @Test
        fun `should return note with category`() {
            val categoryId = createCategory("Work", "#FF0000")
            val noteId = createNoteWithCategory("Test Note", "Test Content", categoryId)

            mockMvc.perform(
                get("$BASE_URL/$noteId")
                    .header("Authorization", "Bearer $authToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(noteId))
                .andExpect(jsonPath("$.category.id").value(categoryId))
                .andExpect(jsonPath("$.category.name").value("Work"))
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
                KEY_TITLE to "Test Note",
                KEY_CONTENT to "Test Content"
            )

            mockMvc.perform(
                post(BASE_URL)
                    .header("Authorization", "Bearer $authToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Test Note"))
                .andExpect(jsonPath("$.content").value("Test Content"))
                .andExpect(jsonPath("$.isPinned").value(false))
                .andExpect(jsonPath("$.userId").exists())
        }

        @Test
        fun `should create note with category`() {
            val categoryId = createCategory("Work", "#FF0000")

            val request = mapOf(
                KEY_TITLE to "Test Note",
                KEY_CONTENT to "Test Content",
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
                .andExpect(jsonPath("$.title").value("Test Note"))
                .andExpect(jsonPath("$.category.id").value(categoryId))
                .andExpect(jsonPath("$.category.name").value("Work"))
        }

        @Test
        fun `should create note with isPinned true`() {
            val request = mapOf(
                KEY_TITLE to "Pinned Note",
                KEY_CONTENT to "Test Content",
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
                KEY_TITLE to "Test Note"
            )

            mockMvc.perform(
                post(BASE_URL)
                    .header("Authorization", "Bearer $authToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Test Note"))
                .andExpect(jsonPath("$.content").doesNotExist())
        }

        @Test
        fun `should return 404 when category not found`() {
            val request = mapOf(
                KEY_TITLE to "Test Note",
                KEY_CONTENT to "Test Content",
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
                KEY_CONTENT to "Test Content"
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
                KEY_CONTENT to "Test Content"
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
                KEY_CONTENT to "Test Content"
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
                KEY_TITLE to "Test Note",
                KEY_CONTENT to "Test Content"
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
            val noteId = createNote("Original Title", "Original Content")

            val request = mapOf(
                KEY_TITLE to "Updated Title",
                KEY_CONTENT to "Updated Content"
            )

            mockMvc.perform(
                put("$BASE_URL/$noteId")
                    .header("Authorization", "Bearer $authToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(noteId))
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.content").value("Updated Content"))
        }

        @Test
        fun `should update note with category`() {
            val categoryId = createCategory("Work", "#FF0000")
            val noteId = createNote("Original Title", "Original Content")

            val request = mapOf(
                KEY_TITLE to "Updated Title",
                KEY_CONTENT to "Updated Content",
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
            val noteId = createNote("Test Note", "Test Content")

            val request = mapOf(
                KEY_TITLE to "Test Note",
                KEY_CONTENT to "Test Content",
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
                KEY_TITLE to "Updated Title",
                KEY_CONTENT to "Updated Content"
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
            val noteId = createNote("Test Note", "Test Content")

            val request = mapOf(
                KEY_TITLE to "Updated Title",
                KEY_CONTENT to "Updated Content",
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
            val noteId = createNote("Test Note", "Test Content")

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
                KEY_TITLE to "Updated Title"
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
            val noteId = createNote("Test Note", "Test Content")

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