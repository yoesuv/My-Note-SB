# My Note API 

[![Build Status](https://github.com/yoesuv/My-Note-SB/actions/workflows/github-actions.yml/badge.svg)](https://github.com/yoesuv/My-Note-SB/actions)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/05da8d3007824831b3cc199fdef71f6c)](https://app.codacy.com/gh/yoesuv/My-Note-SB)
[![Codacy Badge](https://app.codacy.com/project/badge/Coverage/05da8d3007824831b3cc199fdef71f6c)](https://app.codacy.com/gh/yoesuv/My-Note-SB)

A **Spring Boot** RESTful API for a personal note-taking application with JWT authentication, built with Kotlin, Spring Data JPA, PostgreSQL, Flyway, Spring Security, and JJWT.

## Features

- **Authentication & Authorization**
  - JWT-based authentication with secure token generation
  - User registration and login
  - Token-based API access control

- **Notes Management**
  - Create, read, update, and delete personal notes
  - Pin important notes
  - Filter notes by category
  - Full audit trail (createdAt, updatedAt)

- **Categories**
  - Organize notes with custom categories
  - Color-coded categories for visual organization
  - CRUD operations for category management

- **Security**
  - Spring Security with JWT tokens
  - Password encryption with BCrypt
  - User-scoped data isolation (users can only access their own data)

## Project Structure

```
my-note/
├── src/main/kotlin/com/yoesuv/mynote/
│   ├── config/          # Configuration classes (Security, JPA, JWT)
│   ├── controller/      # REST API controllers
│   ├── domain/          # JPA entities
│   ├── dto/             # Data Transfer Objects
│   │   ├── auth/        # Auth request/response DTOs
│   │   ├── category/    # Category request/response DTOs
│   │   └── note/        # Note request/response DTOs
│   ├── exception/       # Exception handling
│   ├── repository/      # Spring Data repositories
│   ├── security/        # JWT service and filters
│   └── service/         # Business logic layer
├── src/main/resources/
│   └── db/migration/    # Flyway database migrations
└── docs/                # API documentation
```

## Getting Started

### Prerequisites

- Java 17 or higher
- PostgreSQL 12+
- Gradle (or use the included wrapper)

### Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd my-note
   ```

2. **Create database**
   
   Create a PostgreSQL database (tables will be auto-created on first run):
   ```sql
   CREATE DATABASE mynotes;
   ```
   
   Or using psql:
   ```bash
   psql -U postgres -c "CREATE DATABASE mynotes;"
   ```

3. **Configure database connection**
   
   Update `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/mynotes
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

4. **Configure JWT** (optional)
   
   Update JWT settings in `application.properties`:
   ```properties
   jwt.secret=your-256-bit-secret-key-here-minimum-32-characters
   jwt.expiration=86400000
   ```

5. **Run the application**
   ```bash
   ./gradlew bootRun
   ```
   
   Or build and run the JAR:
   ```bash
   ./gradlew build
   java -jar build/libs/my-note-0.0.1-SNAPSHOT.jar
   ```

The API will be available at `http://localhost:8080`

## API Documentation

**Base URL:** `http://localhost:8080/api`

| Resource | Endpoints |
|----------|-----------|
| Auth | `POST /auth/register`, `POST /auth/login` |
| Notes | `GET/POST /notes`, `GET/PUT/DELETE /notes/{id}` |
| Categories | `GET/POST /categories`, `GET/PUT/DELETE /categories/{id}` |

> All endpoints except `/auth/**` require `Authorization: Bearer <token>` header.

For full documentation with request/response examples and error codes, see:
- [Register API](docs/register.md)
- [Login API](docs/login.md)
- [Notes API](docs/note.md)
- [Categories API](docs/category.md)

## Database Schema

The application uses Flyway for database migrations. Tables are automatically created:

- `users` - User accounts
- `categories` - Note categories
- `notes` - User notes

## Running Tests

```bash
./gradlew test
```

## Building for Production

```bash
./gradlew bootJar
```

The production JAR will be in `build/libs/`.

## License

This project is open source and available under the [MIT License](LICENSE).

---

**Note:** This is a backend API. For a complete application, pair it with a frontend client (web, mobile, or desktop).
