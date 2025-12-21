# Password Validation Update

## ✅ Changes Made

### Problem
Password validation was happening **during registration** (after OTP verification), but users wanted **immediate feedback** when entering their password **before requesting OTP**.

### Solution
Password validation now happens **when requesting OTP**, providing immediate feedback to users before the OTP is sent.

---

## 📋 Updated Flow

### Before:
1. User requests OTP (no password validation)
2. User receives OTP
3. User registers with password → **Password validated here** ❌

### After:
1. User requests OTP with password → **Password validated here** ✅
2. User receives OTP (only if password is valid)
3. User registers with password → Password validated again (safety check)

---

## 🔧 Code Changes

### 1. Updated `OtpRequest` DTO
**File**: `src/main/java/com/personalfin/server/auth/dto/OtpRequest.java`

**Added**:
- `password` field (required, min 8 characters)

**Before**:
```java
public record OtpRequest(
    @NotBlank @Email String email
)
```

**After**:
```java
public record OtpRequest(
    @NotBlank @Email String email,
    @NotBlank @Size(min = 8) String password
)
```

### 2. Updated `AuthController.requestOtp()`
**File**: `src/main/java/com/personalfin/server/auth/web/AuthController.java`

**Added**:
- Password validation using `PasswordValidator`
- Email existence check (prevent duplicate registrations)
- Better error handling and audit logging

**New Validation**:
- ✅ Password strength validation (uppercase, lowercase, digit, special char)
- ✅ Password length validation (8-128 characters)
- ✅ Common password check
- ✅ Email already exists check

### 3. Updated API Documentation
**File**: `API_DOCUMENTATION.md`

**Added**:
- Complete documentation for `/api/auth/register/otp` endpoint
- Password requirements clearly listed
- Error response documentation

---

## 🎯 Password Validation Rules

When requesting OTP, password must meet:

1. **Minimum Length**: 8 characters
2. **Maximum Length**: 128 characters
3. **Uppercase Letter**: At least one (A-Z)
4. **Lowercase Letter**: At least one (a-z)
5. **Digit**: At least one (0-9)
6. **Special Character**: At least one (@$!%*?&)
7. **Not Common**: Cannot be a common password (password123, 12345678, etc.)

---

## 📝 API Usage

### Request OTP (with Password Validation)

```bash
POST /api/auth/register/otp
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "MySecureP@ss123"
}
```

**Success Response** (200 OK):
```json
{
  "message": "OTP sent successfully to your email",
  "expiresInSeconds": 600
}
```

**Error Response** (400 Bad Request):
```json
{
  "timestamp": "2024-01-01T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character (@$!%*?&)",
  "path": "/api/auth/register/otp"
}
```

### Complete Registration

```bash
POST /api/auth/register
Content-Type: application/json

{
  "username": "johndoe",
  "email": "user@example.com",
  "password": "MySecureP@ss123",
  "otp": "123456"
}
```

---

## ✅ Benefits

1. **Immediate Feedback**: Users know if their password is valid **before** waiting for OTP
2. **Better UX**: No wasted time waiting for OTP with an invalid password
3. **Early Validation**: Catches password issues at the first step
4. **Defense in Depth**: Password is still validated during registration as a safety check
5. **Prevents Duplicate Emails**: Checks if email already exists before sending OTP

---

## 🔒 Security Notes

- Password is **NOT stored** when requesting OTP (only validated)
- Password validation happens **twice**:
  1. At OTP request (immediate feedback)
  2. At registration (safety check)
- Email existence is checked to prevent duplicate registrations
- All validation errors are logged for audit purposes

---

## 🧪 Testing

### Test Valid Password:
```bash
POST /api/auth/register/otp
{
  "email": "test@example.com",
  "password": "ValidP@ss123"
}
```
✅ Should return OTP response

### Test Invalid Password (too short):
```bash
POST /api/auth/register/otp
{
  "email": "test@example.com",
  "password": "short"
}
```
❌ Should return: "Password must be at least 8 characters long"

### Test Invalid Password (missing requirements):
```bash
POST /api/auth/register/otp
{
  "email": "test@example.com",
  "password": "alllowercase123"
}
```
❌ Should return: "Password must contain at least one uppercase letter..."

### Test Duplicate Email:
```bash
POST /api/auth/register/otp
{
  "email": "existing@example.com",
  "password": "ValidP@ss123"
}
```
❌ Should return: "Email already registered"

---

## 📚 Related Files

- `src/main/java/com/personalfin/server/auth/dto/OtpRequest.java` - Updated DTO
- `src/main/java/com/personalfin/server/auth/web/AuthController.java` - Updated controller
- `src/main/java/com/personalfin/server/security/validation/PasswordValidator.java` - Password validator
- `API_DOCUMENTATION.md` - Updated API docs

---

## ✅ Summary

Password validation now happens **when requesting OTP**, providing immediate feedback to users. The password is validated again during registration as a safety check. This improves user experience by catching password issues early in the registration flow.

