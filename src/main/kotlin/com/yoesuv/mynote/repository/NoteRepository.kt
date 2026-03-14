package com.yoesuv.mynote.repository

import com.yoesuv.mynote.domain.Note
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface NoteRepository : JpaRepository<Note, Long> {
    fun findByCategoryId(categoryId: Long): List<Note>
}