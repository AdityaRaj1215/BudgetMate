# Security Configuration Guide

## Environment Variables

This application uses environment variables for sensitive configuration. Never commit actual secrets to version control.

### Required Environment Variables

#### Database Configuration
- `DB_URL` - PostgreSQL connection URL (e.g., `jdbc:postgresql://localhost:5432/personal_finance`)
- `DB_USERNAME` - Database username
- `DB_PASSWORD` - Database password

#### JWT Configuration
- `JWT_SECRET` - Secret key for JWT token signing (minimum 256 bits / 32 characters)
  - **CRITICAL**: Must be a strong, random secret in production
  - Generate using: `openssl rand -base64 32`
- `JWT_EXPIRATION` - Token expiration time in milliseconds (default: 86400000 = 24 hours)
- `JWT_ISSUER` - Token issuer identifier (default: personal-finance-server)

#### Server Configuration
- `SERVER_PORT` - Server port (default: 8080)
- `SPRING_PROFILES_ACTIVE` - Active Spring profile (`dev` or `prod`)

### Optional Environment Variables

- `TIMEZONE` - Application timezone (default: Asia/Kolkata)
- `BUDGET_COACH_THRESHOLD` - Budget coach threshold percentage (default: 10)

## Setup Instructions

### 1. Copy Environment Template

```bash
cp .env.example .env
```

### 2. Edit .env File

Update the `.env` file with your actual configuration values:

```bash
# Edit with your preferred editor
nano .env
# or
vim .env
```

### 3. Generate JWT Secret

For production, generate a secure random secret:

```bash
openssl rand -base64 32
```

Add the generated secret to your `.env` file as `JWT_SECRET`.

### 4. Set Environment Variables

#### Linux/macOS
```bash
export $(cat .env | xargs)
```

#### Windows PowerShell
```powershell
Get-Content .env | ForEach-Object {
    if ($_ -match '^([^=]+)=(.*)$') {
        [Environment]::SetEnvironmentVariable($matches[1], $matches[2], 'Process')
    }
}
```

#### Windows CMD
```cmd
for /f "tokens=1,2 delims==" %a in (.env) do set %a=%b
```

### 5. Run Application

The application will automatically load environment variables. Make sure `.env` is in your `.gitignore` file.

## Spring Profiles

### Development Profile (`dev`)

- Uses default values for missing environment variables
- Enables debug logging
- SQL formatting enabled
- Uses `application-dev.yml`

Activate with:
```bash
export SPRING_PROFILES_ACTIVE=dev
# or
java -Dspring.profiles.active=dev -jar app.jar
```

### Production Profile (`prod`)

- **Requires** all sensitive environment variables to be set
- Reduced logging
- SQL formatting disabled
- Uses `application-prod.yml`

Activate with:
```bash
export SPRING_PROFILES_ACTIVE=prod
# or
java -Dspring.profiles.active=prod -jar app.jar
```

## Security Best Practices

1. **Never commit `.env` files** - Already in `.gitignore`
2. **Use strong JWT secrets** - Minimum 256 bits (32 characters)
3. **Rotate secrets regularly** - Especially JWT secrets
4. **Use different secrets per environment** - Dev, staging, production
5. **Limit access to production secrets** - Use secret management tools (AWS Secrets Manager, HashiCorp Vault, etc.)
6. **Monitor for exposed secrets** - Use tools like GitGuardian or GitHub Secret Scanning

## Production Deployment

For production deployments, consider:

1. **Secret Management Services**
   - AWS Secrets Manager
   - HashiCorp Vault
   - Azure Key Vault
   - Google Secret Manager

2. **Container Orchestration**
   - Kubernetes Secrets
   - Docker Secrets
   - Environment variables in deployment configs

3. **CI/CD Integration**
   - Inject secrets during build/deploy
   - Never store secrets in CI/CD configuration files

## Troubleshooting

### Application fails to start with "JWT_SECRET not set"

- Ensure `.env` file exists and contains `JWT_SECRET`
- Verify environment variables are loaded correctly
- Check that `SPRING_PROFILES_ACTIVE=prod` is set if using production profile

### Database connection fails

- Verify `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` are set correctly
- Check database server is running and accessible
- Verify network connectivity and firewall rules

### Environment variables not loading

- Ensure `.env` file is in the project root directory
- Verify environment variables are exported before running the application
- Check that Spring Boot is configured to read environment variables






