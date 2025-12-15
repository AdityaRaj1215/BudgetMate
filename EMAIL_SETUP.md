# Email Configuration for OTP

This guide explains how to configure email for OTP delivery in the Personal Finance Tracker application.

## Quick Answer

**In Development Mode (Default):**
- If email is **NOT configured**, the OTP will be:
  - ✅ **Returned in the API response** (check `devOtp` field)
  - ✅ **Logged in the server console** (check application logs)
- You can test registration without email setup!

**In Production Mode:**
- Email **MUST** be configured to send OTPs
- OTPs will be sent via email only (not returned in response)

---

## Option 1: Development Mode (No Email Setup Required)

If you're just testing, you don't need to configure email. The OTP will be available in:

1. **API Response**: When you call `POST /api/auth/register/otp`, check the response:
   ```json
   {
     "message": "OTP generated successfully (Email disabled - Dev mode)",
     "expiresInSeconds": 600,
     "devOtp": "123456"  // <-- Your OTP is here!
   }
   ```

2. **Server Logs**: Check your console/terminal for:
   ```
   DEV MODE: Email disabled - OTP for user@example.com is 123456
   ```

---

## Option 2: Configure Email (Gmail Example)

To actually receive OTP emails, follow these steps:

### Step 1: Enable 2-Factor Authentication on Gmail
1. Go to your Google Account settings
2. Navigate to Security
3. Enable 2-Step Verification

### Step 2: Generate App Password
1. Go to Google Account → Security
2. Under "2-Step Verification", click "App passwords"
3. Select "Mail" and "Other (Custom name)"
4. Enter "Personal Finance Tracker" as the name
5. Click "Generate"
6. **Copy the 16-character password** (you'll need this)

### Step 3: Set Environment Variables

Create a `.env` file in the server root directory (or set environment variables):

```bash
# Enable email
EMAIL_ENABLED=true

# Gmail SMTP settings
EMAIL_HOST=smtp.gmail.com
EMAIL_PORT=587
EMAIL_USERNAME=your-email@gmail.com
EMAIL_PASSWORD=your-16-char-app-password  # The app password from Step 2
EMAIL_FROM=your-email@gmail.com
```

### Step 4: Restart the Application

After setting the environment variables, restart your Spring Boot application.

---

## Testing Email Configuration

1. **Request OTP**:
   ```bash
   POST /api/auth/register/otp
   {
     "email": "your-email@gmail.com"
   }
   ```

2. **Check Response**:
   - If email is configured: `"message": "OTP sent successfully to your email"` (no `devOtp` field)
   - If email is NOT configured: `"devOtp": "123456"` will be in the response

3. **Check Your Email Inbox**:
   - Subject: "Your Registration OTP - Personal Finance Tracker"
   - Contains the 6-digit OTP code

---

## Troubleshooting

### Email Not Sending?

1. **Check Logs**: Look for error messages in the console
2. **Verify App Password**: Make sure you're using the 16-character app password, not your regular Gmail password
3. **Check Firewall**: Ensure port 587 is not blocked
4. **Verify Credentials**: Double-check `EMAIL_USERNAME` and `EMAIL_PASSWORD`

### Common Errors

- **"Authentication failed"**: Wrong app password or username
- **"Connection timeout"**: Firewall blocking port 587
- **"Invalid credentials"**: Not using app password (must use app password, not regular password)

---

## Other Email Providers

### Outlook/Hotmail
```bash
EMAIL_HOST=smtp-mail.outlook.com
EMAIL_PORT=587
EMAIL_USERNAME=your-email@outlook.com
EMAIL_PASSWORD=your-password
```

### Yahoo
```bash
EMAIL_HOST=smtp.mail.yahoo.com
EMAIL_PORT=587
EMAIL_USERNAME=your-email@yahoo.com
EMAIL_PASSWORD=your-app-password
```

### Custom SMTP Server
```bash
EMAIL_HOST=your-smtp-server.com
EMAIL_PORT=587  # or 465 for SSL
EMAIL_USERNAME=your-username
EMAIL_PASSWORD=your-password
```

---

## Security Notes

- ⚠️ **Never commit `.env` file to version control**
- ⚠️ **Use app passwords, not your main account password**
- ⚠️ **In production, use environment variables or a secrets manager**
- ✅ **App passwords can be revoked if compromised**

---

## Summary

- **For Development/Testing**: No email setup needed - OTP is in API response and logs
- **For Production**: Configure email with app password for Gmail (or other SMTP server)
- **Check OTP**: Either in email inbox OR in API response `devOtp` field (dev mode only)
