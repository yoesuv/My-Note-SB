package com.yoesuv.mynote.dto.category

import java.time.LocalDateTime

data class CategoryResponse(
    val id: Long,
    val name: String,
    val color: String?,
    val userId: Long,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?
)