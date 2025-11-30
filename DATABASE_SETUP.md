# Database Setup Guide

## PostgreSQL Configuration

The application requires PostgreSQL to be running. Follow these steps:

### 1. Install PostgreSQL (if not already installed)
- Download from: https://www.postgresql.org/download/
- Or use: `choco install postgresql` (Windows with Chocolatey)

### 2. Create Database

Open PostgreSQL command line (psql) or pgAdmin and run:

```sql
CREATE DATABASE personal_finance;
```

### 3. Set Environment Variables

The application uses environment variables for database credentials. Set them before running:

**Windows (PowerShell):**
```powershell
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="your_actual_password"
```

**Windows (Command Prompt):**
```cmd
set DB_USERNAME=postgres
set DB_PASSWORD=your_actual_password
```

**Linux/Mac:**
```bash
export DB_USERNAME=postgres
export DB_PASSWORD=your_actual_password
```

### 4. Alternative: Update application.yml Directly

If you prefer not to use environment variables, you can directly edit `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    username: postgres
    password: your_actual_password
```

### 5. Verify Database Connection

Test your connection:
```sql
psql -U postgres -d personal_finance
```

### 6. Run the Application

Once the database is set up and credentials are configured, run the application. Flyway will automatically:
- Create all required tables
- Run database migrations
- Set up the schema

### Troubleshooting

**Error: "password authentication failed"**
- Verify your PostgreSQL password is correct
- Check if PostgreSQL service is running
- Ensure the database `personal_finance` exists

**Error: "database does not exist"**
- Create the database: `CREATE DATABASE personal_finance;`

**Error: "connection refused"**
- Check if PostgreSQL is running on port 5432
- Verify connection string in application.yml

### Default Configuration

If environment variables are not set, the application defaults to:
- Username: `postgres`
- Password: `postgres`
- Database: `personal_finance`
- Host: `localhost`
- Port: `5432`





