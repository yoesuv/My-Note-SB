package com.yoesuv.mynote.repository

import com.yoesuv.mynote.domain.Note
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface NoteRepository : JpaRepository<Note, Long> {
    fun findByCategoryId(categoryId: Long): List<Note>
    fun findByUserId(userId: Long): List<Note>
    fun findByIdAndUserId(id: Long, userId: Long): Note?
    fun findByUserIdAndCategoryId(userId: Long, categoryId: Long): List<Note>
}