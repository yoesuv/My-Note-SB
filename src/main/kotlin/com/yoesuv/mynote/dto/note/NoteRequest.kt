package com.yoesuv.mynote.dto.note

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class NoteRequest(
    @field:NotBlank(message = "Title is required")
    @field:Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
    val title: String? = null,

    val content: String? = null,

    @field:JsonProperty("isPinned")
    val isPinned: Boolean = false,

    val categoryId: Long? = null
)