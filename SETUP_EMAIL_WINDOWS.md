# Quick Email Setup for Windows

## ⚠️ IMPORTANT: Spring Boot doesn't automatically load `.env` files!

You need to set environment variables manually. Here are 3 ways to do it:

---

## Method 1: Set Environment Variables in PowerShell (Temporary - Current Session Only)

1. **Open PowerShell** in your project directory
2. **Run these commands** (replace with your actual values):

```powershell
$env:EMAIL_ENABLED="true"
$env:EMAIL_HOST="smtp.gmail.com"
$env:EMAIL_PORT="587"
$env:EMAIL_USERNAME="adityarajchaudhary12@gmail.com"
$env:EMAIL_PASSWORD="hqkcywwierudbcuk"
$env:EMAIL_FROM="adityarajchaudhary12@gmail.com"
```

3. **Start your Spring Boot application** in the SAME PowerShell window
4. ⚠️ **Note**: These variables only last for this PowerShell session. Close PowerShell = variables are gone.

---

## Method 2: Set Environment Variables Permanently (Windows)

1. **Press `Win + R`**, type `sysdm.cpl`, press Enter
2. Click **"Advanced"** tab
3. Click **"Environment Variables"** button
4. Under **"User variables"**, click **"New"**
5. Add each variable one by one:

   **Variable 1:**
   - Name: `EMAIL_ENABLED`
   - Value: `true`

   **Variable 2:**
   - Name: `EMAIL_HOST`
   - Value: `smtp.gmail.com`

   **Variable 3:**
   - Name: `EMAIL_PORT`
   - Value: `587`

   **Variable 4:**
   - Name: `EMAIL_USERNAME`
   - Value: `adityarajchaudhary12@gmail.com`

   **Variable 5:**
   - Name: `EMAIL_PASSWORD`
   - Value: `hqkcywwierudbcuk`

   **Variable 6:**
   - Name: `EMAIL_FROM`
   - Value: `adityarajchaudhary12@gmail.com`

6. Click **OK** on all dialogs
7. **Restart your IDE/terminal** (important!)
8. **Start your Spring Boot application**

---

## Method 3: Edit application-dev.yml Directly (Easiest but Less Secure)

1. Open `src/main/resources/application-dev.yml`
2. Find the `spring.mail` section (around line 22)
3. Replace it with:

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

4. **Save the file**
5. **Restart your Spring Boot application**

⚠️ **Warning**: This puts your password in the code. Don't commit this to Git!

---

## ✅ Verify Configuration

After setting up, test it:

1. **Start your application**
2. **Visit**: `http://localhost:8080/api/email/diagnostic/config`
3. **Check the response**:
   - ✅ If `status: "✅ READY"` → Email is configured correctly!
   - ❌ If `status: "❌ NOT CONFIGURED"` → Check the error message

---

## 🐛 Troubleshooting

### Still showing "NOT CONFIGURED"?

1. **Check if variables are set:**
   - Open PowerShell
   - Run: `$env:EMAIL_USERNAME`
   - Should show your email, not empty

2. **Restart everything:**
   - Close IDE
   - Close all terminals
   - Reopen and start application

3. **Check application logs:**
   - Look for: `✅ Email configuration loaded` or `⚠️ Email is enabled but credentials are not set!`

4. **Try Method 3** (edit application-dev.yml directly) - it's the most reliable

---

## 📝 Quick Test

After setup, test OTP:

```bash
POST http://localhost:8080/api/auth/register/otp
Content-Type: application/json

{
  "email": "adityarajchaudhary12@gmail.com"
}
```

**Expected Response (if email works):**
```json
{
  "message": "OTP sent successfully to your email",
  "expiresInSeconds": 600
}
```

**Check your email inbox!** 📧


