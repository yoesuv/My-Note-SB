# Note API Documentation

## Authentication

All Note API endpoints require JWT authentication. Include the token in the `Authorization` header:

```
Authorization: Bearer <token>
```

Get a token by logging in via `/api/auth/login` or registering via `/api/auth/register`.

---

## Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/notes` | List all notes for authenticated user |
| GET | `/api/notes?categoryId={id}` | Filter notes by category |
| GET | `/api/notes/{id}` | Get a single note by ID |
| POST | `/api/notes` | Create a new note |
| PUT | `/api/notes/{id}` | Update an existing note |
| DELETE | `/api/notes/{id}` | Delete a note |

---

## 1. List All Notes

### Endpoint

```
GET /api/notes
```

### Headers

| Header | Value | Required |
|--------|-------|----------|
| Authorization | `Bearer <token>` | Yes |

### Query Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| categoryId | number | No | Filter notes by category ID |

### Success Response (200 OK)

```json
[
  {
    "id": 1,
    "title": "My First Note",
    "content": "This is my first note content",
    "isPinned": false,
    "category": null,
    "userId": 1,
    "createdAt": "2026-03-14T15:38:34.627146",
    "updatedAt": "2026-03-14T15:38:34.627157"
  },
  {
    "id": 2,
    "title": "Work Note",
    "content": "Meeting notes",
    "isPinned": true,
    "category": {
      "id": 1,
      "name": "Work",
      "color": "#3498db",
      "userId": 1,
      "createdAt": "2026-03-14T11:52:48.175129",
      "updatedAt": "2026-03-14T11:52:48.175134"
    },
    "userId": 1,
    "createdAt": "2026-03-14T15:38:34.670918",
    "updatedAt": "2026-03-14T15:38:34.670921"
  }
]
```

### Examples

#### Get all notes

```bash
curl -X GET http://localhost:8080/api/notes \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

#### Filter notes by category

```bash
curl -X GET "http://localhost:8080/api/notes?categoryId=1" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

### Error Responses

| Status | Error | Description |
|--------|-------|-------------|
| 403 | Forbidden | Missing or invalid JWT token |

---

## 2. Get Note by ID

### Endpoint

```
GET /api/notes/{id}
```

### Headers

| Header | Value | Required |
|--------|-------|----------|
| Authorization | `Bearer <token>` | Yes |

### Path Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| id | number | Note ID |

### Success Response (200 OK)

```json
{
  "id": 1,
  "title": "My First Note",
  "content": "This is my first note content",
  "isPinned": false,
  "category": null,
  "userId": 1,
  "createdAt": "2026-03-14T15:38:34.627146",
  "updatedAt": "2026-03-14T15:38:34.627157"
}
```

### Example

```bash
curl -X GET http://localhost:8080/api/notes/1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

### Error Responses

| Status | Error | Description |
|--------|-------|-------------|
| 403 | Forbidden | Missing or invalid JWT token |
| 404 | Not Found | Note not found or doesn't belong to user |

```json
{
  "error": "Note not found with id: 999"
}
```

---

## 3. Create Note

### Endpoint

```
POST /api/notes
```

### Headers

| Header | Value | Required |
|--------|-------|----------|
| Authorization | `Bearer <token>` | Yes |
| Content-Type | `application/json` | Yes |

### Body Parameters

| Field | Type | Required | Validation |
|-------|------|----------|------------|
| title | string | Yes | 1-200 characters |
| content | string | No | Any length |
| isPinned | boolean | No | Default: false |
| categoryId | number | No | Must belong to user if provided |

### Request Examples

#### Create note without category

```bash
curl -X POST http://localhost:8080/api/notes \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "title": "My First Note",
    "content": "This is my first note content"
  }'
```

#### Create note with category

```bash
curl -X POST http://localhost:8080/api/notes \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Work Note",
    "content": "Meeting notes",
    "isPinned": true,
    "categoryId": 1
  }'
```

### Success Response (201 Created)

```json
{
  "id": 1,
  "title": "Work Note",
  "content": "Meeting notes",
  "isPinned": true,
  "category": {
    "id": 1,
    "name": "Work",
    "color": "#3498db",
    "userId": 1,
    "createdAt": "2026-03-14T11:52:48.175129",
    "updatedAt": "2026-03-14T11:52:48.175134"
  },
  "userId": 1,
  "createdAt": "2026-03-14T15:38:34.670918",
  "updatedAt": "2026-03-14T15:38:34.670921"
}
```

### Error Responses

| Status | Error | Description |
|--------|-------|-------------|
| 400 | Bad Request | Validation error (empty title) |
| 403 | Forbidden | Missing or invalid JWT token |
| 404 | Not Found | Category not found (if categoryId provided) |

#### Empty Title (400)

```json
{
  "errors": {
    "title": "Title is required"
  }
}
```

#### Invalid Category (404)

```json
{
  "error": "Category not found with id: 999"
}
```

---

## 4. Update Note

### Endpoint

```
PUT /api/notes/{id}
```

### Headers

| Header | Value | Required |
|--------|-------|----------|
| Authorization | `Bearer <token>` | Yes |
| Content-Type | `application/json` | Yes |

### Path Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| id | number | Note ID |

### Body Parameters

| Field | Type | Required | Validation |
|-------|------|----------|------------|
| title | string | Yes | 1-200 characters |
| content | string | No | Any length |
| isPinned | boolean | No | Default: false |
| categoryId | number | No | Must belong to user if provided |

### Request Example

```bash
curl -X PUT http://localhost:8080/api/notes/1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Updated Note",
    "content": "Updated content",
    "isPinned": true,
    "categoryId": 2
  }'
```

### Success Response (200 OK)

```json
{
  "id": 1,
  "title": "Updated Note",
  "content": "Updated content",
  "isPinned": true,
  "category": {
    "id": 2,
    "name": "Personal",
    "color": "#e74c3c",
    "userId": 1,
    "createdAt": "2026-03-14T11:52:48.194003",
    "updatedAt": "2026-03-14T11:52:48.194005"
  },
  "userId": 1,
  "createdAt": "2026-03-14T15:38:34.627146",
  "updatedAt": "2026-03-14T15:38:41.064677"
}
```

### Error Responses

| Status | Error | Description |
|--------|-------|-------------|
| 400 | Bad Request | Validation error (empty title) |
| 403 | Forbidden | Missing or invalid JWT token |
| 404 | Not Found | Note not found or category not found |

#### Note Not Found (404)

```json
{
  "error": "Note not found with id: 999"
}
```

#### Empty Title (400)

```json
{
  "errors": {
    "title": "Title is required"
  }
}
```

---

## 5. Delete Note

### Endpoint

```
DELETE /api/notes/{id}
```

### Headers

| Header | Value | Required |
|--------|-------|----------|
| Authorization | `Bearer <token>` | Yes |

### Path Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| id | number | Note ID |

### Request Example

```bash
curl -X DELETE http://localhost:8080/api/notes/1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

### Success Response (200 OK)

```json
{
  "message": "Note deleted successfully"
}
```

### Error Responses

| Status | Error | Description |
|--------|-------|-------------|
| 403 | Forbidden | Missing or invalid JWT token |
| 404 | Not Found | Note not found or doesn't belong to user |

#### Not Found (404)

```json
{
  "error": "Note not found with id: 999"
}
```

---

## Response Fields

| Field | Type | Description |
|-------|------|-------------|
| id | number | Unique identifier of the note |
| title | string | Note title (1-200 characters) |
| content | string \| null | Note content, optional |
| isPinned | boolean | Whether the note is pinned |
| category | object \| null | Category object if assigned, null otherwise |
| userId | number | ID of the user who owns this note |
| createdAt | string | ISO 8601 timestamp of creation |
| updatedAt | string | ISO 8601 timestamp of last update |

### Category Object (nested)

| Field | Type | Description |
|-------|------|-------------|
| id | number | Unique identifier of the category |
| name | string | Category name |
| color | string \| null | Hex color code, optional |
| userId | number | ID of the user who owns this category |
| createdAt | string | ISO 8601 timestamp of creation |
| updatedAt | string | ISO 8601 timestamp of last update |

---

## Validation Rules

| Field | Rule | Error Message |
|-------|------|---------------|
| title | Required, not blank | "Title is required" |
| title | Min 1, Max 200 characters | "Title must be between 1 and 200 characters" |
| content | Optional, any length | - |
| isPinned | Optional, default false | - |
| categoryId | Optional, must exist and belong to user | "Category not found with id: ..." |

**Note:** The `title` and `content` fields are automatically trimmed of leading/trailing whitespace before processing.

---

## Error Summary Table

| Error Case | HTTP Status | Error Message | Error Format |
|------------|-------------|---------------|--------------|
| Success (Create) | 201 Created | - | NoteResponse object |
| Success (Others) | 200 OK | - | NoteResponse object or message |
| Unauthorized | 403 Forbidden | - | Empty response |
| Note Not Found | 404 Not Found | "Note not found with id: ..." | `{"error": "..."}` |
| Category Not Found | 404 Not Found | "Category not found with id: ..." | `{"error": "..."}` |
| Empty Title | 400 Bad Request | "Title is required" | `{"errors": {"title": "..."}}` |
| Title Too Long | 400 Bad Request | "Title must be between 1 and 200 characters" | `{"errors": {"title": "..."}}` |
| Invalid JSON | 400 Bad Request | "Invalid JSON format" | `{"error": "..."}` |

---

## Notes

- **User Isolation:** All notes are scoped to the authenticated user. Users can only access, modify, and delete their own notes.
- **Duplicate Titles:** Note titles are NOT required to be unique. Multiple notes can have the same title.
- **Category Assignment:** Notes can optionally be assigned to a category. The category must belong to the same user.
- **Category Removal:** To remove a category from a note, set `categoryId` to `null` in the update request.
- **Pinned Notes:** The `isPinned` field can be used to mark important notes. Defaults to `false`.
- **Category Deletion:** When a category is deleted, notes assigned to that category will have their `category` field set to `null`, but the notes themselves are not deleted.