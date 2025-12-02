# Personal Finance Tracker - Implementation Guide

## Overview

This document provides a comprehensive guide to all features implemented in the Personal Finance Tracker backend server. The application is built using Spring Boot 3.2.5 with Java 17, PostgreSQL database, and follows RESTful API design principles.

## Table of Contents

1. [Architecture](#architecture)
2. [Features Implemented](#features-implemented)
3. [Database Schema](#database-schema)
4. [API Endpoints](#api-endpoints)
5. [Configuration](#configuration)
6. [Dependencies](#dependencies)
7. [Usage Examples](#usage-examples)

---

## Architecture

### Technology Stack
- **Framework**: Spring Boot 3.2.5
- **Language**: Java 17
- **Database**: PostgreSQL (with H2 for testing)
- **ORM**: JPA/Hibernate
- **Migration**: Flyway
- **Security**: Spring Security (configured for CORS)
- **OCR**: Tesseract (Tess4j)

### Project Structure
```
src/main/java/com/personalfin/server/
├── budget/          # Budget and daily spend limit management
├── config/          # Security and web configuration
├── expense/         # Expense tracking and categorization
├── investment/      # Investment tracking (FD, SIP, RD, Gold, Crypto)
├── preferences/     # User preferences (theme, currency)
├── receipt/         # Receipt scanning and OCR
├── reminder/        # Bill reminders and notifications
├── savings/         # Savings goals tracking
└── split/           # Split bills and settlement tracking
```

---

## Features Implemented

### 1. Smart Expense Categorizer (Offline)

**Location**: `com.personalfin.server.expense.service.RuleBasedExpenseCategorizer`

**Features**:
- Rule-based categorization using keyword matching
- Fuzzy matching for typos (Levenshtein distance algorithm)
- Configurable categories via `application.yml`
- Confidence scoring for categorization

**How it works**:
1. Preloads keywords from configuration into a cache
2. Searches expense description and merchant name for keyword matches
3. Falls back to fuzzy matching if exact match not found
4. Returns category with confidence score

**Configuration** (`application.yml`):
```yaml
expense:
  categorizer:
    default-category: Uncategorized
    match-confidence: 0.92
    fallback-confidence: 0.35
    categories:
      Food:
        - swiggy
        - zomato
        - restaurant
      Travel:
        - uber
        - ola
        - metro
```

---

### 2. Bill & Subscription Reminder

**Location**: `com.personalfin.server.reminder`

**Features**:
- Track recurring bills (rent, subscriptions, EMIs)
- Automatic reminder notifications (3 days before and on due date)
- Support for multiple frequencies (weekly, monthly, quarterly, yearly, one-time)
- Mark bills as paid to automatically calculate next due date

**Entities**:
- `Bill`: Stores bill information
- `ReminderNotificationLog`: Tracks sent notifications

**Scheduled Job**: Runs daily at 6 AM (configurable via cron)

---

### 3. Cashflow Heatmap

**Location**: `com.personalfin.server.expense.service.ExpenseService.heatmap()`

**Features**:
- Visual representation of daily spending
- Color-coded levels based on spending thresholds
- GitHub contributions-style visualization

**Level Calculation**:
- Level 0: No spending
- Level 1-4: Based on configured thresholds (default: 500, 1500, 3000, 6000)
- Level 5+: Above highest threshold

---

### 4. Daily Spend Limit AI Coach

**Location**: `com.personalfin.server.budget`

**Features**:
- Monthly budget creation
- Automatic daily limit calculation (budget / days in month)
- Real-time spending tracking
- Overspend warnings with coaching messages
- Integration with expense creation

**Coaching Messages**:
- **INFO**: Within limit, shows remaining amount
- **WARNING**: 70-90% of limit used
- **CRITICAL**: Overspent, shows overspend amount

**Example Message**:
```
"You overspent by ₹450.00 today. Your daily limit was ₹1,000.00 but you spent ₹1,450.00."
```

---

### 5. Receipt Scanner → Auto Add Expense

**Location**: `com.personalfin.server.receipt`

**Features**:
- OCR text extraction from receipt images (base64 encoded)
- Automatic parsing of amount, merchant, date
- Auto-categorization using expense categorizer
- Direct expense creation from scanned receipt

**Technology**: Tesseract OCR (Tess4j library)

**Process**:
1. Receive base64 image data
2. Extract text using OCR
3. Parse amount using regex patterns
4. Extract merchant name (usually first line)
5. Parse date (multiple format support)
6. Auto-categorize using existing categorizer
7. Create expense automatically

---

### 6. Split Bills & Settlement (Without UPI)

**Location**: `com.personalfin.server.split`

**Features**:
- Create expense groups
- Split expenses among multiple members
- Track who owes whom
- Automatic settlement calculation
- Settlement tracking and marking

**Entities**:
- `ExpenseGroup`: Group of people sharing expenses
- `ExpenseShare`: Individual member's share of an expense
- `Settlement`: Tracks payments between members

**Settlement Algorithm**:
- Calculates net balance for each member
- Matches debtors with creditors
- Generates optimal settlement transactions

---

### 7. Savings Goals with Progress Bar

**Location**: `com.personalfin.server.savings`

**Features**:
- Create savings goals with target amount
- Track current progress
- Calculate progress percentage
- Add/withdraw amounts
- Set target dates

**Progress Calculation**:
```java
progressPercentage = (currentAmount / targetAmount) * 100
```

**Response includes**:
- Current amount
- Remaining amount
- Progress percentage (0-100%)

---

### 8. Expense Overlap Detector

**Location**: `com.personalfin.server.expense.service.ExpenseAnalyticsService`

**Features**:
- Weekly spending patterns (e.g., "You spend ₹1200 on food every Sunday")
- Recurring expense detection
- Monthly category comparisons
- Spending trend analysis

**Pattern Detection**:
- Analyzes expenses by day of week
- Identifies recurring merchants/categories
- Compares spending across time periods

---

### 9. Simple Investment Tracker (NOT Trading)

**Location**: `com.personalfin.server.investment`

**Features**:
- Track multiple investment types: FD, SIP, RD, Gold, Crypto
- Store principal amount and current value
- Calculate gains/loss automatically
- Track maturity dates
- Interest rate tracking

**Investment Types**:
- **FD**: Fixed Deposit
- **SIP**: Systematic Investment Plan
- **RD**: Recurring Deposit
- **GOLD**: Gold investments
- **CRYPTO**: Cryptocurrency

**Gains Calculation**:
```java
gains = currentValue - principalAmount
gainsPercentage = (gains / principalAmount) * 100
```

---

### 10. Dark Mode + Minimal UI Support

**Location**: `com.personalfin.server.preferences`

**Features**:
- User preferences API
- Theme selection (light/dark)
- Currency preference
- Per-user configuration

**Note**: This is backend API support. Frontend implementation required.

---

## Database Schema

### Core Tables

#### Expenses
```sql
CREATE TABLE expenses (
    id UUID PRIMARY KEY,
    description TEXT NOT NULL,
    merchant VARCHAR(120),
    category VARCHAR(60),
    amount NUMERIC(14, 2) NOT NULL,
    transaction_date DATE NOT NULL,
    payment_method VARCHAR(40),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
```

#### Budgets
```sql
CREATE TABLE budgets (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    amount NUMERIC(14, 2) NOT NULL,
    month_year DATE NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE daily_spend_limits (
    id UUID PRIMARY KEY,
    budget_id UUID NOT NULL REFERENCES budgets (id),
    date DATE NOT NULL,
    daily_limit NUMERIC(14, 2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    UNIQUE(budget_id, date)
);
```

#### Savings Goals
```sql
CREATE TABLE savings_goals (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    target_amount NUMERIC(14, 2) NOT NULL,
    current_amount NUMERIC(14, 2) NOT NULL DEFAULT 0,
    target_date DATE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
```

#### Investments
```sql
CREATE TABLE investments (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    type VARCHAR(20) NOT NULL,
    principal_amount NUMERIC(14, 2) NOT NULL,
    current_value NUMERIC(14, 2),
    interest_rate NUMERIC(5, 2),
    start_date DATE NOT NULL,
    maturity_date DATE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
```

#### Split Bills
```sql
CREATE TABLE expense_groups (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    description TEXT,
    created_by VARCHAR(100),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE expense_shares (
    id UUID PRIMARY KEY,
    group_id UUID NOT NULL REFERENCES expense_groups (id),
    expense_id UUID REFERENCES expenses (id),
    member_name VARCHAR(100) NOT NULL,
    amount NUMERIC(14, 2) NOT NULL,
    paid BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE settlements (
    id UUID PRIMARY KEY,
    group_id UUID NOT NULL REFERENCES expense_groups (id),
    from_member VARCHAR(100) NOT NULL,
    to_member VARCHAR(100) NOT NULL,
    amount NUMERIC(14, 2) NOT NULL,
    settled BOOLEAN NOT NULL DEFAULT FALSE,
    settled_at TIMESTAMPTZ,
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
```

#### User Preferences
```sql
CREATE TABLE user_preferences (
    id UUID PRIMARY KEY,
    user_id VARCHAR(100) UNIQUE,
    theme VARCHAR(20) DEFAULT 'light',
    currency VARCHAR(10) DEFAULT 'INR',
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
```

---

## API Endpoints

### Base URL
```
http://localhost:8080/api
```

### Expense Management

#### Create Expense
```http
POST /api/expenses
Content-Type: application/json

{
  "description": "Dinner at restaurant",
  "merchant": "Restaurant ABC",
  "amount": 500.00,
  "transactionDate": "2025-11-25",
  "category": "Food",
  "paymentMethod": "UPI"
}
```

#### Create Expense with Coach Message
```http
POST /api/expenses/with-coach
Content-Type: application/json

{
  "description": "Lunch",
  "merchant": "Swiggy",
  "amount": 250.00,
  "transactionDate": "2025-11-25"
}
```

**Response**:
```json
{
  "expense": { ... },
  "coachMessage": {
    "message": "You've spent ₹250.00 today. ₹750.00 remaining...",
    "type": "INFO",
    "overspendAmount": 0.00,
    "dailyLimit": 1000.00,
    "spentAmount": 250.00
  }
}
```

#### List All Expenses
```http
GET /api/expenses
```

#### Get Heatmap
```http
GET /api/expenses/heatmap?start=2025-11-01&end=2025-11-30
```

#### Get Category Spending
```http
GET /api/expenses/analytics/categories?start=2025-11-01&end=2025-11-30
```

#### Get Spending Patterns
```http
GET /api/expenses/analytics/patterns?start=2025-11-01&end=2025-11-30
```

#### Get Weekly Patterns
```http
GET /api/expenses/analytics/weekly?start=2025-11-01&end=2025-11-30
```

#### Monthly Comparison
```http
GET /api/expenses/analytics/comparison?month1Start=2025-10-01&month1End=2025-10-31&month2Start=2025-11-01&month2End=2025-11-30
```

### Budget Management

#### Create Budget
```http
POST /api/budgets
Content-Type: application/json

{
  "name": "November 2025 Budget",
  "amount": 30000.00,
  "monthYear": "2025-11-01"
}
```

#### Get Current Budget
```http
GET /api/budgets/current
```

#### Get Daily Limit
```http
GET /api/budgets/daily-limit?date=2025-11-25
```

#### Get Coach Message
```http
GET /api/budgets/coach?date=2025-11-25
```

### Savings Goals

#### Create Savings Goal
```http
POST /api/savings-goals
Content-Type: application/json

{
  "name": "Buy Laptop",
  "targetAmount": 70000.00,
  "targetDate": "2026-06-01"
}
```

#### Add Amount to Goal
```http
POST /api/savings-goals/{id}/add?amount=5000.00
```

#### Get Progress
```http
GET /api/savings-goals/{id}
```

**Response**:
```json
{
  "id": "...",
  "name": "Buy Laptop",
  "targetAmount": 70000.00,
  "currentAmount": 31500.00,
  "remainingAmount": 38500.00,
  "progressPercentage": 45.0,
  "targetDate": "2026-06-01",
  "active": true
}
```

### Investment Tracking

#### Create Investment
```http
POST /api/investments
Content-Type: application/json

{
  "name": "HDFC FD",
  "type": "FD",
  "principalAmount": 100000.00,
  "currentValue": 105000.00,
  "interestRate": 7.5,
  "startDate": "2025-01-01",
  "maturityDate": "2026-01-01",
  "notes": "Fixed deposit at HDFC Bank"
}
```

#### Update Current Value
```http
PUT /api/investments/{id}/current-value?currentValue=106000.00
```

#### List Investments by Type
```http
GET /api/investments?type=FD
```

#### Get Upcoming Maturities
```http
GET /api/investments/maturities?start=2025-12-01&end=2026-01-31
```

### Receipt Scanning

#### Scan Receipt
```http
POST /api/receipts/scan
Content-Type: application/json

{
  "imageData": "data:image/jpeg;base64,/9j/4AAQSkZJRg..."
}
```

**Response**:
```json
{
  "amount": 450.00,
  "merchant": "Swiggy",
  "date": "2025-11-25",
  "category": "Food",
  "rawText": "SWIGGY\nOrder #12345\nTotal: ₹450.00\n...",
  "expenseRequest": {
    "description": "Receipt from Swiggy",
    "merchant": "Swiggy",
    "amount": 450.00,
    "transactionDate": "2025-11-25",
    "category": "Food"
  }
}
```

#### Scan and Create Expense
```http
POST /api/receipts/scan-and-create
Content-Type: application/json

{
  "imageData": "data:image/jpeg;base64,..."
}
```

### Split Bills

#### Create Expense Group
```http
POST /api/split-bills/groups
Content-Type: application/json

{
  "name": "Trip to Goa",
  "description": "Expenses for Goa trip",
  "createdBy": "user123"
}
```

#### Split Expense
```http
POST /api/split-bills/split
Content-Type: application/json

{
  "groupId": "uuid-here",
  "expenseId": "expense-uuid",
  "amount": 2000.00,
  "memberShares": {
    "Alice": 800.00,
    "Bob": 600.00,
    "Charlie": 600.00
  }
}
```

#### Calculate Settlements
```http
POST /api/split-bills/settlements/calculate?groupId=uuid-here
```

#### Get Group with Balances
```http
GET /api/split-bills/groups/{id}
```

**Response**:
```json
{
  "id": "...",
  "name": "Trip to Goa",
  "memberBalances": [
    {
      "memberName": "Alice",
      "totalOwed": 1200.00,
      "totalPaid": 800.00
    },
    {
      "memberName": "Bob",
      "totalOwed": 600.00,
      "totalPaid": 600.00
    }
  ]
}
```

### User Preferences

#### Get Preferences
```http
GET /api/preferences/{userId}
```

#### Update Preferences
```http
PUT /api/preferences/{userId}
Content-Type: application/json

{
  "theme": "dark",
  "currency": "USD"
}
```

### Bill Reminders

#### Create Bill
```http
POST /api/bills
Content-Type: application/json

{
  "name": "Netflix Subscription",
  "category": "Entertainment",
  "amount": 649.00,
  "nextDueDate": "2025-12-01",
  "frequency": "MONTHLY",
  "remindDaysBefore": 3
}
```

#### Mark Bill as Paid
```http
POST /api/bills/{id}/mark-paid
Content-Type: application/json

{
  "paidDate": "2025-11-25"
}
```

#### List Bills
```http
GET /api/bills
```

---

## Configuration

### Application Configuration (`application.yml`)

```yaml
spring:
  application:
    name: personal-finance-server
  datasource:
    url: jdbc:postgresql://localhost:5432/personal_finance
    username: postgres
    password: postgres
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        format_sql: true
    open-in-view: false
  flyway:
    enabled: true
    locations: classpath:db/migration

server:
  port: 8080

logging:
  level:
    org.springframework: INFO

# Reminder Configuration
reminder:
  scheduler:
    cron: "0 0 6 * * *"  # Daily at 6 AM
    zone-id: "Asia/Kolkata"
  notification:
    days-before: 3

# Expense Categorizer Configuration
expense:
  categorizer:
    default-category: Uncategorized
    match-confidence: 0.92
    fallback-confidence: 0.35
    categories:
      Food:
        - swiggy
        - zomato
        - restaurant
      Travel:
        - uber
        - ola
        - metro
      Utilities:
        - electricity
        - broadband
        - dth
      Entertainment:
        - netflix
        - spotify
        - prime video
  analytics:
    heatmap:
      thresholds:
        - 500
        - 1500
        - 3000
        - 6000

# Budget Configuration
budget:
  coach:
    enabled: true
    overspend-threshold: 1.0
```

### CORS Configuration

The application is configured to allow requests from:
- `http://localhost:5173` (Vite default)
- `http://localhost:3000` (React default)
- `http://localhost:8080` (Spring Boot)

To modify, update `SecurityConfig.java` and `WebConfig.java`.

---

## Dependencies

### Key Dependencies (`pom.xml`)

```xml
<dependencies>
    <!-- Spring Boot Starters -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    
    <!-- Database -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
    </dependency>
    <dependency>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-core</artifactId>
    </dependency>
    
    <!-- OCR -->
    <dependency>
        <groupId>net.sourceforge.tess4j</groupId>
        <artifactId>tess4j</artifactId>
        <version>5.8.0</version>
    </dependency>
</dependencies>
```

---

## Usage Examples

### Example 1: Complete Expense Flow with Budget Checking

```java
// 1. Create a monthly budget
POST /api/budgets
{
  "name": "November Budget",
  "amount": 30000,
  "monthYear": "2025-11-01"
}

// 2. Create an expense with coach message
POST /api/expenses/with-coach
{
  "description": "Lunch at restaurant",
  "merchant": "Restaurant ABC",
  "amount": 500,
  "transactionDate": "2025-11-25"
}

// Response includes coach message if overspent
```

### Example 2: Receipt Scanning Workflow

```java
// 1. Scan receipt image
POST /api/receipts/scan
{
  "imageData": "base64-encoded-image"
}

// 2. Review parsed data, then create expense
POST /api/receipts/scan-and-create
{
  "imageData": "base64-encoded-image"
}
```

### Example 3: Split Bill Workflow

```java
// 1. Create expense group
POST /api/split-bills/groups
{
  "name": "Dinner Party",
  "createdBy": "user123"
}

// 2. Split an expense
POST /api/split-bills/split
{
  "groupId": "group-uuid",
  "amount": 2000,
  "memberShares": {
    "Alice": 800,
    "Bob": 600,
    "Charlie": 600
  }
}

// 3. Calculate who owes whom
POST /api/split-bills/settlements/calculate?groupId=group-uuid

// 4. Mark settlement as paid
POST /api/split-bills/settlements/{id}/settle
```

### Example 4: Savings Goal Tracking

```java
// 1. Create savings goal
POST /api/savings-goals
{
  "name": "Vacation Fund",
  "targetAmount": 50000,
  "targetDate": "2026-06-01"
}

// 2. Add money to goal
POST /api/savings-goals/{id}/add?amount=5000

// 3. Check progress
GET /api/savings-goals/{id}
// Returns progress percentage automatically
```

---

## Testing

### Running Tests
```bash
./mvnw test
```

### Test Coverage
- Unit tests for services
- Integration tests for controllers
- Repository tests with H2 in-memory database

---

## Deployment Notes

### Database Setup
1. Create PostgreSQL database: `personal_finance`
2. Update `application.yml` with correct database credentials
3. Flyway will automatically run migrations on startup

### OCR Setup (Optional)
For receipt scanning to work:
1. Install Tesseract OCR on the server
2. Download language data files
3. Configure path in `ReceiptScannerService.java`

### Environment Variables
Consider using environment variables for:
- Database credentials
- OCR data path
- CORS allowed origins

---

## Future Enhancements

Potential improvements:
1. User authentication and authorization
2. Multi-user support with proper isolation
3. Export functionality (CSV, PDF reports)
4. Email/SMS notifications for reminders
5. Mobile app API support
6. Advanced analytics and charts
7. Budget templates
8. Recurring expense automation

---

## Support

For issues or questions:
1. Check the API documentation above
2. Review the code comments
3. Check application logs for errors
4. Verify database migrations completed successfully

---

**Last Updated**: November 2025
**Version**: 0.0.1-SNAPSHOT









