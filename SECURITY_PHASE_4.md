# Security Phase 4: Enhanced Security Features

## Overview

Phase 4 implements advanced security features including password validation, account lockout, audit logging, input sanitization, and secure error handling.

## Implemented Features

### 1. Password Strength Validation

**Location**: `src/main/java/com/personalfin/server/security/validation/PasswordValidator.java`

**Requirements**:
- Minimum 8 characters, maximum 128 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one digit
- At least one special character (@$!%*?&)
- Rejects common weak passwords

**Usage**:
- Automatically applied during user registration
- Returns detailed error messages for failed validation

### 2. Account Lockout Mechanism

**Location**: `src/main/java/com/personalfin/server/security/service/AccountLockoutService.java`

**Features**:
- Tracks failed login attempts per username and IP address
- Locks account after configurable number of failed attempts (default: 5)
- Configurable lockout window (default: 15 minutes)
- Configurable lockout duration (default: 30 minutes)
- Separate thresholds for username-based and IP-based blocking

**Configuration** (in `application-dev.yml` or `application-prod.yml`):
```yaml
security:
  account-lockout:
    max-attempts: 5
    lockout-duration-minutes: 30
    window-minutes: 15
```

**Environment Variables**:
- `SECURITY_MAX_ATTEMPTS`: Maximum failed attempts before lockout
- `SECURITY_LOCKOUT_DURATION`: Lockout duration in minutes
- `SECURITY_WINDOW_MINUTES`: Time window for counting attempts

### 3. Audit Logging

**Location**: `src/main/java/com/personalfin/server/security/service/AuditLogService.java`

**Features**:
- Logs all authentication attempts (successful and failed)
- Logs user registration attempts
- Logs sensitive operations (data access, modifications)
- Tracks user ID, username, IP address, and event details
- Stores success/failure status

**Event Types**:
- `AUTHENTICATION`: Login attempts
- `REGISTRATION`: User registration
- Custom event types for sensitive operations

**Database Table**: `audit_logs`
- Indexed by `user_id`, `event_type`, and `created_at` for efficient querying

### 4. Input Sanitization and Validation

**Location**: `src/main/java/com/personalfin/server/security/filter/InputSanitizationFilter.java`

**Protection Against**:
- **SQL Injection**: Detects common SQL injection patterns
- **XSS (Cross-Site Scripting)**: Detects script tags and event handlers
- **Path Traversal**: Detects directory traversal attempts (../, ..\\, encoded variants)

**Features**:
- Validates all query parameters
- Validates request paths
- Throws `SecurityException` on malicious input detection
- Skips filtering for actuator endpoints

**Utility Class**: `SecurityUtils`
- `sanitizeInput()`: Removes dangerous characters
- `containsSqlInjection()`: Checks for SQL injection patterns
- `containsXss()`: Checks for XSS patterns
- `containsPathTraversal()`: Checks for path traversal patterns
- `sanitizeFileName()`: Sanitizes file names
- `getClientIpAddress()`: Safely extracts client IP from request

### 5. Secure Error Handling

**Location**: `src/main/java/com/personalfin/server/security/exception/GlobalExceptionHandler.java`

**Features**:
- Prevents information leakage in error messages
- Consistent error response format
- Secure handling of authentication failures
- Proper HTTP status codes

**Error Responses**:
- **401 Unauthorized**: Generic "Invalid username or password" (doesn't reveal if username exists)
- **403 Forbidden**: Account locked or security violation
- **400 Bad Request**: Validation errors with field-level details
- **413 Payload Too Large**: File upload size exceeded
- **500 Internal Server Error**: Generic message (no stack traces exposed)

### 6. Request Size Limits

**Configuration** (in `application-dev.yml` and `application-prod.yml`):
```yaml
spring:
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB
```

**Protection**:
- Prevents DoS attacks via large file uploads
- Limits request payload size
- Returns `413 Payload Too Large` when exceeded

## Database Schema

### Login Attempts Table
```sql
CREATE TABLE login_attempts (
    id UUID PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    ip_address VARCHAR(45),
    successful BOOLEAN NOT NULL,
    failure_reason VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL
);
```

### Audit Logs Table
```sql
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    event_type VARCHAR(50) NOT NULL,
    user_id UUID,
    username VARCHAR(100),
    ip_address VARCHAR(45),
    details TEXT,
    success BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);
```

## Integration Points

### Updated Services

1. **UserService** (`src/main/java/com/personalfin/server/user/service/UserService.java`)
   - Validates password strength before creating users
   - Throws `IllegalArgumentException` with detailed error message

2. **AuthController** (`src/main/java/com/personalfin/server/auth/web/AuthController.java`)
   - Checks account lockout status before authentication
   - Records all login attempts (successful and failed)
   - Records registration attempts
   - Throws `AccountLockedException` when account is locked
   - Extracts and logs IP addresses

3. **SecurityConfig** (`src/main/java/com/personalfin/server/config/SecurityConfig.java`)
   - Added `InputSanitizationFilter` to filter chain
   - Executes before other security filters

## Security Filter Chain Order

1. `InputSanitizationFilter` - Validates and sanitizes input
2. `SecurityHeadersConfig` - Adds security headers
3. `RateLimitingFilter` - Rate limiting
4. `JwtAuthenticationFilter` - JWT authentication

## Usage Examples

### Password Validation
```java
@Autowired
private PasswordValidator passwordValidator;

PasswordValidator.PasswordValidationResult result = passwordValidator.validate(password);
if (!result.isValid()) {
    throw new IllegalArgumentException(result.getErrorMessage());
}
```

### Account Lockout Check
```java
@Autowired
private AccountLockoutService accountLockoutService;

if (accountLockoutService.isAccountLocked(username)) {
    throw new AccountLockedException("Account is locked", 
        accountLockoutService.getRemainingAttempts(username),
        accountLockoutService.getLockoutDurationMinutes());
}
```

### Audit Logging
```java
@Autowired
private AuditLogService auditLogService;

// Log authentication
auditLogService.logAuthentication(username, ipAddress, true, null);

// Log sensitive operation
auditLogService.logSensitiveOperation("DATA_EXPORT", userId, username, ipAddress, 
    "Exported expenses to PDF");
```

### Input Sanitization
```java
import com.personalfin.server.security.util.SecurityUtils;

// Sanitize user input
String sanitized = SecurityUtils.sanitizeInput(userInput);

// Check for SQL injection
if (SecurityUtils.containsSqlInjection(input)) {
    throw new SecurityException("SQL injection detected");
}
```

## Configuration

### Environment Variables

Add to your `.env` file:
```bash
# Account Lockout Configuration
SECURITY_MAX_ATTEMPTS=5
SECURITY_LOCKOUT_DURATION=30
SECURITY_WINDOW_MINUTES=15
```

### Application Properties

Already configured in `application-dev.yml` and `application-prod.yml`:
- Account lockout settings
- Request size limits
- Security filter chain

## Testing

### Test Password Validation
```bash
# Valid password
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","email":"test@example.com","password":"Test123!@#"}'

# Invalid password (too short)
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","email":"test@example.com","password":"short"}'
```

### Test Account Lockout
1. Attempt to login with wrong password 5 times
2. 6th attempt should return `403 Forbidden` with account locked message

### Test Input Sanitization
```bash
# SQL injection attempt
curl "http://localhost:8080/api/expenses?category=test' OR '1'='1"

# Should return 403 Forbidden with security error
```

## Security Best Practices

1. **Password Policy**: Enforce strong passwords during registration
2. **Account Lockout**: Prevent brute force attacks
3. **Audit Logging**: Track all security events for compliance and forensics
4. **Input Validation**: Validate and sanitize all user input
5. **Error Handling**: Don't leak sensitive information in error messages
6. **Request Limits**: Prevent DoS attacks via large payloads

## Monitoring

### Query Failed Login Attempts
```sql
SELECT username, ip_address, COUNT(*) as failed_attempts
FROM login_attempts
WHERE successful = false
  AND created_at >= NOW() - INTERVAL '1 hour'
GROUP BY username, ip_address
ORDER BY failed_attempts DESC;
```

### Query Audit Logs
```sql
SELECT event_type, username, ip_address, success, details, created_at
FROM audit_logs
WHERE created_at >= NOW() - INTERVAL '24 hours'
ORDER BY created_at DESC;
```

## Migration

Run the database migration:
```bash
# Migration file: V10__create_security_tables.sql
# Automatically applied on application startup via Flyway
```

## Next Steps (Future Phases)

- **Token Refresh**: Implement refresh token mechanism
- **Password History**: Prevent password reuse
- **Two-Factor Authentication**: Add 2FA support
- **Session Management**: Track active sessions
- **IP Whitelisting**: Allow trusted IPs to bypass some restrictions
- **Security Notifications**: Email alerts for security events

