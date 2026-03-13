# Register API Documentation

## Endpoint

```
POST /api/auth/register
```

## Description

Register a new user account. Creates a new user with the provided information and returns a JWT token for immediate authentication.

## Request

### Headers

| Header | Value | Required |
|--------|-------|----------|
| Content-Type | `application/json` | Yes |

### Body Parameters

| Field | Type | Required | Validation |
|-------|------|----------|------------|
| fullName | string | Yes | 2-100 characters |
| email | string | Yes | Valid email format, must be unique |
| password | string | Yes | Minimum 6 characters |

### Request Example

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "John Doe",
    "email": "john@example.com",
    "password": "securepassword"
  }'
```

## Responses

### Success Response (201 Created)

**Status Code:** `201 Created`

**Response Body:**

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJuZXd1c2VyQGV4YW1wbGUuY29tIiwiaWQiOjQsImlhdCI6MTc3MzM5MDc2NywiZXhwIjoxODA0OTI2NzY3fQ.lxwGj_kehnlrh3rV26uxn1jkaJCCz8OnhoxjCVesMZA",
  "userId": 4,
  "fullName": "New User",
  "email": "newuser@example.com"
}
```

**Fields:**

| Field | Type | Description |
|-------|------|-------------|
| token | string | JWT token for authentication (expires in 365 days) |
| userId | number | Unique identifier of the newly created user |
| fullName | string | User's full name |
| email | string | User's email address |

---

## Error Responses

### 1. Email Already Registered

**Status Code:** `409 Conflict`

**Condition:** The provided email address is already registered in the system.

**Response:**

```json
{
  "error": "User already exists with email: test@example.com"
}
```

**Example:**

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Test User",
    "email": "test@example.com",
    "password": "password123"
  }'
```

---

### 2. Empty Full Name

**Status Code:** `400 Bad Request`

**Condition:** The fullName field is empty or contains only whitespace.

**Response:**

```json
{
  "errors": {
    "fullName": "Full name is required"
  }
}
```

**Example:**

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "",
    "email": "test@example.com",
    "password": "password123"
  }'
```

---

### 3. Full Name Too Short

**Status Code:** `400 Bad Request`

**Condition:** The fullName field contains less than 2 characters.

**Response:**

```json
{
  "errors": {
    "fullName": "Full name must be between 2 and 100 characters"
  }
}
```

**Example:**

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "A",
    "email": "test@example.com",
    "password": "password123"
  }'
```

---

### 4. Empty Email

**Status Code:** `400 Bad Request`

**Condition:** The email field is empty or contains only whitespace.

**Response:**

```json
{
  "errors": {
    "email": "Email is required"
  }
}
```

**Example:**

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Test User",
    "email": "",
    "password": "password123"
  }'
```

---

### 5. Invalid Email Format

**Status Code:** `400 Bad Request`

**Condition:** The email field contains an invalid email format.

**Response:**

```json
{
  "errors": {
    "email": "Invalid email format"
  }
}
```

**Example:**

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Test User",
    "email": "invalid-email",
    "password": "password123"
  }'
```

---

### 6. Empty Password

**Status Code:** `400 Bad Request`

**Condition:** The password field is empty or contains only whitespace.

**Response:**

```json
{
  "errors": {
    "password": "Password is required"
  }
}
```

**Example:**

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Test User",
    "email": "test@example.com",
    "password": ""
  }'
```

---

### 7. Password Too Short

**Status Code:** `400 Bad Request`

**Condition:** The password field contains less than 6 characters.

**Response:**

```json
{
  "errors": {
    "password": "Password must be at least 6 characters"
  }
}
```

**Example:**

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Test User",
    "email": "test@example.com",
    "password": "12345"
  }'
```

---

### 8. Missing All Fields

**Status Code:** `400 Bad Request`

**Condition:** The request body is empty or missing all required fields.

**Response:**

```json
{
  "errors": {
    "password": "Password is required",
    "fullName": "Full name is required",
    "email": "Email is required"
  }
}
```

**Example:**

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{}'
```

---

### 9. Invalid JSON Format

**Status Code:** `400 Bad Request`

**Condition:** The request body is not valid JSON.

**Response:**

```json
{
  "error": "Invalid JSON format"
}
```

**Example:**

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  --data-binary 'not-valid-json'
```

---

## Error Summary Table

| Error Case | HTTP Status | Error Message | Error Format |
|------------|-------------|---------------|--------------|
| Register Success | 201 Created | - | AuthResponse object |
| Email Already Registered | 409 Conflict | "User already exists with email: ..." | `{"error": "..."}` |
| Empty Full Name | 400 Bad Request | "Full name is required" | `{"errors": {"fullName": "..."}}` |
| Full Name Too Short | 400 Bad Request | "Full name must be between 2 and 100 characters" | `{"errors": {"fullName": "..."}}` |
| Empty Email | 400 Bad Request | "Email is required" | `{"errors": {"email": "..."}}` |
| Invalid Email Format | 400 Bad Request | "Invalid email format" | `{"errors": {"email": "..."}}` |
| Empty Password | 400 Bad Request | "Password is required" | `{"errors": {"password": "..."}}` |
| Password Too Short | 400 Bad Request | "Password must be at least 6 characters" | `{"errors": {"password": "..."}}` |
| Missing All Fields | 400 Bad Request | Multiple validation messages | `{"errors": {...}}` |
| Invalid JSON | 400 Bad Request | "Invalid JSON format" | `{"error": "..."}` |

## Validation Rules

| Field | Rule | Error Message |
|-------|------|---------------|
| fullName | Required, not blank | "Full name is required" |
| fullName | Min 2, Max 100 characters | "Full name must be between 2 and 100 characters" |
| email | Required, not blank | "Email is required" |
| email | Valid email format | "Invalid email format" |
| email | Must be unique | "User already exists with email: ..." |
| password | Required, not blank | "Password is required" |
| password | Minimum 6 characters | "Password must be at least 6 characters" |

## Notes

- **Stateless Authentication:** After successful registration, the user is immediately authenticated with a JWT token (valid for 365 days).
- **Token Usage:** Include the token in the `Authorization` header for protected endpoints: `Authorization: Bearer <token>`
- **Security:** Always use HTTPS in production to protect user credentials.
- **Email Uniqueness:** Email addresses must be unique across the system. Attempting to register with an existing email will return an error.
- **Password Storage:** Passwords are hashed using BCrypt before storage and are never stored in plain text.
