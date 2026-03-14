package com.yoesuv.mynote.service

import com.yoesuv.mynote.domain.Note
import com.yoesuv.mynote.dto.category.CategoryResponse
import com.yoesuv.mynote.dto.note.NoteRequest
import com.yoesuv.mynote.dto.note.NoteResponse
import com.yoesuv.mynote.exception.errors.EntityNotFoundException
import com.yoesuv.mynote.repository.CategoryRepository
import com.yoesuv.mynote.repository.NoteRepository
import com.yoesuv.mynote.repository.UserRepository
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NoteService(
    private val noteRepository: NoteRepository,
    private val categoryRepository: CategoryRepository,
    private val userRepository: UserRepository
) {

    fun getAllNotes(categoryId: Long?): List<NoteResponse> {
        val user = getCurrentUser()
        val notes = if (categoryId != null) {
            noteRepository.findByUserIdAndCategoryId(user.id!!, categoryId)
        } else {
            noteRepository.findByUserId(user.id!!)
        }
        return notes.map { it.toResponse() }
    }

    fun getNoteById(id: Long): NoteResponse {
        val user = getCurrentUser()
        val note = noteRepository.findByIdAndUserId(id, user.id!!)
            ?: throw EntityNotFoundException("Note", id)
        return note.toResponse()
    }

    @Transactional
    fun createNote(request: NoteRequest): NoteResponse {
        val user = getCurrentUser()
        val category = request.categoryId?.let { categoryId ->
            categoryRepository.findByIdAndUserId(categoryId, user.id!!)
                ?: throw EntityNotFoundException("Category", categoryId)
        }

        val note = Note(
            user = user,
            title = request.title!!.trim(),
            content = request.content?.trim(),
            isPinned = request.isPinned,
            category = category
        )

        val savedNote = noteRepository.save(note)
        return savedNote.toResponse()
    }

    @Transactional
    fun updateNote(id: Long, request: NoteRequest): NoteResponse {
        val user = getCurrentUser()
        val note = noteRepository.findByIdAndUserId(id, user.id!!)
            ?: throw EntityNotFoundException("Note", id)

        val category = request.categoryId?.let { categoryId ->
            categoryRepository.findByIdAndUserId(categoryId, user.id!!)
                ?: throw EntityNotFoundException("Category", categoryId)
        }

        note.title = request.title!!.trim()
        note.content = request.content?.trim()
        note.isPinned = request.isPinned
        note.category = category

        val updatedNote = noteRepository.save(note)
        return updatedNote.toResponse()
    }

    @Transactional
    fun deleteNote(id: Long) {
        val user = getCurrentUser()
        val note = noteRepository.findByIdAndUserId(id, user.id!!)
            ?: throw EntityNotFoundException("Note", id)
        noteRepository.delete(note)
    }

    private fun getCurrentUser(): com.yoesuv.mynote.domain.User {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw IllegalStateException("No authentication found in security context")

        val email = authentication.name
        return userRepository.findByEmail(email)
            ?: throw EntityNotFoundException("User", email)
    }

    private fun Note.toResponse(): NoteResponse {
        return NoteResponse(
            id = id!!,
            title = title,
            content = content,
            isPinned = isPinned,
            category = category?.toCategoryResponse(),
            userId = user.id!!,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    private fun com.yoesuv.mynote.domain.Category.toCategoryResponse(): CategoryResponse {
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