# Email Configuration - Step by Step Guide

This guide will walk you through configuring email for OTP delivery in your Personal Finance Tracker application.

---

## 📋 Step-by-Step Instructions

### **Step 1: Choose Your Email Provider**

You have 3 options:

#### **Option A: Gmail (Recommended for Development)**
- Most common and easy to set up
- Requires App Password (not your regular password)

#### **Option B: Outlook/Hotmail**
- Similar setup to Gmail
- Uses regular password or app password

#### **Option C: Custom SMTP Server**
- For corporate emails or other providers
- Requires SMTP server details from your email provider

---

### **Step 2: Get Your Email Credentials**

#### **For Gmail Users:**

1. **Enable 2-Factor Authentication:**
   - Go to: https://myaccount.google.com/security
   - Click on "2-Step Verification"
   - Follow the prompts to enable it

2. **Generate App Password:**
   - Go to: https://myaccount.google.com/apppasswords
   - Or: Google Account → Security → 2-Step Verification → App passwords
   - Select "Mail" as the app
   - Select "Other (Custom name)" as device
   - Enter name: "Personal Finance Tracker"
   - Click "Generate"
   - **Copy the 16-character password** (looks like: `abcd efgh ijkl mnop`)
   - ⚠️ **Important**: Remove spaces when using it (use: `abcdefghijklmnop`)

3. **Note Your Email:**
   - Your Gmail address (e.g., `yourname@gmail.com`)

#### **For Outlook/Hotmail Users:**

1. Go to: https://account.microsoft.com/security
2. Enable 2-factor authentication (if not already enabled)
3. Generate an app password (similar to Gmail)
4. Or use your regular password (less secure)

#### **For Custom SMTP:**

1. Contact your email provider for:
   - SMTP server hostname
   - SMTP port (usually 587 or 465)
   - Username
   - Password
   - Whether SSL/TLS is required

---

### **Step 3: Configure in Your Application**

You have **2 ways** to configure email:

#### **Method 1: Environment Variables (Recommended)**

Create a `.env` file in your project root directory:

```bash
# Email Configuration
EMAIL_ENABLED=true
EMAIL_HOST=smtp.gmail.com
EMAIL_PORT=587
EMAIL_USERNAME=your-email@gmail.com
EMAIL_PASSWORD=your-16-char-app-password
EMAIL_FROM=your-email@gmail.com
```

**For Gmail:**
```bash
EMAIL_ENABLED=true
EMAIL_HOST=smtp.gmail.com
EMAIL_PORT=587
EMAIL_USERNAME=adityarajchaudhary12@gmail.com
EMAIL_PASSWORD=hqkcywwierudbcuk
EMAIL_FROM=adityarajchaudhary12@gmail.com
```

**For Outlook:**
```bash
EMAIL_ENABLED=true
EMAIL_HOST=smtp-mail.outlook.com
EMAIL_PORT=587
EMAIL_USERNAME=yourname@outlook.com
EMAIL_PASSWORD=your-app-password
EMAIL_FROM=yourname@outlook.com
```

**For Yahoo:**
```bash
EMAIL_ENABLED=true
EMAIL_HOST=smtp.mail.yahoo.com
EMAIL_PORT=587
EMAIL_USERNAME=yourname@yahoo.com
EMAIL_PASSWORD=your-app-password
EMAIL_FROM=yourname@yahoo.com
```

#### **Method 2: Direct Configuration in application-dev.yml**

Edit `src/main/resources/application-dev.yml`:

```yaml
spring:
  mail:
    enabled: true  # Change from ${EMAIL_ENABLED:true} to true
    host: smtp.gmail.com  # Change from ${EMAIL_HOST:smtp.gmail.com}
    port: 587  # Change from ${EMAIL_PORT:587}
    username: your-email@gmail.com  # Change from ${EMAIL_USERNAME:}
    password: your-16-char-app-password  # Change from ${EMAIL_PASSWORD:}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true
          connectiontimeout: 5000
          timeout: 5000
          writetimeout: 5000
    from: your-email@gmail.com  # Change from ${EMAIL_FROM:no-reply@personalfinance.com}
```

⚠️ **Warning**: Method 2 is less secure as credentials are in the code. Use Method 1 (environment variables) for production.

---

### **Step 4: Set Environment Variables (If Using Method 1)**

#### **On Windows (PowerShell):**

```powershell
# Set environment variables for current session
$env:EMAIL_ENABLED="true"
$env:EMAIL_HOST="smtp.gmail.com"
$env:EMAIL_PORT="587"
$env:EMAIL_USERNAME="your-email@gmail.com"
$env:EMAIL_PASSWORD="your-16-char-app-password"
$env:EMAIL_FROM="your-email@gmail.com"
```

#### **On Windows (Command Prompt):**

```cmd
set EMAIL_ENABLED=true
set EMAIL_HOST=smtp.gmail.com
set EMAIL_PORT=587
set EMAIL_USERNAME=your-email@gmail.com
set EMAIL_PASSWORD=your-16-char-app-password
set EMAIL_FROM=your-email@gmail.com
```

#### **On Linux/Mac:**

```bash
export EMAIL_ENABLED=true
export EMAIL_HOST=smtp.gmail.com
export EMAIL_PORT=587
export EMAIL_USERNAME=your-email@gmail.com
export EMAIL_PASSWORD=your-16-char-app-password
export EMAIL_FROM=your-email@gmail.com
```

#### **Permanent Setup (Windows):**

1. Right-click "This PC" → Properties
2. Click "Advanced system settings"
3. Click "Environment Variables"
4. Under "User variables", click "New"
5. Add each variable:
   - Variable name: `EMAIL_ENABLED`, Value: `true`
   - Variable name: `EMAIL_HOST`, Value: `smtp.gmail.com`
   - Variable name: `EMAIL_PORT`, Value: `587`
   - Variable name: `EMAIL_USERNAME`, Value: `your-email@gmail.com`
   - Variable name: `EMAIL_PASSWORD`, Value: `your-app-password`
   - Variable name: `EMAIL_FROM`, Value: `your-email@gmail.com`

#### **Using .env File (Recommended):**

1. Create a file named `.env` in your project root
2. Add the email configuration:
   ```bash
   EMAIL_ENABLED=true
   EMAIL_HOST=smtp.gmail.com
   EMAIL_PORT=587
   EMAIL_USERNAME=your-email@gmail.com
   EMAIL_PASSWORD=your-16-char-app-password
   EMAIL_FROM=your-email@gmail.com
   ```
3. Make sure `.env` is in `.gitignore` (never commit it!)
4. Your IDE or Spring Boot should automatically load it

---

### **Step 5: Restart Your Application**

After setting the configuration:

1. **Stop** your Spring Boot application (if running)
2. **Start** it again to load the new configuration
3. The application will read the environment variables on startup

---

### **Step 6: Test Email Configuration**

1. **Start your application**

2. **Test OTP Request:**
   ```bash
   POST http://localhost:8080/api/auth/register/otp
   Content-Type: application/json
   
   {
     "email": "your-email@gmail.com"
   }
   ```

3. **Check Response:**
   - ✅ **If email is configured correctly:**
     ```json
     {
       "message": "OTP sent successfully to your email",
       "expiresInSeconds": 600
     }
     ```
     - No `devOtp` field
     - Check your email inbox for the OTP
   
   - ⚠️ **If email is NOT configured:**
     ```json
     {
       "message": "OTP generated successfully (Email disabled - Dev mode)",
       "expiresInSeconds": 600,
       "devOtp": "123456"
     }
     ```
     - `devOtp` field is present
     - OTP is also in server logs

4. **Check Your Email:**
   - Subject: "Your Registration OTP - Personal Finance Tracker"
   - Body contains: 6-digit OTP code
   - Valid for 10 minutes

5. **Check Server Logs:**
   - Look for: `"OTP email sent successfully to your-email@gmail.com"`
   - Or errors if configuration is wrong

---

## 🔧 Configuration Values Explained

| Variable | Description | Example |
|----------|-------------|---------|
| `EMAIL_ENABLED` | Enable/disable email sending | `true` or `false` |
| `EMAIL_HOST` | SMTP server hostname | `smtp.gmail.com` |
| `EMAIL_PORT` | SMTP server port | `587` (TLS) or `465` (SSL) |
| `EMAIL_USERNAME` | Your email address | `yourname@gmail.com` |
| `EMAIL_PASSWORD` | App password (Gmail) or regular password | `abcdefghijklmnop` |
| `EMAIL_FROM` | Sender email address | `yourname@gmail.com` |

---

## 🐛 Troubleshooting

### **Error: "Authentication failed"**
- ✅ Check that you're using **App Password** (not regular password) for Gmail
- ✅ Verify username is correct (full email address)
- ✅ Make sure 2-factor authentication is enabled

### **Error: "Connection timeout"**
- ✅ Check firewall isn't blocking port 587
- ✅ Verify SMTP host and port are correct
- ✅ Try port 465 with SSL instead of 587 with TLS

### **Error: "Invalid credentials"**
- ✅ For Gmail: Must use App Password, not regular password
- ✅ Remove spaces from App Password
- ✅ Regenerate App Password if needed

### **OTP not received in email**
- ✅ Check spam/junk folder
- ✅ Verify email address is correct
- ✅ Check server logs for errors
- ✅ Make sure `EMAIL_ENABLED=true`

### **Still getting devOtp in response**
- ✅ Verify environment variables are set correctly
- ✅ Restart application after setting variables
- ✅ Check `EMAIL_ENABLED=true` is set
- ✅ Verify `EMAIL_USERNAME` and `EMAIL_PASSWORD` are not empty

---

## 📝 Quick Reference

### **Gmail Configuration (Copy-Paste Ready)**

```bash
EMAIL_ENABLED=true
EMAIL_HOST=smtp.gmail.com
EMAIL_PORT=587
EMAIL_USERNAME=your-email@gmail.com
EMAIL_PASSWORD=your-16-char-app-password
EMAIL_FROM=your-email@gmail.com
```

### **Development Mode (No Email)**

```bash
EMAIL_ENABLED=false
```

When `EMAIL_ENABLED=false`, OTP will be:
- ✅ Returned in API response (`devOtp` field)
- ✅ Logged in server console
- ❌ NOT sent via email

---

## ✅ Checklist

Before testing, make sure:

- [ ] 2-Factor Authentication is enabled (for Gmail)
- [ ] App Password is generated and copied
- [ ] Environment variables are set (or `.env` file created)
- [ ] `.env` file is in `.gitignore` (if using)
- [ ] Application is restarted after configuration
- [ ] Email address is correct
- [ ] No spaces in App Password
- [ ] Port 587 is not blocked by firewall

---

## 🎯 Summary

1. **Get App Password** from Gmail/Outlook
2. **Set Environment Variables** (or edit `application-dev.yml`)
3. **Restart Application**
4. **Test** by requesting OTP
5. **Check Email** inbox for OTP

That's it! Your email configuration is complete. 🎉


