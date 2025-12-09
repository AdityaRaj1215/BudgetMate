# Security Implementation Documentation

This document provides a comprehensive overview of all security features implemented in the Personal Finance Tracker Server application.

## Table of Contents

1. [Overview](#overview)
2. [Phase 1: Authentication & Authorization](#phase-1-authentication--authorization)
3. [Phase 2: Secure Configuration & Secrets Management](#phase-2-secure-configuration--secrets-management)
4. [Phase 3: API Security Enhancements](#phase-3-api-security-enhancements)
5. [Phase 4: Advanced Security Features](#phase-4-advanced-security-features)
6. [Security Architecture](#security-architecture)
7. [Configuration](#configuration)
8. [Best Practices](#best-practices)

---

## Overview

The application implements a multi-layered security approach following industry best practices. Security features are organized into four phases, each building upon the previous one to create a comprehensive security posture.

**Key Security Principles:**
- Defense in depth
- Least privilege
- Secure by default
- Fail securely
- Don't trust user input

---

## Phase 1: Authentication & Authorization

### 1.1 JWT (JSON Web Token) Authentication

**Implementation:**
- **Location:** `com.personalfin.server.auth.service.JwtTokenService`
- **Filter:** `com.personalfin.server.auth.filter.JwtAuthenticationFilter`

**Features:**
- Stateless authentication using JWT tokens
- Token validation on every request
- Username extraction from token claims
- Token expiration support (configurable, default: 24 hours)
- HMAC-SHA256 algorithm for token signing

**Token Structure:**
- Header: Algorithm and token type
- Payload: Username, expiration, issuer
- Signature: HMAC-SHA256 signature

**Configuration:**
```yaml
jwt:
  secret: ${JWT_SECRET}  # Minimum 256 bits (32 characters)
  expiration: ${JWT_EXPIRATION:86400000}  # 24 hours in milliseconds
  issuer: ${JWT_ISSUER:personal-finance-server}
```

### 1.2 User Management & Roles

**Implementation:**
- **Entity:** `com.personalfin.server.user.model.User`
- **Service:** `com.personalfin.server.user.service.UserService`
- **Repository:** `com.personalfin.server.user.repository.UserRepository`

**Features:**
- User registration with unique username and email
- Role-based access control (RBAC)
- User roles: `USER`, `ADMIN`
- Password hashing using BCrypt (10 rounds)
- User enablement/disablement support

**Database Schema:**
- `users` table with UUID primary key
- `roles` as element collection (enum-based)
- Indexes on username and email for fast lookups

### 1.3 Password Security

**Implementation:**
- **Encoder:** BCryptPasswordEncoder (Spring Security)
- **Hashing:** BCrypt with 10 strength rounds
- **Storage:** Only hashed passwords stored in database

**Security Properties:**
- One-way hashing (cannot be reversed)
- Salt automatically generated per password
- Resistant to rainbow table attacks
- Slow by design (prevents brute force)

### 1.4 Authentication Endpoints

**Registration:**
- `POST /api/auth/register`
- Validates username uniqueness
- Validates email uniqueness
- Creates user with hashed password
- Returns JWT token upon successful registration

**Login:**
- `POST /api/auth/login`
- Validates credentials
- Returns JWT token upon successful authentication
- Records login attempts (success/failure)

---

## Phase 2: Secure Configuration & Secrets Management

### 2.1 Environment-Based Configuration

**Implementation:**
- **Files:** `application.yml`, `application-dev.yml`, `application-prod.yml`
- **Profile Support:** Development and Production profiles

**Features:**
- Sensitive data externalized to environment variables
- Profile-specific configurations
- Default values for development
- Strict requirements for production

**Environment Variables:**
```bash
# Database
DB_URL=jdbc:postgresql://localhost:5432/personal_finance
DB_USERNAME=postgres
DB_PASSWORD=<secure-password>

# JWT
JWT_SECRET=<256-bit-random-secret>
JWT_EXPIRATION=86400000
JWT_ISSUER=personal-finance-server

# Server
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=prod
```

### 2.2 Secrets Management

**Best Practices:**
- Never commit secrets to version control
- `.env` files in `.gitignore`
- Use secret management services in production:
  - AWS Secrets Manager
  - HashiCorp Vault
  - Azure Key Vault
  - Kubernetes Secrets

**JWT Secret Generation:**
```bash
openssl rand -base64 32
```

### 2.3 Configuration Properties

**Location:** `com.personalfin.server.auth.config.JwtProperties`

**Features:**
- Type-safe configuration binding
- Validation of required properties
- Default values for optional properties

---

## Phase 3: API Security Enhancements

### 3.1 CORS (Cross-Origin Resource Sharing)

**Implementation:**
- **Location:** `com.personalfin.server.config.SecurityConfig.corsConfigurationSource()`

**Configuration:**
```yaml
cors:
  allowed-origins: http://localhost:5173,http://localhost:3000,http://localhost:3001
```

**Features:**
- Whitelist-based origin control
- Configurable allowed methods (GET, POST, PUT, DELETE, OPTIONS, PATCH)
- Credential support for authenticated requests
- Preflight request handling
- Max age configuration (1 hour)

**Security:**
- Only specified origins allowed
- Credentials only sent to trusted origins
- Prevents unauthorized cross-origin requests

### 3.2 CSRF (Cross-Site Request Forgery) Protection

**Implementation:**
- **Location:** `com.personalfin.server.config.SecurityConfig`

**Configuration:**
- CSRF disabled for `/api/**` endpoints (stateless JWT authentication)
- Cookie-based token repository for stateful endpoints
- Custom request handler for token processing

**Rationale:**
- JWT tokens in Authorization header are not vulnerable to CSRF
- Stateless authentication doesn't require CSRF protection
- Reduces complexity for API clients

### 3.3 Rate Limiting

**Implementation:**
- **Config:** `com.personalfin.server.config.RateLimitingConfig`
- **Filter:** `com.personalfin.server.config.filter.RateLimitingFilter`
- **Library:** Bucket4j with Caffeine cache

**Features:**
- Per-user rate limiting (authenticated users)
- Per-IP rate limiting (unauthenticated users)
- Different limits for authentication endpoints vs. API endpoints
- Token bucket algorithm with burst capacity
- In-memory cache for bucket storage (10,000 entries, 1-minute TTL)

**Configuration:**
```yaml
rate-limit:
  auth-requests-per-minute: 5
  api-requests-per-minute: 100
  burst-capacity: 10
```

**Rate Limits:**
- **Authentication endpoints:** 5 requests/minute
- **API endpoints:** 100 requests/minute
- **Burst capacity:** 10 additional requests

**Response:**
- HTTP 429 (Too Many Requests) when limit exceeded
- JSON error message: `{"error":"Too many requests. Please try again later."}`

### 3.4 Security Headers

**Implementation:**
- **Filter:** `com.personalfin.server.config.SecurityHeadersConfig`

**Headers Set:**

1. **X-Frame-Options: DENY**
   - Prevents clickjacking attacks
   - Blocks embedding in iframes

2. **X-Content-Type-Options: nosniff**
   - Prevents MIME type sniffing
   - Forces browsers to respect Content-Type header

3. **X-XSS-Protection: 1; mode=block**
   - Enables browser XSS protection
   - Blocks pages when XSS detected

4. **Content-Security-Policy (CSP)**
   - **Development:** More permissive (allows localhost connections)
   - **Production:** Strict (only 'self')
   - Prevents XSS, data injection, and other attacks

5. **Referrer-Policy: strict-origin-when-cross-origin**
   - Controls referrer information sent
   - Balances privacy and functionality

6. **Permissions-Policy**
   - Disables geolocation, microphone, camera, payment, USB
   - Prevents unauthorized access to device features

7. **Strict-Transport-Security (HSTS)**
   - Only set for HTTPS connections
   - Forces HTTPS for 1 year
   - Includes subdomains

**CSP Configuration:**
- **Development:** Allows localhost connections for development
- **Production:** Strict 'self' only policy

---

## Phase 4: Advanced Security Features

### 4.1 Password Strength Validation

**Implementation:**
- **Validator:** `com.personalfin.server.security.validation.PasswordValidator`

**Password Requirements:**
- Minimum 8 characters
- Maximum 128 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one digit
- At least one special character (@$!%*?&)
- Not a common password

**Common Password Detection:**
- Checks against list of common passwords
- Case-insensitive matching
- Prevents weak password usage

**Usage:**
- Validated during user registration
- Returns detailed error messages for failed validation
- Integrated into `UserService.createUser()`

**Example Validation Result:**
```java
PasswordValidationResult result = passwordValidator.validate(password);
if (!result.isValid()) {
    throw new IllegalArgumentException(result.getErrorMessage());
}
```

### 4.2 Account Lockout Mechanism

**Implementation:**
- **Service:** `com.personalfin.server.security.service.AccountLockoutService`
- **Model:** `com.personalfin.server.security.model.LoginAttempt`
- **Repository:** `com.personalfin.server.security.repository.LoginAttemptRepository`

**Features:**
- Tracks failed login attempts per username
- Tracks failed login attempts per IP address
- Automatic account lockout after threshold exceeded
- Configurable lockout duration
- Sliding window for attempt counting

**Configuration:**
```yaml
security:
  account-lockout:
    max-attempts: 5
    lockout-duration-minutes: 30
    window-minutes: 15
```

**Behavior:**
- **User Lockout:** After 5 failed attempts in 15 minutes
- **IP Blocking:** After 10 failed attempts from same IP (2x user threshold)
- **Lockout Duration:** 30 minutes
- **Window:** 15-minute sliding window for counting attempts

**Database Schema:**
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

**Indexes:**
- `idx_login_attempts_username` - Fast lookup by username
- `idx_login_attempts_ip_address` - Fast lookup by IP
- `idx_login_attempts_created_at` - Time-based queries

**Integration:**
- Checks lockout status before authentication
- Records all login attempts (success and failure)
- Throws `AccountLockedException` when locked
- Returns remaining attempts in error response

### 4.3 Audit Logging

**Implementation:**
- **Service:** `com.personalfin.server.security.service.AuditLogService`
- **Model:** `com.personalfin.server.security.model.AuditLog`
- **Repository:** `com.personalfin.server.security.repository.AuditLogRepository`

**Features:**
- Comprehensive security event logging
- User activity tracking
- IP address logging
- Success/failure tracking
- Timestamp for all events

**Event Types:**
- `AUTHENTICATION` - Login attempts
- `REGISTRATION` - User registrations
- `SENSITIVE_OPERATION` - Critical operations
- `DATA_ACCESS` - Resource access

**Database Schema:**
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

**Indexes:**
- `idx_audit_logs_user_id` - User activity queries
- `idx_audit_logs_event_type` - Event type filtering
- `idx_audit_logs_created_at` - Time-based queries

**Logged Events:**
1. **Authentication:**
   - Successful logins
   - Failed login attempts (with reason)
   - Account lockouts

2. **Registration:**
   - Successful registrations
   - Failed registrations (with reason)

3. **Sensitive Operations:**
   - Password changes
   - Account modifications
   - Data exports

4. **Data Access:**
   - Resource access with IDs
   - Bulk operations

**Usage Example:**
```java
auditLogService.logAuthentication(username, ipAddress, true, null);
auditLogService.logSensitiveOperation("PASSWORD_CHANGE", userId, username, ipAddress, "Password updated");
```

### 4.4 Input Sanitization & Validation

**Implementation:**
- **Filter:** `com.personalfin.server.security.filter.InputSanitizationFilter`
- **Utils:** `com.personalfin.server.security.util.SecurityUtils`

**Protection Against:**

1. **SQL Injection:**
   - Pattern detection for SQL keywords
   - Blocks: UNION, SELECT, INSERT, UPDATE, DELETE, DROP, CREATE, ALTER, EXEC, EXECUTE
   - Case-insensitive matching

2. **XSS (Cross-Site Scripting):**
   - Pattern detection for script tags
   - Blocks: `<script>`, `</script>`, `javascript:`, `onerror=`, `onload=`, `onclick=`, `<iframe>`
   - Character sanitization (removes `<>"'&`)

3. **Path Traversal:**
   - Pattern detection for directory traversal
   - Blocks: `../`, `..\`, URL-encoded variants
   - Prevents access to unauthorized files

**Sanitization Methods:**
- `sanitizeInput()` - Removes dangerous characters
- `containsSqlInjection()` - Detects SQL injection patterns
- `containsXss()` - Detects XSS patterns
- `containsPathTraversal()` - Detects path traversal patterns
- `sanitizeFileName()` - Sanitizes file names
- `urlDecode()` - Safe URL decoding

**Filter Behavior:**
- Checks all query parameters
- Validates parameter names and values
- Validates request URI
- Throws `SecurityException` on detection
- Applied before authentication filter

**Example:**
```java
// Blocks: SELECT * FROM users
if (SecurityUtils.containsSqlInjection(input)) {
    throw new SecurityException("Invalid input detected");
}
```

### 4.5 Secure Error Handling

**Implementation:**
- **Handler:** `com.personalfin.server.security.exception.GlobalExceptionHandler`

**Features:**
- Centralized exception handling
- No information leakage in error messages
- Consistent error response format
- Appropriate HTTP status codes
- Detailed logging for debugging

**Exception Handling:**

1. **AccountLockedException:**
   - HTTP 429 (Too Many Requests)
   - Returns remaining attempts
   - Returns lockout duration

2. **SecurityException:**
   - HTTP 403 (Forbidden)
   - Generic error message
   - No details about security violation

3. **BadCredentialsException:**
   - HTTP 401 (Unauthorized)
   - Generic "Invalid username or password"
   - Doesn't reveal which field is wrong

4. **MethodArgumentNotValidException:**
   - HTTP 400 (Bad Request)
   - Returns validation errors per field
   - No sensitive information exposed

5. **MaxUploadSizeExceededException:**
   - HTTP 413 (Payload Too Large)
   - Generic file size error

6. **Generic Exceptions:**
   - HTTP 500 (Internal Server Error)
   - Generic error message
   - Full stack trace logged server-side only

**Error Response Format:**
```json
{
  "error": "Error type",
  "message": "User-friendly message",
  "timestamp": "2025-01-15T10:30:00Z",
  "errors": { /* field-specific errors */ }
}
```

**Security Benefits:**
- Prevents information disclosure
- Doesn't reveal system internals
- Consistent error format
- Appropriate status codes

### 4.6 Request Size Limits

**Implementation:**
- **Configuration:** `application-dev.yml`

**Configuration:**
```yaml
spring:
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB
```

**Features:**
- Limits file upload size
- Limits total request size
- Prevents DoS attacks via large payloads
- Automatic validation by Spring Boot

**Protection:**
- Prevents memory exhaustion
- Prevents disk space exhaustion
- Limits resource consumption
- Returns HTTP 413 on violation

---

## Security Architecture

### Filter Chain Order

The security filters are applied in the following order:

1. **InputSanitizationFilter** - Validates and sanitizes input
2. **SecurityHeadersConfig** - Sets security headers
3. **RateLimitingFilter** - Enforces rate limits
4. **JwtAuthenticationFilter** - Authenticates JWT tokens
5. **Spring Security Filters** - Authorization and CSRF

### Request Flow

```
Client Request
    ↓
InputSanitizationFilter (Phase 4)
    ↓
SecurityHeadersConfig (Phase 3)
    ↓
RateLimitingFilter (Phase 3)
    ↓
JwtAuthenticationFilter (Phase 1)
    ↓
Spring Security Authorization (Phase 1)
    ↓
Controller/Endpoint
    ↓
Response
```

### Database Security

**User Data Isolation:**
- All tables include `user_id` column
- Queries filtered by authenticated user ID
- Prevents unauthorized data access
- UUID-based user identification

**SQL Injection Prevention:**
- JPA/Hibernate parameterized queries
- No raw SQL with user input
- Input validation before database access
- Pattern detection in filter layer

---

## Configuration

### Environment Variables

**Required for Production:**
```bash
# Database
DB_URL=jdbc:postgresql://localhost:5432/personal_finance
DB_USERNAME=postgres
DB_PASSWORD=<secure-password>

# JWT
JWT_SECRET=<256-bit-random-secret>
JWT_EXPIRATION=86400000
JWT_ISSUER=personal-finance-server

# Server
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=prod
```

**Optional:**
```bash
# Rate Limiting
RATE_LIMIT_AUTH=5
RATE_LIMIT_API=100
RATE_LIMIT_BURST=10

# Account Lockout
SECURITY_MAX_ATTEMPTS=5
SECURITY_LOCKOUT_DURATION=30
SECURITY_WINDOW_MINUTES=15

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000
```

### Application Configuration

**Development Profile (`application-dev.yml`):**
- Permissive CORS for localhost
- Debug logging enabled
- Default JWT secret (not for production)
- SQL formatting enabled

**Production Profile (`application-prod.yml`):**
- Strict CORS configuration
- Reduced logging
- Requires all environment variables
- SQL formatting disabled

---

## Best Practices

### 1. Password Security
- ✅ Strong password requirements enforced
- ✅ BCrypt hashing (10 rounds)
- ✅ No password storage in plain text
- ✅ Common password detection

### 2. Authentication
- ✅ JWT tokens with expiration
- ✅ Stateless authentication
- ✅ Token validation on every request
- ✅ Secure token storage (not in localStorage for production)

### 3. Authorization
- ✅ Role-based access control
- ✅ User data isolation
- ✅ Principle of least privilege
- ✅ Method-level security support

### 4. Input Validation
- ✅ Input sanitization
- ✅ SQL injection prevention
- ✅ XSS prevention
- ✅ Path traversal prevention
- ✅ Request size limits

### 5. Rate Limiting
- ✅ Per-user rate limiting
- ✅ Per-IP rate limiting
- ✅ Different limits for auth vs. API
- ✅ Burst capacity support

### 6. Account Security
- ✅ Account lockout after failed attempts
- ✅ IP-based blocking
- ✅ Login attempt tracking
- ✅ Audit logging

### 7. Error Handling
- ✅ No information leakage
- ✅ Consistent error format
- ✅ Appropriate HTTP status codes
- ✅ Server-side logging

### 8. Security Headers
- ✅ X-Frame-Options
- ✅ X-Content-Type-Options
- ✅ X-XSS-Protection
- ✅ Content-Security-Policy
- ✅ Referrer-Policy
- ✅ Permissions-Policy
- ✅ HSTS (for HTTPS)

### 9. Secrets Management
- ✅ Environment variables for secrets
- ✅ No secrets in code
- ✅ Profile-based configuration
- ✅ `.env` in `.gitignore`

### 10. Monitoring & Auditing
- ✅ Comprehensive audit logging
- ✅ Security event tracking
- ✅ Login attempt logging
- ✅ Sensitive operation logging

---

## Security Checklist

### Authentication & Authorization
- [x] JWT token-based authentication
- [x] Password hashing (BCrypt)
- [x] Role-based access control
- [x] User data isolation
- [x] Token expiration

### Input Validation
- [x] SQL injection prevention
- [x] XSS prevention
- [x] Path traversal prevention
- [x] Request size limits
- [x] Input sanitization

### Rate Limiting & Account Security
- [x] Rate limiting per user
- [x] Rate limiting per IP
- [x] Account lockout mechanism
- [x] Login attempt tracking
- [x] IP-based blocking

### Security Headers
- [x] X-Frame-Options
- [x] X-Content-Type-Options
- [x] X-XSS-Protection
- [x] Content-Security-Policy
- [x] Referrer-Policy
- [x] Permissions-Policy
- [x] HSTS

### Configuration & Secrets
- [x] Environment-based configuration
- [x] Secrets externalization
- [x] Profile support
- [x] Secure defaults

### Error Handling
- [x] Secure error messages
- [x] No information leakage
- [x] Consistent error format
- [x] Appropriate status codes

### Audit & Logging
- [x] Security event logging
- [x] Authentication logging
- [x] Sensitive operation logging
- [x] Data access logging

---

## Future Enhancements

### Potential Additions:
1. **Two-Factor Authentication (2FA)**
   - TOTP support
   - SMS/Email verification

2. **Token Refresh Mechanism**
   - Refresh tokens
   - Token rotation

3. **Password Reset Flow**
   - Secure token-based reset
   - Email verification

4. **Session Management**
   - Active session tracking
   - Session revocation

5. **API Key Management**
   - API key generation
   - Key rotation

6. **Advanced Monitoring**
   - Security dashboard
   - Anomaly detection
   - Alert system

---

## Conclusion

The Personal Finance Tracker Server implements a comprehensive, multi-layered security approach covering:

- **Authentication & Authorization** (Phase 1)
- **Secure Configuration** (Phase 2)
- **API Security** (Phase 3)
- **Advanced Security Features** (Phase 4)

All security features follow industry best practices and are designed to protect against common web application vulnerabilities including:
- SQL Injection
- XSS (Cross-Site Scripting)
- CSRF (Cross-Site Request Forgery)
- Brute Force Attacks
- Account Takeover
- Information Disclosure
- Path Traversal
- DoS Attacks

The implementation is production-ready with proper configuration management, error handling, and audit logging.

---

**Last Updated:** January 2025
**Version:** 1.0
**Author:** Personal Finance Tracker Development Team


