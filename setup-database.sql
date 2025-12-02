-- PostgreSQL Database Setup Script
-- Run this script as a PostgreSQL superuser (usually 'postgres')

-- Create database if it doesn't exist
CREATE DATABASE personal_finance;

-- Connect to the database
\c personal_finance

-- Create a user 'root' with password 'root' (if you want to use root)
-- Option 1: Create root user
CREATE USER root WITH PASSWORD 'root';
GRANT ALL PRIVILEGES ON DATABASE personal_finance TO root;

-- Option 2: If you prefer to use 'postgres' user, just make sure it has the right password
-- ALTER USER postgres WITH PASSWORD 'your_password_here';

-- Grant all privileges on the database
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO root;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO root;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO root;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO root;








