package com.yoesuv.mynote.dto.category

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CategoryRequest(
    @field:NotBlank(message = "Category name is required")
    @field:Size(min = 2, max = 100, message = "Category name must be between 2 and 100 characters")
    val name: String? = null,

    @field:Size(max = 7, message = "Color must be at most 7 characters")
    val color: String? = null
)