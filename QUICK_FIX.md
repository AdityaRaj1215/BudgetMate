# Quick Fix for Database Connection

## The Problem
PostgreSQL doesn't have a user "root" by default. You need to either:
1. Create a "root" user in PostgreSQL, OR
2. Use the default "postgres" user with the correct password

## Solution Options

### Option 1: Create Root User (Recommended if you want to use root/root)

1. **Open pgAdmin** (PostgreSQL GUI) or **psql** command line

2. **Connect as the default 'postgres' user** (you'll need the password you set during PostgreSQL installation)

3. **Run this SQL:**
```sql
CREATE USER root WITH PASSWORD 'root';
CREATE DATABASE personal_finance;
GRANT ALL PRIVILEGES ON DATABASE personal_finance TO root;
```

4. **Or use the provided script:**
   - Open pgAdmin
   - Right-click on your PostgreSQL server → Query Tool
   - Copy and paste the contents of `setup-database.sql`
   - Execute

### Option 2: Use Default Postgres User

1. **Find your PostgreSQL password** (the one you set during installation)

2. **Update `application.yml`:**
```yaml
spring:
  datasource:
    username: postgres
    password: your_actual_postgres_password
```

3. **Make sure the database exists:**
```sql
CREATE DATABASE personal_finance;
```

### Option 3: Reset Postgres Password (If you forgot it)

**Windows:**
1. Stop PostgreSQL service
2. Edit `pg_hba.conf` (usually in `C:\Program Files\PostgreSQL\XX\data\`)
3. Change `md5` to `trust` for local connections
4. Start PostgreSQL service
5. Connect without password: `psql -U postgres`
6. Reset password: `ALTER USER postgres WITH PASSWORD 'newpassword';`
7. Change `pg_hba.conf` back to `md5`
8. Restart PostgreSQL service

## Quick Test

After setting up, test the connection:

**Using pgAdmin:**
- Try connecting with your credentials

**Using psql (if in PATH):**
```bash
psql -U root -d personal_finance
# or
psql -U postgres -d personal_finance
```

## Current Configuration

Your `application.yml` is set to:
- Username: `root`
- Password: `root`
- Database: `personal_finance`

Make sure these match your PostgreSQL setup!





