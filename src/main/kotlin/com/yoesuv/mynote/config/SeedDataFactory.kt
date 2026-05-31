package com.yoesuv.mynote.config

import com.yoesuv.mynote.domain.Category
import com.yoesuv.mynote.domain.Note
import com.yoesuv.mynote.domain.User

data class SeededUsers(
    val demoUser: User,
    val johnUser: User
)

data class SeededCategories(
    val workCategory: Category,
    val personalCategory: Category,
    val ideasCategory: Category,
    val studyCategory: Category,
    val fitnessCategory: Category
)

object SeedDataFactory {

    fun notes(users: SeededUsers, categories: SeededCategories): List<Note> {
        return demoNotes(users, categories) + johnNotes(users, categories)
    }

    private fun demoNotes(users: SeededUsers, categories: SeededCategories): List<Note> = listOf(
        note(users.demoUser, "Welcome to My Notes", WELCOME_NOTE_CONTENT, isPinned = true),
        note(users.demoUser, "Meeting Notes", MEETING_NOTE_CONTENT, category = categories.workCategory),
        note(users.demoUser, "Birthday Gift Ideas", BIRTHDAY_NOTE_CONTENT, category = categories.personalCategory),
        note(users.demoUser, "Project Ideas", PROJECT_NOTE_CONTENT, category = categories.ideasCategory),
        note(users.demoUser, "Grocery List", GROCERY_NOTE_CONTENT, category = categories.personalCategory)
    )

    private fun johnNotes(users: SeededUsers, categories: SeededCategories): List<Note> = listOf(
        note(users.johnUser, "Study Plan", STUDY_NOTE_CONTENT, category = categories.studyCategory),
        note(users.johnUser, "Workout Routine", WORKOUT_NOTE_CONTENT, isPinned = true, category = categories.fitnessCategory)
    )

    private fun note(
        user: User,
        title: String,
        content: String,
        isPinned: Boolean = false,
        category: Category? = null
    ): Note = Note(
        user = user,
        title = title,
        content = content,
        isPinned = isPinned,
        category = category
    )

    private const val WELCOME_NOTE_CONTENT =
        "This is your first note! Use this app to organize your thoughts and ideas."
    private const val MEETING_NOTE_CONTENT = "Discuss project timeline and deliverables for Q2."
    private const val BIRTHDAY_NOTE_CONTENT = "- Book subscription box\n- Coffee brewing kit\n- Wireless earbuds"
    private const val PROJECT_NOTE_CONTENT =
        "Build a REST API with Spring Boot and Kotlin. Focus on clean architecture and testing."
    private const val GROCERY_NOTE_CONTENT = "- Milk\n- Bread\n- Eggs\n- Coffee beans"
    private const val STUDY_NOTE_CONTENT = "Review chapters 5-8 for the upcoming exam."
    private const val WORKOUT_NOTE_CONTENT = "Monday: Chest\nWednesday: Back\nFriday: Legs"
}
