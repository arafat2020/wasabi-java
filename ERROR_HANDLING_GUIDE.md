# Comprehensive Error Handling Guide

## Overview
This guide explains the detailed error handling implemented for both registration and login endpoints in your Spring Boot application.

## Registration Endpoint (`/auth/register`)

### Possible Error Responses

#### 1. Validation Errors
**HTTP Status:** `400 Bad Request`

**Example Response:**
```json
{
    "error": "Validation failed",
    "success": false,
    "errorType": "VALIDATION_ERROR",
    "validationErrors": [
        "Username must be between 3 and 50 characters",
        "Email should be valid",
        "Password must be at least 6 characters long"
    ]
}
```

**Triggers:**
- Username is null, empty, or not between 3-50 characters
- Email is null, empty, or invalid format
- Password is null, empty, or less than 6 characters

#### 2. User Already Exists
**HTTP Status:** `400 Bad Request`

**Example Response:**
```json
{
    "error": "Username already taken",
    "success": false,
    "errorType": "USER_ALREADY_EXISTS"
}
```

**Triggers:**
- Username already exists in database
- Email already exists in database

#### 3. Internal Server Error
**HTTP Status:** `500 Internal Server Error`

**Example Response:**
```json
{
    "error": "Registration failed: Database connection error",
    "success": false,
    "errorType": "INTERNAL_ERROR",
    "details": "SQLException"
}
```

#### 4. Success Response
**HTTP Status:** `200 OK`

**Example Response:**
```json
{
    "message": "User registered successfully",
    "success": true
}
```

## Login Endpoint (`/auth/login`)

### Possible Error Responses

#### 1. Validation Errors
**HTTP Status:** `400 Bad Request`

**Example Response:**
```json
{
    "error": "Validation failed",
    "success": false,
    "errorType": "VALIDATION_ERROR",
    "validationErrors": [
        "Username is required",
        "Password is required"
    ]
}
```

**Triggers:**
- Username is null or empty
- Password is null or empty

#### 2. User Not Found
**HTTP Status:** `400 Bad Request`

**Example Response:**
```json
{
    "error": "User not found",
    "success": false,
    "errorType": "USER_NOT_FOUND"
}
```

**Triggers:**
- Username doesn't exist in the database

#### 3. Invalid Credentials (Wrong Password)
**HTTP Status:** `400 Bad Request`

**Example Response:**
```json
{
    "error": "Invalid username or password",
    "success": false,
    "errorType": "INVALID_CREDENTIALS"
}
```

**Triggers:**
- User exists but password is incorrect
- Authentication fails due to bad credentials

#### 4. Authentication Error
**HTTP Status:** `400 Bad Request`

**Example Response:**
```json
{
    "error": "Authentication failed",
    "success": false,
    "errorType": "AUTHENTICATION_ERROR"
}
```

**Triggers:**
- General authentication failures
- Account locked, disabled, etc. (if implemented)

#### 5. Internal Server Error
**HTTP Status:** `500 Internal Server Error`

**Example Response:**
```json
{
    "error": "Login failed due to unexpected error",
    "success": false,
    "errorType": "INTERNAL_ERROR",
    "details": "NullPointerException"
}
```

#### 6. Success Response
**HTTP Status:** `200 OK`

**Example Response:**
```json
{
    "message": "Login successful",
    "success": true,
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "username": "testuser"
}
```

## Error Types Summary

| Error Type | Description | HTTP Status |
|------------|-------------|-------------|
| `VALIDATION_ERROR` | Input validation failed | 400 |
| `USER_ALREADY_EXISTS` | User registration conflict | 400 |
| `USER_NOT_FOUND` | Login with non-existent user | 400 |
| `INVALID_CREDENTIALS` | Wrong password | 400 |
| `AUTHENTICATION_ERROR` | General auth failure | 400 |
| `INTERNAL_ERROR` | Server-side error | 500 |

## Logging

### Registration Logs
- `INFO`: Registration attempts and successes
- `WARN`: Validation failures and user conflicts
- `ERROR`: Unexpected errors
- `DEBUG`: Detailed flow information

### Login Logs
- `INFO`: Login attempts and successes
- `WARN`: Authentication failures
- `ERROR`: Unexpected errors
- `DEBUG`: Authentication flow details

### Example Log Output
```
2025-08-02 23:59:38 INFO  AuthController - Registration attempt for username: testuser
2025-08-02 23:59:38 DEBUG RegisterService - Input validation passed for user: testuser
2025-08-02 23:59:38 DEBUG RegisterService - Checking if username exists: testuser
2025-08-02 23:59:38 DEBUG RegisterService - Checking if email exists: test@example.com
2025-08-02 23:59:38 DEBUG RegisterService - Creating new user entity for: testuser
2025-08-02 23:59:38 DEBUG RegisterService - Saving user to database: testuser
2025-08-02 23:59:38 INFO  RegisterService - User registered successfully: testuser
```

## Testing Error Scenarios

### Registration Tests
1. **Empty fields:** Send request with null/empty username, email, or password
2. **Invalid email:** Send request with malformed email
3. **Short password:** Send password less than 6 characters
4. **Duplicate username:** Register same username twice
5. **Duplicate email:** Register same email twice

### Login Tests
1. **Empty fields:** Send request with null/empty username or password
2. **Non-existent user:** Login with username that doesn't exist
3. **Wrong password:** Login with correct username but wrong password
4. **Successful login:** Login with correct credentials

### Sample Test Requests

#### Register with validation errors:
```json
POST /auth/register
{
    "username": "ab",
    "email": "invalid-email",
    "password": "123"
}
```

#### Login with non-existent user:
```json
POST /auth/login
{
    "username": "nonexistent",
    "password": "anypassword"
}
```

#### Login with wrong password:
```json
POST /auth/login
{
    "username": "existinguser",
    "password": "wrongpassword"
}
```

## Best Practices

1. **Never expose sensitive information** in error messages
2. **Log security events** for monitoring and auditing
3. **Use consistent error response format** across all endpoints
4. **Provide meaningful error types** for client-side handling
5. **Validate input** at multiple layers (annotation + manual validation)
6. **Handle database constraint violations** as backup for race conditions

This comprehensive error handling ensures your API provides clear, actionable feedback to clients while maintaining security and logging appropriate information for debugging and monitoring.
