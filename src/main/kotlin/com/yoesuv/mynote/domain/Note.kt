package com.yoesuv.mynote.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "notes")
data class Note(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = true)
    var category: Category? = null,

    @Column(name = "title", nullable = false, length = 200)
    var title: String,

    @Column(name = "content", columnDefinition = "TEXT")
    var content: String? = null,

    @Column(name = "is_pinned", nullable = false)
    var isPinned: Boolean = false

) : BaseAuditEntity()