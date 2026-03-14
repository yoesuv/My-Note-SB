# My Note API

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

2. **Configure database**
   
   Create a PostgreSQL database and update `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/mynote
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

3. **Configure JWT** (optional)
   
   Update JWT settings in `application.properties`:
   ```properties
   jwt.secret=your-256-bit-secret-key-here-minimum-32-characters
   jwt.expiration=86400000
   ```

4. **Run the application**
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

### Authentication Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Login and get JWT token |
| POST | `/api/auth/logout` | Logout (client-side token removal) |

### Notes Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/notes` | Get all notes (optionally filter by `?categoryId={id}`) |
| GET | `/api/notes/{id}` | Get a specific note |
| POST | `/api/notes` | Create a new note |
| PUT | `/api/notes/{id}` | Update an existing note |
| DELETE | `/api/notes/{id}` | Delete a note |

### Categories Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/categories` | Get all categories |
| GET | `/api/categories/{id}` | Get a specific category |
| POST | `/api/categories` | Create a new category |
| PUT | `/api/categories/{id}` | Update a category |
| DELETE | `/api/categories/{id}` | Delete a category |

### Authentication Header

All endpoints except `/api/auth/**` require a valid JWT token:

```
Authorization: Bearer <your-jwt-token>
```

### Example Requests

**Register:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com",
    "password": "password123"
  }'
```

**Login:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "password": "password123"
  }'
```

**Create a Note:**
```bash
curl -X POST http://localhost:8080/api/notes \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "My First Note",
    "content": "This is the content of my note",
    "isPinned": true
  }'
```

For complete API documentation, see the `/docs` directory:
- [Authentication API](docs/login.md)
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
