# Category API Documentation

## Authentication

All Category API endpoints require JWT authentication. Include the token in the `Authorization` header:

```
Authorization: Bearer <token>
```

Get a token by logging in via `/api/auth/login` or registering via `/api/auth/register`.

---

## Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/categories` | List all categories for authenticated user |
| GET | `/api/categories/{id}` | Get a single category by ID |
| POST | `/api/categories` | Create a new category |
| PUT | `/api/categories/{id}` | Update an existing category |
| DELETE | `/api/categories/{id}` | Delete a category |

---

## 1. List All Categories

### Endpoint

```
GET /api/categories
```

### Headers

| Header | Value | Required |
|--------|-------|----------|
| Authorization | `Bearer <token>` | Yes |

### Success Response (200 OK)

```json
[
  {
    "id": 1,
    "name": "Work",
    "color": "#3498db",
    "userId": 1,
    "createdAt": "2026-03-14T11:52:48.175129",
    "updatedAt": "2026-03-14T11:52:48.175134"
  },
  {
    "id": 2,
    "name": "Personal",
    "color": "#e74c3c",
    "userId": 1,
    "createdAt": "2026-03-14T11:52:48.194003",
    "updatedAt": "2026-03-14T11:52:48.194005"
  }
]
```

### Example

```bash
curl -X GET http://localhost:8080/api/categories \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

### Error Responses

| Status | Error | Description |
|--------|-------|-------------|
| 403 | Forbidden | Missing or invalid JWT token |

---

## 2. Get Category by ID

### Endpoint

```
GET /api/categories/{id}
```

### Headers

| Header | Value | Required |
|--------|-------|----------|
| Authorization | `Bearer <token>` | Yes |

### Path Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| id | number | Category ID |

### Success Response (200 OK)

```json
{
  "id": 1,
  "name": "Work",
  "color": "#3498db",
  "userId": 1,
  "createdAt": "2026-03-14T11:52:48.175129",
  "updatedAt": "2026-03-14T11:52:48.175134"
}
```

### Example

```bash
curl -X GET http://localhost:8080/api/categories/1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

### Error Responses

| Status | Error | Description |
|--------|-------|-------------|
| 403 | Forbidden | Missing or invalid JWT token |
| 404 | Not Found | Category not found or doesn't belong to user |

```json
{
  "error": "Category not found with id: 999"
}
```

---

## 3. Create Category

### Endpoint

```
POST /api/categories
```

### Headers

| Header | Value | Required |
|--------|-------|----------|
| Authorization | `Bearer <token>` | Yes |
| Content-Type | `application/json` | Yes |

### Body Parameters

| Field | Type | Required | Validation |
|-------|------|----------|------------|
| name | string | Yes | 2-100 characters, unique per user |
| color | string | No | Max 7 characters (hex color) |

### Request Example

```bash
curl -X POST http://localhost:8080/api/categories \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Work",
    "color": "#3498db"
  }'
```

### Success Response (201 Created)

```json
{
  "id": 1,
  "name": "Work",
  "color": "#3498db",
  "userId": 1,
  "createdAt": "2026-03-14T11:52:48.175129",
  "updatedAt": "2026-03-14T11:52:48.175134"
}
```

### Error Responses

| Status | Error | Description |
|--------|-------|-------------|
| 400 | Bad Request | Validation error (empty/short name) |
| 403 | Forbidden | Missing or invalid JWT token |
| 409 | Conflict | Category name already exists for user |

#### Empty Name (400)

```json
{
  "errors": {
    "name": "Category name must be between 2 and 100 characters"
  }
}
```

#### Duplicate Name (409)

```json
{
  "error": "Category already exists with name: Work"
}
```

---

## 4. Update Category

### Endpoint

```
PUT /api/categories/{id}
```

### Headers

| Header | Value | Required |
|--------|-------|----------|
| Authorization | `Bearer <token>` | Yes |
| Content-Type | `application/json` | Yes |

### Path Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| id | number | Category ID |

### Body Parameters

| Field | Type | Required | Validation |
|-------|------|----------|------------|
| name | string | Yes | 2-100 characters, unique per user |
| color | string | No | Max 7 characters (hex color) |

### Request Example

```bash
curl -X PUT http://localhost:8080/api/categories/1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Work Updated",
    "color": "#2980b9"
  }'
```

### Success Response (200 OK)

```json
{
  "id": 1,
  "name": "Work Updated",
  "color": "#2980b9",
  "userId": 1,
  "createdAt": "2026-03-14T11:52:48.175129",
  "updatedAt": "2026-03-14T11:52:51.822636"
}
```

### Error Responses

| Status | Error | Description |
|--------|-------|-------------|
| 400 | Bad Request | Validation error (empty/short name) |
| 403 | Forbidden | Missing or invalid JWT token |
| 404 | Not Found | Category not found or doesn't belong to user |
| 409 | Conflict | Category name already exists for user |

#### Not Found (404)

```json
{
  "error": "Category not found with id: 999"
}
```

#### Duplicate Name (409)

```json
{
  "error": "Category already exists with name: Work"
}
```

---

## 5. Delete Category

### Endpoint

```
DELETE /api/categories/{id}
```

### Headers

| Header | Value | Required |
|--------|-------|----------|
| Authorization | `Bearer <token>` | Yes |

### Path Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| id | number | Category ID |

### Request Example

```bash
curl -X DELETE http://localhost:8080/api/categories/1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

### Success Response (200 OK)

```json
{
  "message": "Category deleted successfully"
}
```

### Error Responses

| Status | Error | Description |
|--------|-------|-------------|
| 403 | Forbidden | Missing or invalid JWT token |
| 404 | Not Found | Category not found or doesn't belong to user |

#### Not Found (404)

```json
{
  "error": "Category not found with id: 999"
}
```

### Notes

- When a category is deleted, all notes associated with that category will have their `category` field set to `null`
- The category must belong to the authenticated user

---

## Response Fields

| Field | Type | Description |
|-------|------|-------------|
| id | number | Unique identifier of the category |
| name | string | Category name (2-100 characters) |
| color | string \| null | Hex color code (e.g., "#3498db"), optional |
| userId | number | ID of the user who owns this category |
| createdAt | string | ISO 8601 timestamp of creation |
| updatedAt | string \| null | ISO 8601 timestamp of last update |

---

## Validation Rules

| Field | Rule | Error Message |
|-------|------|---------------|
| name | Required, not blank | "Category name is required" |
| name | Min 2, Max 100 characters | "Category name must be between 2 and 100 characters" |
| name | Unique per user | "Category already exists with name: ..." |
| color | Max 20 characters | "Color must be at most 20 characters" |

**Note:** The `name` and `color` fields are automatically trimmed of leading/trailing whitespace before processing.

---

## Error Summary Table

| Error Case | HTTP Status | Error Message | Error Format |
|------------|-------------|---------------|--------------|
| Success (Create) | 201 Created | - | CategoryResponse object |
| Success (Others) | 200 OK | - | CategoryResponse object or message |
| Unauthorized | 403 Forbidden | - | Empty response |
| Category Not Found | 404 Not Found | "Category not found with id: ..." | `{"error": "..."}` |
| Duplicate Name | 409 Conflict | "Category already exists with name: ..." | `{"error": "..."}` |
| Empty Name | 400 Bad Request | "Category name is required" | `{"errors": {"name": "..."}}` |
| Name Too Short | 400 Bad Request | "Category name must be between 2 and 100 characters" | `{"errors": {"name": "..."}}` |
| Color Too Long | 400 Bad Request | "Color must be at most 20 characters" | `{"errors": {"color": "..."}}` |
| Invalid JSON | 400 Bad Request | "Invalid JSON format" | `{"error": "..."}` |

---

## Notes

- **User Isolation:** All categories are scoped to the authenticated user. Users can only access, modify, and delete their own categories.
- **Unique Names:** Category names must be unique per user. Attempting to create or update a category with an existing name will return a 409 Conflict error.
- **Self-Update:** Updating a category with the same name is allowed (no conflict).
- **Notes Relationship:** When a category is deleted, notes that were assigned to that category will have their reference cleared (`categoryId` set to `null`).
- **Color Format:** The `color` field accepts any string up to 7 characters. Typically used for hex color codes (e.g., "#3498db").