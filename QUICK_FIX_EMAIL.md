# 🚨 QUICK FIX: Email Not Sending

## The Problem
Spring Boot **does NOT automatically load `.env` files**. Your email credentials aren't being loaded!

## ✅ Solution: Choose ONE Method

---

### **Method 1: Use PowerShell Script (Easiest)**

1. **Open PowerShell** in your project folder
2. **Run**: `.\setup-email.ps1`
3. **In the SAME PowerShell window**, start your Spring Boot app:
   ```powershell
   mvn spring-boot:run
   ```
   OR if using IDE, make sure to run it from this PowerShell session

✅ **Done!** Email should work now.

---

### **Method 2: Edit application-dev.yml Directly (Most Reliable)**

1. Open `src/main/resources/application-dev.yml`
2. Find lines 22-38 (the `spring.mail` section)
3. **Replace** with this (using YOUR credentials):

```yaml
  mail:
    enabled: true
    host: smtp.gmail.com
    port: 587
    username: adityarajchaudhary12@gmail.com
    password: hqkcywwierudbcuk
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
    from: adityarajchaudhary12@gmail.com
```

4. **Save** the file
5. **Restart** your Spring Boot application

✅ **Done!** Email should work now.

⚠️ **Note**: Don't commit this file with your password to Git!

---

### **Method 3: Set Environment Variables Manually**

1. **Open PowerShell**
2. **Run these commands** (one by one):

```powershell
$env:EMAIL_ENABLED="true"
$env:EMAIL_HOST="smtp.gmail.com"
$env:EMAIL_PORT="587"
$env:EMAIL_USERNAME="adityarajchaudhary12@gmail.com"
$env:EMAIL_PASSWORD="hqkcywwierudbcuk"
$env:EMAIL_FROM="adityarajchaudhary12@gmail.com"
```

3. **In the SAME PowerShell window**, start your app
4. ✅ **Done!**

---

## ✅ Verify It's Working

1. **Start your application**
2. **Visit**: `http://localhost:8080/api/email/diagnostic/config`
3. **Check the response**:
   - ✅ `"status": "✅ READY"` → Email is configured!
   - ❌ `"status": "❌ NOT CONFIGURED"` → Try Method 2 (edit yml file)

---

## 🧪 Test Email Sending

```bash
POST http://localhost:8080/api/auth/register/otp
Content-Type: application/json

{
  "email": "adityarajchaudhary12@gmail.com"
}
```

**Expected Response:**
```json
{
  "message": "OTP sent successfully to your email",
  "expiresInSeconds": 600
}
```

**Check your email inbox!** 📧

---

## 🐛 Still Not Working?

1. **Check application logs** for:
   - `✅ Email configuration loaded` → Good!
   - `⚠️ Email is enabled but credentials are not set!` → Credentials not loaded

2. **Check diagnostic endpoint**: `http://localhost:8080/api/email/diagnostic/config`
   - See what's actually configured

3. **Try Method 2** (edit yml file) - it's the most reliable

4. **Restart everything**:
   - Close IDE
   - Close all terminals
   - Reopen and start application

---

## 📝 Why This Happens

Spring Boot reads configuration from:
1. `application.yml` / `application-dev.yml` ✅
2. Environment variables ✅
3. **NOT `.env` files** ❌ (unless you add a library)

So either:
- Set environment variables manually, OR
- Edit `application-dev.yml` directly


