package com.yoesuv.mynote.service

import com.yoesuv.mynote.domain.Category
import com.yoesuv.mynote.dto.category.CategoryRequest
import com.yoesuv.mynote.dto.category.CategoryResponse
import com.yoesuv.mynote.exception.errors.CategoryAlreadyExistsException
import com.yoesuv.mynote.exception.errors.EntityNotFoundException
import com.yoesuv.mynote.repository.CategoryRepository
import com.yoesuv.mynote.repository.NoteRepository
import com.yoesuv.mynote.repository.UserRepository
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CategoryService(
    private val categoryRepository: CategoryRepository,
    private val noteRepository: NoteRepository,
    private val userRepository: UserRepository
) {

    fun getAllCategories(): List<CategoryResponse> {
        val user = getCurrentUser()
        val categories = categoryRepository.findByUserId(user.id!!)
        return categories.map { it.toResponse() }
    }

    fun getCategoryById(id: Long): CategoryResponse {
        val user = getCurrentUser()
        val category = categoryRepository.findByIdAndUserId(id, user.id!!)
            ?: throw EntityNotFoundException("Category", id)
        return category.toResponse()
    }

    @Transactional
    fun createCategory(request: CategoryRequest): CategoryResponse {
        val user = getCurrentUser()
        val name = request.name!!.trim()
        val color = request.color?.trim()

        if (categoryRepository.existsByUserIdAndName(user.id!!, name)) {
            throw CategoryAlreadyExistsException(name)
        }

        val category = Category(
            user = user,
            name = name,
            color = color
        )

        val savedCategory = categoryRepository.save(category)
        return savedCategory.toResponse()
    }

    @Transactional
    fun updateCategory(id: Long, request: CategoryRequest): CategoryResponse {
        val user = getCurrentUser()
        val userId = user.id!!
        val category = categoryRepository.findByIdAndUserId(id, userId)
            ?: throw EntityNotFoundException("Category", id)

        val newName = request.name!!.trim()
        val newColor = request.color?.trim()

        if (categoryRepository.existsByUserIdAndNameAndIdNot(userId, newName, id)) {
            throw CategoryAlreadyExistsException(newName)
        }

        category.name = newName
        category.color = newColor

        val updatedCategory = categoryRepository.save(category)
        return updatedCategory.toResponse()
    }

    @Transactional
    fun deleteCategory(id: Long) {
        val user = getCurrentUser()
        val category = categoryRepository.findByIdAndUserId(id, user.id!!)
            ?: throw EntityNotFoundException("Category", id)

        val notes = noteRepository.findByCategoryId(id)
        notes.forEach { note ->
            note.category = null
        }
        noteRepository.saveAll(notes)

        categoryRepository.delete(category)
    }

    private fun getCurrentUser(): com.yoesuv.mynote.domain.User {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw IllegalStateException("No authentication found in security context")

        val email = authentication.name
        return userRepository.findByEmail(email)
            ?: throw EntityNotFoundException("User", email)
    }

    private fun Category.toResponse(): CategoryResponse {
        return CategoryResponse(
            id = id!!,
            name = name,
            color = color,
            userId = user.id!!,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}