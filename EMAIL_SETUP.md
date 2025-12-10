# Email Configuration Guide

This guide explains how to configure email sending for OTP verification during user registration.

## Overview

The application sends OTP codes via email to verify user email addresses during registration. The email service is configured through Spring Mail and supports multiple email providers.

## Configuration

### Environment Variables

Add the following environment variables to your `.env` file or environment:

```bash
# Email Configuration
EMAIL_ENABLED=true
EMAIL_HOST=smtp.gmail.com
EMAIL_PORT=587
EMAIL_USERNAME=your-email@gmail.com
EMAIL_PASSWORD=your-app-password
EMAIL_FROM=no-reply@personalfinance.com
```

### Gmail Setup (Recommended for Development)

1. **Enable 2-Factor Authentication** on your Gmail account
2. **Generate App Password:**
   - Go to Google Account settings
   - Security → 2-Step Verification → App passwords
   - Generate a new app password for "Mail"
   - Use this password in `EMAIL_PASSWORD`

3. **Configuration:**
```bash
EMAIL_HOST=smtp.gmail.com
EMAIL_PORT=587
EMAIL_USERNAME=your-email@gmail.com
EMAIL_PASSWORD=your-16-char-app-password
EMAIL_FROM=your-email@gmail.com
```

### Other Email Providers

#### Outlook/Hotmail
```bash
EMAIL_HOST=smtp-mail.outlook.com
EMAIL_PORT=587
EMAIL_USERNAME=your-email@outlook.com
EMAIL_PASSWORD=your-password
```

#### SendGrid
```bash
EMAIL_HOST=smtp.sendgrid.net
EMAIL_PORT=587
EMAIL_USERNAME=apikey
EMAIL_PASSWORD=your-sendgrid-api-key
EMAIL_FROM=your-verified-sender@domain.com
```

#### AWS SES
```bash
EMAIL_HOST=email-smtp.us-east-1.amazonaws.com
EMAIL_PORT=587
EMAIL_USERNAME=your-aws-access-key
EMAIL_PASSWORD=your-aws-secret-key
EMAIL_FROM=your-verified-email@domain.com
```

#### Mailtrap (Testing)
```bash
EMAIL_HOST=smtp.mailtrap.io
EMAIL_PORT=2525
EMAIL_USERNAME=your-mailtrap-username
EMAIL_PASSWORD=your-mailtrap-password
EMAIL_FROM=test@personalfinance.com
```

## Development Mode

In development mode (`spring.profiles.active=dev`), if email is disabled or fails to send, the OTP will be logged to the console for testing purposes:

```
WARN: DEV MODE: OTP for user@example.com is 123456
```

To disable email sending in development (for testing):
```bash
EMAIL_ENABLED=false
```

## Production Setup

For production, ensure:

1. **Use a reliable email service** (SendGrid, AWS SES, etc.)
2. **Set proper SPF/DKIM records** for your domain
3. **Use environment variables** for all email credentials
4. **Monitor email delivery** and handle failures gracefully
5. **Set up email bounce handling** if needed

## Testing

### Test Email Sending

1. Start the application with email configuration
2. Request an OTP:
```bash
POST /api/auth/register/otp
{
  "email": "test@example.com"
}
```

3. Check the email inbox for the OTP code
4. Use the OTP to register:
```bash
POST /api/auth/register
{
  "username": "testuser",
  "email": "test@example.com",
  "password": "SecurePass123!",
  "otp": "123456"
}
```

### Email Template

The OTP email includes:
- Subject: "Your Registration OTP - Personal Finance Tracker"
- Body: OTP code and expiration time (10 minutes)
- Professional formatting

## Troubleshooting

### Email Not Sending

1. **Check logs** for error messages
2. **Verify credentials** are correct
3. **Check firewall/network** allows SMTP connections
4. **Verify email provider** allows SMTP access
5. **Check spam folder** if emails are sent but not received

### Common Errors

**"Authentication failed"**
- Wrong username/password
- App password not used (for Gmail)
- 2FA not enabled (for Gmail)

**"Connection timeout"**
- Wrong SMTP host/port
- Firewall blocking SMTP
- Network connectivity issues

**"Email disabled"**
- Set `EMAIL_ENABLED=true`
- Check application logs

## Security Considerations

1. **Never commit email credentials** to version control
2. **Use app passwords** instead of main passwords (Gmail)
3. **Rotate credentials** regularly
4. **Use environment variables** for all sensitive data
5. **Monitor email sending** for abuse
6. **Rate limit** OTP requests to prevent spam

## Email Service Implementation

The email service (`EmailService`) handles:
- Sending OTP emails
- Error handling and logging
- Development mode fallback
- Email template generation

## Next Steps

- [ ] Configure email provider credentials
- [ ] Test email sending
- [ ] Set up email monitoring
- [ ] Configure SPF/DKIM records (production)
- [ ] Set up email bounce handling (production)

