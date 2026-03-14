package com.yoesuv.mynote.repository

import com.yoesuv.mynote.domain.Category
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CategoryRepository : JpaRepository<Category, Long> {
    fun findByUserId(userId: Long): List<Category>
    fun findByIdAndUserId(id: Long, userId: Long): Category?
    fun existsByUserIdAndName(userId: Long, name: String): Boolean
    fun existsByUserIdAndNameAndIdNot(userId: Long, name: String, id: Long): Boolean
}