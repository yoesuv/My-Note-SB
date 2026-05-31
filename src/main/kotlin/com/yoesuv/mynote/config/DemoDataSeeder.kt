package com.yoesuv.mynote.config

import com.yoesuv.mynote.domain.Category
import com.yoesuv.mynote.domain.User
import com.yoesuv.mynote.repository.CategoryRepository
import com.yoesuv.mynote.repository.NoteRepository
import com.yoesuv.mynote.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * Seeds demo data only when the dev profile is active.
 *
 * Activate with: spring.profiles.active=dev
 * Or via env var: SPRING_PROFILES_ACTIVE=dev
 */
@Component
@Profile("dev")
class DemoDataSeeder(
    private val userRepository: UserRepository,
    private val categoryRepository: CategoryRepository,
    private val noteRepository: NoteRepository,
    private val passwordEncoder: PasswordEncoder
) : ApplicationRunner {

    @Transactional
    override fun run(args: ApplicationArguments) {
        if (userRepository.existsByEmail(DEMO_EMAIL)) {
            logger.info("Seed data already present — skipping seeder")
            return
        }

        logger.info("Seeding database with demo data...")

        val users = seedUsers()
        val categories = seedCategories(users)
        noteRepository.saveAll(SeedDataFactory.notes(users, categories))

        logger.info("Database seeded — {} / {} (password: \"{}\")", DEMO_EMAIL, JOHN_EMAIL, SEED_PASSWORD)
    }

    private fun seedUsers(): SeededUsers {
        val demoUser = saveUser("Demo User", DEMO_EMAIL)
        val johnUser = saveUser("John Doe", JOHN_EMAIL)
        return SeededUsers(demoUser = demoUser, johnUser = johnUser)
    }

    private fun saveUser(fullName: String, email: String): User = userRepository.save(
        User(
            fullName = fullName,
            email = email,
            passwordHash = passwordEncoder.encode(SEED_PASSWORD)
        )
    )

    private fun seedCategories(users: SeededUsers): SeededCategories {
        return SeededCategories(
            workCategory = saveCategory(users.demoUser, "Work", "#3498db"),
            personalCategory = saveCategory(users.demoUser, "Personal", "#e74c3c"),
            ideasCategory = saveCategory(users.demoUser, "Ideas", "#2ecc71"),
            studyCategory = saveCategory(users.johnUser, "Study", "#9b59b6"),
            fitnessCategory = saveCategory(users.johnUser, "Fitness", "#f39c12")
        )
    }

    private fun saveCategory(user: User, name: String, color: String): Category = categoryRepository.save(
        Category(user = user, name = name, color = color)
    )

    private companion object {
        private const val DEMO_EMAIL = "demo@mynotes.com"
        private const val JOHN_EMAIL = "john@example.com"
        private const val SEED_PASSWORD = "password"
        private val logger = LoggerFactory.getLogger(DemoDataSeeder::class.java)
    }
}
