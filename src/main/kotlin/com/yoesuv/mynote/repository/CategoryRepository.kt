package com.yoesuv.mynote.repository

import com.yoesuv.mynote.domain.Category
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface CategoryRepository : JpaRepository<Category, Long> {
    fun findByUserId(userId: Long): List<Category>
    fun findByIdAndUserId(id: Long, userId: Long): Category?

    @Query("""
        select count(c) > 0
        from Category c
        where c.user.id = :userId
          and lower(trim(c.name)) = lower(:name)
    """)
    fun existsByUserIdAndNormalizedName(
        @Param("userId") userId: Long,
        @Param("name") name: String
    ): Boolean

    @Query("""
        select count(c) > 0
        from Category c
        where c.user.id = :userId
          and lower(trim(c.name)) = lower(:name)
          and c.id <> :id
    """)
    fun existsByUserIdAndNormalizedNameAndIdNot(
        @Param("userId") userId: Long,
        @Param("name") name: String,
        @Param("id") id: Long
    ): Boolean
}
