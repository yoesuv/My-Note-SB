package com.yoesuv.mynote.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.yoesuv.mynote.repository.CategoryRepository
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
class CategoryTests {

    companion object {
        private const val BASE_URL = "/api/categories"
        private const val AUTH_URL = "/api/auth/register"
        private const val TEST_EMAIL = "test@example.com"
        private const val TEST_PASSWORD = "password123"
        private const val TEST_FULL_NAME = "Test User"
        private const val KEY_NAME = "name"
        private const val KEY_COLOR = "color"
        private const val KEY_EMAIL = "email"
        private const val KEY_PASSWORD = "password"
        private const val KEY_FULL_NAME = "fullName"
        private const val JSON_PATH_ERROR = "$.error"
        private const val JSON_PATH_ERRORS_NAME = "$.errors.name"
        private const val JSON_PATH_ERRORS_COLOR = "$.errors.color"
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var categoryRepository: CategoryRepository

    private var authToken: String = ""

    @BeforeEach
    fun setup() {
        userRepository.deleteAll()
        categoryRepository.deleteAll()

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
        categoryRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Nested
    inner class GetAllCategories {
        @Test
        fun `should return empty list when no categories exist`() {
            mockMvc.perform(
                get(BASE_URL)
                    .header("Authorization", "Bearer $authToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath("$").isEmpty)
        }

        @Test
        fun `should return list of categories`() {
            createCategory("Work", "#FF0000")
            createCategory("Personal", "#00FF00")

            mockMvc.perform(
                get(BASE_URL)
                    .header("Authorization", "Bearer $authToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Work"))
                .andExpect(jsonPath("$[1].name").value("Personal"))
        }

        @Test
        fun `should return 401 when not authenticated`() {
            mockMvc.perform(get(BASE_URL))
                .andExpect(status().isForbidden)
        }
    }

    @Nested
    inner class GetCategoryById {
        @Test
        fun `should return category by id`() {
            val categoryId = createCategory("Work", "#FF0000")

            mockMvc.perform(
                get("$BASE_URL/$categoryId")
                    .header("Authorization", "Bearer $authToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(categoryId))
                .andExpect(jsonPath("$.name").value("Work"))
                .andExpect(jsonPath("$.color").value("#FF0000"))
        }

        @Test
        fun `should return 404 when category not found`() {
            mockMvc.perform(
                get("$BASE_URL/999")
                    .header("Authorization", "Bearer $authToken")
            )
                .andExpect(status().isNotFound)
                .andExpect(jsonPath(JSON_PATH_ERROR).value("Category not found with id: 999"))
        }

        @Test
        fun `should return 401 when not authenticated`() {
            mockMvc.perform(get("$BASE_URL/1"))
                .andExpect(status().isForbidden)
        }
    }

    @Nested
    inner class CreateCategory {
        @Test
        fun `should create category successfully`() {
            val request = mapOf(
                KEY_NAME to "Work",
                KEY_COLOR to "#FF0000"
            )

            mockMvc.perform(
                post(BASE_URL)
                    .header("Authorization", "Bearer $authToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Work"))
                .andExpect(jsonPath("$.color").value("#FF0000"))
                .andExpect(jsonPath("$.userId").exists())
        }

        @Test
        fun `should create category without color`() {
            val request = mapOf(
                KEY_NAME to "Personal"
            )

            mockMvc.perform(
                post(BASE_URL)
                    .header("Authorization", "Bearer $authToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Personal"))
                .andExpect(jsonPath("$.color").doesNotExist())
        }

        @Test
        fun `should return 409 when category name already exists`() {
            createCategory("Work", "#FF0000")

            val request = mapOf(
                KEY_NAME to "Work",
                KEY_COLOR to "#00FF00"
            )

            mockMvc.perform(
                post(BASE_URL)
                    .header("Authorization", "Bearer $authToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isConflict)
                .andExpect(jsonPath(JSON_PATH_ERROR).value("Category already exists with name: Work"))
        }

        @Test
        fun `should return 400 when name is empty`() {
            val request = mapOf(
                KEY_NAME to "",
                KEY_COLOR to "#FF0000"
            )

            mockMvc.perform(
                post(BASE_URL)
                    .header("Authorization", "Bearer $authToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath(JSON_PATH_ERRORS_NAME).value("Category name is required"))
        }

        @Test
        fun `should return 400 when name is too short`() {
            val request = mapOf(
                KEY_NAME to "A",
                KEY_COLOR to "#FF0000"
            )

            mockMvc.perform(
                post(BASE_URL)
                    .header("Authorization", "Bearer $authToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath(JSON_PATH_ERRORS_NAME).value("Category name must be between 2 and 100 characters"))
        }

        @Test
        fun `should return 400 when name is too long`() {
            val request = mapOf(
                KEY_NAME to "A".repeat(101),
                KEY_COLOR to "#FF0000"
            )

            mockMvc.perform(
                post(BASE_URL)
                    .header("Authorization", "Bearer $authToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath(JSON_PATH_ERRORS_NAME).value("Category name must be between 2 and 100 characters"))
        }

        @Test
        fun `should return 400 when color is too long`() {
            val request = mapOf(
                KEY_NAME to "Work",
                KEY_COLOR to "A".repeat(21)
            )

            mockMvc.perform(
                post(BASE_URL)
                    .header("Authorization", "Bearer $authToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath(JSON_PATH_ERRORS_COLOR).value("Color must be at most 20 characters"))
        }

        @Test
        fun `should return 400 when name is missing`() {
            val request = mapOf(
                KEY_COLOR to "#FF0000"
            )

            mockMvc.perform(
                post(BASE_URL)
                    .header("Authorization", "Bearer $authToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath(JSON_PATH_ERRORS_NAME).value("Category name is required"))
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
                KEY_NAME to "Work",
                KEY_COLOR to "#FF0000"
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
    inner class UpdateCategory {
        @Test
        fun `should update category successfully`() {
            val categoryId = createCategory("Work", "#FF0000")

            val request = mapOf(
                KEY_NAME to "Work Updated",
                KEY_COLOR to "#00FF00"
            )

            mockMvc.perform(
                put("$BASE_URL/$categoryId")
                    .header("Authorization", "Bearer $authToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(categoryId))
                .andExpect(jsonPath("$.name").value("Work Updated"))
                .andExpect(jsonPath("$.color").value("#00FF00"))
        }

        @Test
        fun `should update category without changing color`() {
            val categoryId = createCategory("Work", "#FF0000")

            val request = mapOf(
                KEY_NAME to "Work Updated"
            )

            mockMvc.perform(
                put("$BASE_URL/$categoryId")
                    .header("Authorization", "Bearer $authToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.name").value("Work Updated"))
        }

        @Test
        fun `should return 404 when updating non-existent category`() {
            val request = mapOf(
                KEY_NAME to "Work Updated",
                KEY_COLOR to "#00FF00"
            )

            mockMvc.perform(
                put("$BASE_URL/999")
                    .header("Authorization", "Bearer $authToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isNotFound)
                .andExpect(jsonPath(JSON_PATH_ERROR).value("Category not found with id: 999"))
        }

        @Test
        fun `should return 409 when updating to existing category name`() {
            createCategory("Personal", "#00FF00")
            val categoryId = createCategory("Work", "#FF0000")

            val request = mapOf(
                KEY_NAME to "Personal"
            )

            mockMvc.perform(
                put("$BASE_URL/$categoryId")
                    .header("Authorization", "Bearer $authToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isConflict)
                .andExpect(jsonPath(JSON_PATH_ERROR).value("Category already exists with name: Personal"))
        }

        @Test
        fun `should return 400 when name is empty on update`() {
            val categoryId = createCategory("Work", "#FF0000")

            val request = mapOf(
                KEY_NAME to ""
            )

            mockMvc.perform(
                put("$BASE_URL/$categoryId")
                    .header("Authorization", "Bearer $authToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath(JSON_PATH_ERRORS_NAME).value("Category name is required"))
        }

        @Test
        fun `should return 401 when not authenticated`() {
            val request = mapOf(
                KEY_NAME to "Work Updated"
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
    inner class DeleteCategory {
        @Test
        fun `should delete category successfully`() {
            val categoryId = createCategory("Work", "#FF0000")

            mockMvc.perform(
                delete("$BASE_URL/$categoryId")
                    .header("Authorization", "Bearer $authToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.message").value("Category deleted successfully"))

            mockMvc.perform(
                get("$BASE_URL/$categoryId")
                    .header("Authorization", "Bearer $authToken")
            )
                .andExpect(status().isNotFound)
        }

        @Test
        fun `should return 404 when deleting non-existent category`() {
            mockMvc.perform(
                delete("$BASE_URL/999")
                    .header("Authorization", "Bearer $authToken")
            )
                .andExpect(status().isNotFound)
                .andExpect(jsonPath(JSON_PATH_ERROR).value("Category not found with id: 999"))
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
            post(BASE_URL)
                .header("Authorization", "Bearer $authToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andReturn()

        val response = objectMapper.readTree(result.response.contentAsString)
        return response.get("id").asLong()
    }
}