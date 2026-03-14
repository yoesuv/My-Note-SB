package com.yoesuv.mynote.dto.note

import com.yoesuv.mynote.dto.category.CategoryResponse
import java.time.LocalDateTime

data class NoteResponse(
    val id: Long,
    val title: String,
    val content: String?,
    val isPinned: Boolean,
    val category: CategoryResponse?,
    val userId: Long,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)