# Repository Notes

## Project Shape
- Single-module Gradle Kotlin Spring Boot API; root project name is `note`, application entrypoint is `com.yoesuv.mynote.MyNotesApplication`.
- Main REST routes live under `/api`: auth in `controller/AuthController.kt`, notes in `controller/NoteController.kt`, categories in `controller/CategoryController.kt`.
- Services enforce per-user data access from `SecurityContextHolder`; keep repository queries user-scoped (`findBy...AndUserId`) when touching notes or categories.

## Commands
- Use the checked-in wrapper: `./gradlew ...`.
- Run app: `./gradlew bootRun`.
- Build and test: `./gradlew build`.
- Test suite with coverage XML/HTML: `./gradlew test jacocoTestReport`.
- Focused test class: `./gradlew test --tests 'com.yoesuv.mynote.controller.RegisterTests'`.
- There is no separate lint or formatter Gradle task configured.

## Test And Database Gotchas
- Tests are `@SpringBootTest`/MockMvc integration tests, not pure unit tests; they need PostgreSQL available at `localhost:5432` unless `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` override it.
- Test profile defaults: database `mynotes`, user `postgres`, password `password`; Flyway is disabled and Hibernate uses `ddl-auto=create-drop`.
- Because Hibernate uses default schema `dbo`, create it before local tests if the database is fresh: `PGPASSWORD=password psql -h localhost -U postgres -d mynotes -c "CREATE SCHEMA IF NOT EXISTS dbo;"`.
- CI mirrors this setup with PostgreSQL 16, Java 17, schema creation, then `./gradlew test jacocoTestReport` and `./gradlew build`.

## Config And Persistence
- Runtime config imports optional `./local.properties`; it is gitignored and should stay local.
- Production/default profile validates schema (`spring.jpa.hibernate.ddl-auto=validate`) and relies on Flyway migrations in `src/main/resources/db/migration`.
- The database schema is `dbo`; new migrations should qualify objects consistently with existing `V1__Initial_schema.sql`.
- JWT config is bound from `jwt.secret` / `JWT_SECRET` and `jwt.expiration`; tests provide a safe default secret in `application-test.properties`.

## API Docs
- Endpoint examples are in `docs/register.md`, `docs/login.md`, `docs/note.md`, and `docs/category.md`; update them when changing request/response behavior.
