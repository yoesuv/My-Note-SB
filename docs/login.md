# Login API Documentation

## Endpoint

```
POST /api/auth/login
```

## Description

Authenticate a user with email and password. Returns a JWT token and user information upon successful authentication.

## Request

### Headers

| Header | Value | Required |
|--------|-------|----------|
| Content-Type | `application/json` | Yes |

### Body Parameters

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| email | string | Yes | User's email address (must be valid email format) |
| password | string | Yes | User's password |

### Request Example

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

## Responses

### Success Response (200 OK)

**Status Code:** `200 OK`

**Response Body:**

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwiaWQiOjMsImlhdCI6MTc3MzM4ODE3MiwiZXhwIjoxODA0OTI0MTcyfQ.t9z_cdXSJVKbel8VdGCZk8R8FUahe3EcDLxO3ZDxrOc",
  "userId": 3,
  "fullName": "Test User",
  "email": "test@example.com"
}
```

**Fields:**

| Field | Type | Description |
|-------|------|-------------|
| token | string | JWT token for authentication (expires in 365 days) |
| userId | number | Unique identifier of the user |
| fullName | string | User's full name |
| email | string | User's email address |

---

## Error Responses

### 1. Wrong Email (Email Not Registered)

**Status Code:** `400 Bad Request`

**Condition:** The provided email address is not registered in the system.

**Response:**

```json
{
  "error": "Email not registered"
}
```

**Example:**

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "nonexistent@example.com",
    "password": "password123"
  }'
```

---

### 2. Wrong Password

**Status Code:** `400 Bad Request`

**Condition:** The provided password does not match the registered email.

**Response:**

```json
{
  "error": "Wrong password"
}
```

**Example:**

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "wrongpassword"
  }'
```

---

### 3. Empty Email

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
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "",
    "password": "password123"
  }'
```

---

### 4. Invalid Email Format

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
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "invalid-email",
    "password": "password123"
  }'
```

---

### 5. Empty Password

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
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": ""
  }'
```

---

### 6. Missing Fields

**Status Code:** `400 Bad Request`

**Condition:** One or more required fields are missing from the request body.

**Response:**

```json
{
  "errors": {
    "email": "Email is required",
    "password": "Password is required"
  }
}
```

**Example:**

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{}'
```

---

### 7. Invalid JSON Format

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
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  --data-binary 'not-valid-json'
```

---

## Error Summary Table

| Error Case | HTTP Status | Error Message | Error Format |
|------------|-------------|---------------|--------------|
| Login Success | 200 OK | - | AuthResponse object |
| Wrong Email | 400 Bad Request | "Email not registered" | `{"error": "..."}` |
| Wrong Password | 400 Bad Request | "Wrong password" | `{"error": "..."}` |
| Empty Email | 400 Bad Request | "Email is required" | `{"errors": {"email": "..."}}` |
| Invalid Email Format | 400 Bad Request | "Invalid email format" | `{"errors": {"email": "..."}}` |
| Empty Password | 400 Bad Request | "Password is required" | `{"errors": {"password": "..."}}` |
| Missing Fields | 400 Bad Request | Multiple validation messages | `{"errors": {...}}` |
| Invalid JSON | 400 Bad Request | "Invalid JSON format" | `{"error": "..."}` |

## Notes

- **Stateless Authentication:** This API uses JWT (JSON Web Token) for stateless authentication. The token is valid for 365 days.
- **Token Usage:** Include the token in the `Authorization` header for protected endpoints: `Authorization: Bearer <token>`
- **Security:** Always use HTTPS in production to protect credentials and tokens.
- **Rate Limiting:** Consider implementing rate limiting to prevent brute force attacks on the login endpoint.
