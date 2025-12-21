# Personal Finance Tracker - API Documentation

## Base URL
```
http://localhost:8080
```

## Authentication

All API endpoints (except `/api/auth/**`) require JWT authentication. Include the JWT token in the `Authorization` header:

```
Authorization: Bearer <your-jwt-token>
```

### Authentication Endpoints

#### Validate Password
- **Method**: `POST`
- **URL**: `/api/auth/register/validate-password`
- **Auth**: Not required
- **Description**: Validate password strength in real-time. Can be called as user types password in the frontend.
- **Request Body**:
```json
{
  "password": "string (required)"
}
```
- **Response** (200 OK - Valid Password):
```json
{
  "valid": true,
  "message": "Password is valid"
}
```
- **Response** (200 OK - Invalid Password):
```json
{
  "isValid": false,
  "message": "Password must contain at least one lowercase letter, one digit, and one special character"
}
```
- **Password Requirements**:
  - Minimum 8 characters
  - At least one lowercase letter (a-z)
  - At least one digit (0-9)
  - At least one special character (e.g., @$!%*?&#^_+-=()[]{}|\\:;"'<>,./~`)
  - Maximum 128 characters
  - Cannot be a common password

#### Request OTP for Registration
- **Method**: `POST`
- **URL**: `/api/auth/register/otp`
- **Auth**: Not required
- **Description**: Request an OTP code for email verification.
- **Request Body**:
```json
{
  "email": "string (required, valid email)"
}
```
- **Response** (200 OK):
```json
{
  "message": "OTP sent successfully to your email",
  "expiresInSeconds": 600
}
```
- **Response (Dev Mode - Email Disabled)**:
```json
{
  "message": "OTP generated successfully (Email disabled - Dev mode)",
  "expiresInSeconds": 600,
  "devOtp": "123456"
}
```
- **Error Responses**:
  - `400 Bad Request`: Email already exists
  - `429 Too Many Requests`: Too many OTP requests (rate limited)

#### Register User
- **Method**: `POST`
- **URL**: `/api/auth/register`
- **Auth**: Not required
- **Description**: Complete user registration with OTP verification. Must request OTP first. Password validation happens at this step.
- **Request Body**:
```json
{
  "username": "string (required, 3-50 characters)",
  "email": "string (required, valid email)",
  "password": "string (required, min 8 characters, must contain lowercase letter, digit, and special character)",
  "otp": "string (required, 6-digit code from email)"
}
```
- **Password Requirements**:
  - Minimum 8 characters
  - At least one lowercase letter (a-z)
  - At least one digit (0-9)
  - At least one special character (e.g., @$!%*?&#^_+-=()[]{}|\\:;"'<>,./~`)
  - Maximum 128 characters
  - Cannot be a common password
- **Response** (201 Created):
```json
{
  "token": "string (JWT token)",
  "username": "string",
  "email": "string",
  "roles": ["string"]
}
```
- **Error Responses**:
  - `400 Bad Request`: Invalid OTP, expired OTP, password validation failed
  - `409 Conflict`: Username or email already exists

#### Login
- **Method**: `POST`
- **URL**: `/api/auth/login`
- **Auth**: Not required
- **Request Body**:
```json
{
  "username": "string (required)",
  "password": "string (required)"
}
```
- **Response** (200 OK):
```json
{
  "token": "string (JWT token)",
  "username": "string",
  "email": "string",
  "roles": ["string"]
}
```

---

## Expenses

### Create Expense
- **Method**: `POST`
- **URL**: `/api/expenses`
- **Auth**: Required
- **Request Body**:
```json
{
  "description": "string (required)",
  "merchant": "string (optional)",
  "amount": "number (required, > 0)",
  "transactionDate": "YYYY-MM-DD (required)",
  "category": "string (optional)",
  "paymentMethod": "string (optional)"
}
```
- **Response** (201 Created):
```json
{
  "id": "UUID",
  "description": "string",
  "merchant": "string",
  "category": "string",
  "amount": "number",
  "transactionDate": "YYYY-MM-DD",
  "paymentMethod": "string",
  "createdAt": "ISO 8601 datetime",
  "updatedAt": "ISO 8601 datetime"
}
```

### Create Expense with Coach
- **Method**: `POST`
- **URL**: `/api/expenses/with-coach`
- **Auth**: Required
- **Request Body**: Same as Create Expense
- **Response** (201 Created):
```json
{
  "expense": {
    "id": "UUID",
    "description": "string",
    "merchant": "string",
    "category": "string",
    "amount": "number",
    "transactionDate": "YYYY-MM-DD",
    "paymentMethod": "string",
    "createdAt": "ISO 8601 datetime",
    "updatedAt": "ISO 8601 datetime"
  },
  "coachMessage": "string (optional budget warning)"
}
```

### List All Expenses (with Filtering)
- **Method**: `GET`
- **URL**: `/api/expenses`
- **Auth**: Required
- **Query Parameters** (all optional):
  - `startDate`: `YYYY-MM-DD` - Filter expenses from this date onwards
  - `endDate`: `YYYY-MM-DD` - Filter expenses up to this date
  - `category`: `string` - Filter by category (case-insensitive)
  - `minAmount`: `number` - Minimum amount filter
  - `maxAmount`: `number` - Maximum amount filter
  - `search`: `string` - Search in description and merchant fields (case-insensitive, partial match)
  - `paymentMethod`: `string` - Filter by payment method (case-insensitive)
- **Response** (200 OK): Array of ExpenseResponse objects, sorted by transaction date (newest first)
- **Note**: If no filter parameters are provided, all expenses for the authenticated user are returned.
- **Example**:
  - `/api/expenses` - Returns all expenses
  - `/api/expenses?category=Food&minAmount=10` - Returns Food expenses with amount >= 10
  - `/api/expenses?search=coffee&startDate=2024-01-01` - Returns expenses containing "coffee" from 2024-01-01 onwards

### Get Expense Heatmap
- **Method**: `GET`
- **URL**: `/api/expenses/heatmap`
- **Auth**: Required
- **Query Parameters**:
  - `start`: `YYYY-MM-DD` (required)
  - `end`: `YYYY-MM-DD` (required)
- **Response** (200 OK):
```json
[
  {
    "date": "YYYY-MM-DD",
    "totalAmount": "number"
  }
]
```

### Get Category Spending Summary
- **Method**: `GET`
- **URL**: `/api/expenses/analytics/categories`
- **Auth**: Required
- **Query Parameters**:
  - `start`: `YYYY-MM-DD` (required)
  - `end`: `YYYY-MM-DD` (required)
- **Response** (200 OK):
```json
[
  {
    "category": "string",
    "totalAmount": "number",
    "transactionCount": "number"
  }
]
```

### Get Spending Patterns
- **Method**: `GET`
- **URL**: `/api/expenses/analytics/patterns`
- **Auth**: Required
- **Query Parameters**:
  - `start`: `YYYY-MM-DD` (required)
  - `end`: `YYYY-MM-DD` (required)
- **Response** (200 OK): Array of SpendingPattern objects

### Get Weekly Patterns
- **Method**: `GET`
- **URL**: `/api/expenses/analytics/weekly`
- **Auth**: Required
- **Query Parameters**:
  - `start`: `YYYY-MM-DD` (required)
  - `end`: `YYYY-MM-DD` (required)
- **Response** (200 OK): Array of SpendingPattern objects

### Get Recurring Expenses
- **Method**: `GET`
- **URL**: `/api/expenses/analytics/recurring`
- **Auth**: Required
- **Query Parameters**:
  - `start`: `YYYY-MM-DD` (required)
  - `end`: `YYYY-MM-DD` (required)
  - `category`: `string` (optional)
- **Response** (200 OK): Array of SpendingPattern objects

### Get Monthly Comparison
- **Method**: `GET`
- **URL**: `/api/expenses/analytics/comparison`
- **Auth**: Required
- **Query Parameters**:
  - `month1Start`: `YYYY-MM-DD` (required)
  - `month1End`: `YYYY-MM-DD` (required)
  - `month2Start`: `YYYY-MM-DD` (required)
  - `month2End`: `YYYY-MM-DD` (required)
- **Response** (200 OK):
```json
{
  "month1": {
    "total": "number",
    "byCategory": {}
  },
  "month2": {
    "total": "number",
    "byCategory": {}
  },
  "difference": "number",
  "percentageChange": "number"
}
```

### Export Expenses to PDF
- **Method**: `GET`
- **URL**: `/api/expenses/export/pdf`
- **Auth**: Required
- **Query Parameters**:
  - `start`: `YYYY-MM-DD` (optional)
  - `end`: `YYYY-MM-DD` (optional)
- **Response** (200 OK): PDF file download
- **Headers**:
  - `Content-Type: application/pdf`
  - `Content-Disposition: attachment; filename="transactions.pdf"`

### Export Expenses to CSV
- **Method**: `GET`
- **URL**: `/api/expenses/export/csv`
- **Auth**: Required
- **Query Parameters**:
  - `start`: `YYYY-MM-DD` (optional)
  - `end`: `YYYY-MM-DD` (optional)
- **Response** (200 OK): CSV file download
- **Headers**:
  - `Content-Type: text/csv; charset=UTF-8`
  - `Content-Disposition: attachment; filename="transactions.csv"`
- **CSV Format**:
  - Header row: `Date,Description,Merchant,Category,Amount,Payment Method`
  - UTF-8 BOM included for Excel compatibility
  - Proper escaping for fields containing commas, quotes, or newlines
  - Total row at the end

### Categorize Expense
- **Method**: `POST`
- **URL**: `/api/expenses/categorize`
- **Auth**: Required
- **Request Body**:
```json
{
  "description": "string (required)",
  "merchant": "string (optional)",
  "amount": "number (required, >= 0)"
}
```
- **Response** (200 OK):
```json
{
  "category": "string (suggested category)"
}
```

---

## Budgets

### Create Budget
- **Method**: `POST`
- **URL**: `/api/budgets`
- **Auth**: Required
- **Request Body**:
```json
{
  "amount": "number (required, > 0)",
  "startDate": "YYYY-MM-DD (required)",
  "endDate": "YYYY-MM-DD (required)",
  "category": "string (optional)"
}
```
- **Response** (201 Created):
```json
{
  "id": "UUID",
  "amount": "number",
  "startDate": "YYYY-MM-DD",
  "endDate": "YYYY-MM-DD",
  "category": "string",
  "isActive": "boolean",
  "createdAt": "ISO 8601 datetime",
  "updatedAt": "ISO 8601 datetime"
}
```

### Update Budget
- **Method**: `PUT`
- **URL**: `/api/budgets/{id}`
- **Auth**: Required
- **Path Parameters**:
  - `id`: UUID (required)
- **Request Body**: Same as Create Budget
- **Response** (200 OK): BudgetResponse

### Delete Budget
- **Method**: `DELETE`
- **URL**: `/api/budgets/{id}`
- **Auth**: Required
- **Path Parameters**:
  - `id`: UUID (required)
- **Response** (204 No Content)

### Deactivate Budget
- **Method**: `POST`
- **URL**: `/api/budgets/{id}/deactivate`
- **Auth**: Required
- **Path Parameters**:
  - `id`: UUID (required)
- **Response** (200 OK): BudgetResponse

### List All Budgets
- **Method**: `GET`
- **URL**: `/api/budgets`
- **Auth**: Required
- **Response** (200 OK): Array of BudgetResponse objects

### Get Budget by ID
- **Method**: `GET`
- **URL**: `/api/budgets/{id}`
- **Auth**: Required
- **Path Parameters**:
  - `id`: UUID (required)
- **Response** (200 OK): BudgetResponse

### Get Current Budget
- **Method**: `GET`
- **URL**: `/api/budgets/current`
- **Auth**: Required
- **Response** (200 OK): BudgetResponse
- **Response** (404 Not Found): If no active budget exists

### Get Daily Spend Limit
- **Method**: `GET`
- **URL**: `/api/budgets/daily-limit`
- **Auth**: Required
- **Query Parameters**:
  - `date`: `YYYY-MM-DD` (optional, defaults to today)
- **Response** (200 OK):
```json
{
  "budgetId": "UUID (null if no budget)",
  "dailyLimitId": "UUID (null if no budget)",
  "date": "YYYY-MM-DD",
  "dailyLimit": "number (0 if no budget)",
  "spent": "number",
  "remaining": "number (0 if no budget)"
}
```

### Get Budget Coach Message
- **Method**: `GET`
- **URL**: `/api/budgets/coach`
- **Auth**: Required
- **Query Parameters**:
  - `date`: `YYYY-MM-DD` (optional, defaults to today)
- **Response** (200 OK):
```json
{
  "message": "string",
  "severity": "string (INFO, WARNING, CRITICAL)",
  "date": "YYYY-MM-DD"
}
```
- **Response** (404 Not Found): If no budget exists

---

## Savings Goals

### Create Savings Goal
- **Method**: `POST`
- **URL**: `/api/savings-goals`
- **Auth**: Required
- **Request Body**:
```json
{
  "name": "string (required)",
  "targetAmount": "number (required, > 0)",
  "currentAmount": "number (optional, defaults to 0)",
  "targetDate": "YYYY-MM-DD (optional)",
  "description": "string (optional)"
}
```
- **Response** (201 Created):
```json
{
  "id": "UUID",
  "name": "string",
  "targetAmount": "number",
  "currentAmount": "number",
  "targetDate": "YYYY-MM-DD",
  "description": "string",
  "isActive": "boolean",
  "progressPercentage": "number",
  "createdAt": "ISO 8601 datetime",
  "updatedAt": "ISO 8601 datetime"
}
```

### Update Savings Goal
- **Method**: `PUT`
- **URL**: `/api/savings-goals/{id}`
- **Auth**: Required
- **Path Parameters**:
  - `id`: UUID (required)
- **Request Body**: Same as Create Savings Goal
- **Response** (200 OK): SavingsGoalResponse

### Delete Savings Goal
- **Method**: `DELETE`
- **URL**: `/api/savings-goals/{id}`
- **Auth**: Required
- **Path Parameters**:
  - `id`: UUID (required)
- **Response** (204 No Content)

### Deactivate Savings Goal
- **Method**: `POST`
- **URL**: `/api/savings-goals/{id}/deactivate`
- **Auth**: Required
- **Path Parameters**:
  - `id`: UUID (required)
- **Response** (200 OK): SavingsGoalResponse

### Add Amount to Savings Goal
- **Method**: `POST`
- **URL**: `/api/savings-goals/{id}/add`
- **Auth**: Required
- **Path Parameters**:
  - `id`: UUID (required)
- **Query Parameters**:
  - `amount`: `number` (required, > 0)
- **Response** (200 OK): SavingsGoalResponse

### Withdraw Amount from Savings Goal
- **Method**: `POST`
- **URL**: `/api/savings-goals/{id}/withdraw`
- **Auth**: Required
- **Path Parameters**:
  - `id`: UUID (required)
- **Query Parameters**:
  - `amount`: `number` (required, > 0)
- **Response** (200 OK): SavingsGoalResponse

### Set Savings Goal Amount
- **Method**: `PUT`
- **URL**: `/api/savings-goals/{id}/amount`
- **Auth**: Required
- **Path Parameters**:
  - `id`: UUID (required)
- **Query Parameters**:
  - `amount`: `number` (required, >= 0)
- **Response** (200 OK): SavingsGoalResponse

### List All Savings Goals
- **Method**: `GET`
- **URL**: `/api/savings-goals`
- **Auth**: Required
- **Response** (200 OK): Array of SavingsGoalResponse objects

### Get Savings Goal by ID
- **Method**: `GET`
- **URL**: `/api/savings-goals/{id}`
- **Auth**: Required
- **Path Parameters**:
  - `id`: UUID (required)
- **Response** (200 OK): SavingsGoalResponse

---

## Investments

### Create Investment
- **Method**: `POST`
- **URL**: `/api/investments`
- **Auth**: Required
- **Request Body**:
```json
{
  "name": "string (required)",
  "type": "string (required, enum: STOCK, MUTUAL_FUND, FIXED_DEPOSIT, BONDS, CRYPTO, OTHER)",
  "amount": "number (required, > 0)",
  "currentValue": "number (optional, defaults to amount)",
  "purchaseDate": "YYYY-MM-DD (required)",
  "maturityDate": "YYYY-MM-DD (optional)",
  "description": "string (optional)"
}
```
- **Response** (201 Created):
```json
{
  "id": "UUID",
  "name": "string",
  "type": "string",
  "amount": "number",
  "currentValue": "number",
  "purchaseDate": "YYYY-MM-DD",
  "maturityDate": "YYYY-MM-DD",
  "description": "string",
  "isActive": "boolean",
  "gainLoss": "number",
  "gainLossPercentage": "number",
  "createdAt": "ISO 8601 datetime",
  "updatedAt": "ISO 8601 datetime"
}
```

### Update Investment
- **Method**: `PUT`
- **URL**: `/api/investments/{id}`
- **Auth**: Required
- **Path Parameters**:
  - `id`: UUID (required)
- **Request Body**: Same as Create Investment
- **Response** (200 OK): InvestmentResponse

### Delete Investment
- **Method**: `DELETE`
- **URL**: `/api/investments/{id}`
- **Auth**: Required
- **Path Parameters**:
  - `id`: UUID (required)
- **Response** (204 No Content)

### Deactivate Investment
- **Method**: `POST`
- **URL**: `/api/investments/{id}/deactivate`
- **Auth**: Required
- **Path Parameters**:
  - `id`: UUID (required)
- **Response** (200 OK): InvestmentResponse

### Update Investment Current Value
- **Method**: `PUT`
- **URL**: `/api/investments/{id}/current-value`
- **Auth**: Required
- **Path Parameters**:
  - `id`: UUID (required)
- **Query Parameters**:
  - `currentValue`: `number` (required, >= 0)
- **Response** (200 OK): InvestmentResponse

### List All Investments
- **Method**: `GET`
- **URL**: `/api/investments`
- **Auth**: Required
- **Query Parameters**:
  - `type`: `string` (optional, enum: STOCK, MUTUAL_FUND, FIXED_DEPOSIT, BONDS, CRYPTO, OTHER)
- **Response** (200 OK): Array of InvestmentResponse objects

### Get Investment by ID
- **Method**: `GET`
- **URL**: `/api/investments/{id}`
- **Auth**: Required
- **Path Parameters**:
  - `id`: UUID (required)
- **Response** (200 OK): InvestmentResponse

### Get Upcoming Maturities
- **Method**: `GET`
- **URL**: `/api/investments/maturities`
- **Auth**: Required
- **Query Parameters**:
  - `start`: `YYYY-MM-DD` (required)
  - `end`: `YYYY-MM-DD` (required)
- **Response** (200 OK): Array of InvestmentResponse objects

---

## Split Bills

### Create Expense Group
- **Method**: `POST`
- **URL**: `/api/split-bills/groups`
- **Auth**: Required
- **Request Body**:
```json
{
  "name": "string (required)",
  "description": "string (optional)",
  "memberIds": ["UUID"] (required, array of user IDs)
}
```
- **Response** (201 Created):
```json
{
  "id": "UUID",
  "name": "string",
  "description": "string",
  "memberIds": ["UUID"],
  "createdAt": "ISO 8601 datetime",
  "updatedAt": "ISO 8601 datetime"
}
```

### Update Expense Group
- **Method**: `PUT`
- **URL**: `/api/split-bills/groups/{id}`
- **Auth**: Required
- **Path Parameters**:
  - `id`: UUID (required)
- **Request Body**: Same as Create Expense Group
- **Response** (200 OK): ExpenseGroupResponse

### Delete Expense Group
- **Method**: `DELETE`
- **URL**: `/api/split-bills/groups/{id}`
- **Auth**: Required
- **Path Parameters**:
  - `id`: UUID (required)
- **Response** (204 No Content)

### List All Expense Groups
- **Method**: `GET`
- **URL**: `/api/split-bills/groups`
- **Auth**: Required
- **Response** (200 OK): Array of ExpenseGroupResponse objects

### Get Expense Group by ID
- **Method**: `GET`
- **URL**: `/api/split-bills/groups/{id}`
- **Auth**: Required
- **Path Parameters**:
  - `id`: UUID (required)
- **Response** (200 OK): ExpenseGroupResponse

### Split Expense
- **Method**: `POST`
- **URL**: `/api/split-bills/split`
- **Auth**: Required
- **Request Body**:
```json
{
  "groupId": "UUID (required)",
  "description": "string (required)",
  "amount": "number (required, > 0)",
  "paidBy": "UUID (required, user ID who paid)",
  "shares": [
    {
      "userId": "UUID (required)",
      "amount": "number (required, > 0)"
    }
  ]
}
```
- **Response** (201 Created): Array of ExpenseShare objects

### Create Settlement
- **Method**: `POST`
- **URL**: `/api/split-bills/settlements`
- **Auth**: Required
- **Request Body**:
```json
{
  "groupId": "UUID (required)",
  "fromUserId": "UUID (required)",
  "toUserId": "UUID (required)",
  "amount": "number (required, > 0)"
}
```
- **Response** (201 Created):
```json
{
  "id": "UUID",
  "groupId": "UUID",
  "fromUserId": "UUID",
  "toUserId": "UUID",
  "amount": "number",
  "isSettled": "boolean",
  "settledAt": "ISO 8601 datetime (null if not settled)",
  "createdAt": "ISO 8601 datetime"
}
```

### Mark Settlement as Settled
- **Method**: `POST`
- **URL**: `/api/split-bills/settlements/{id}/settle`
- **Auth**: Required
- **Path Parameters**:
  - `id`: UUID (required)
- **Response** (200 OK): SettlementResponse

### Get Settlements for Group
- **Method**: `GET`
- **URL**: `/api/split-bills/settlements`
- **Auth**: Required
- **Query Parameters**:
  - `groupId`: UUID (required)
- **Response** (200 OK): Array of SettlementResponse objects

### Get Pending Settlements
- **Method**: `GET`
- **URL**: `/api/split-bills/settlements/pending`
- **Auth**: Required
- **Query Parameters**:
  - `groupId`: UUID (required)
- **Response** (200 OK): Array of SettlementResponse objects

### Calculate Settlements
- **Method**: `POST`
- **URL**: `/api/split-bills/settlements/calculate`
- **Auth**: Required
- **Query Parameters**:
  - `groupId`: UUID (required)
- **Response** (200 OK): Array of SettlementResponse objects

---

## Bill Reminders

### Create Bill
- **Method**: `POST`
- **URL**: `/api/reminders/bills`
- **Auth**: Required
- **Request Body**:
```json
{
  "name": "string (required)",
  "category": "string (optional)",
  "amount": "number (required, > 0)",
  "nextDueDate": "YYYY-MM-DD (required, >= today)",
  "frequency": "string (required, enum: MONTHLY, QUARTERLY, YEARLY, WEEKLY)",
  "remindDaysBefore": "number (optional)"
}
```
- **Response** (201 Created):
```json
{
  "id": "UUID",
  "name": "string",
  "category": "string",
  "amount": "number",
  "nextDueDate": "YYYY-MM-DD",
  "frequency": "string",
  "remindDaysBefore": "number",
  "isPaused": "boolean",
  "createdAt": "ISO 8601 datetime",
  "updatedAt": "ISO 8601 datetime"
}
```

### List All Bills
- **Method**: `GET`
- **URL**: `/api/reminders/bills`
- **Auth**: Required
- **Response** (200 OK): Array of BillResponse objects

### Update Bill
- **Method**: `PUT`
- **URL**: `/api/reminders/bills/{id}`
- **Auth**: Required
- **Path Parameters**:
  - `id`: UUID (required)
- **Request Body**: Same as Create Bill
- **Response** (200 OK): BillResponse

### Mark Bill as Paid
- **Method**: `POST`
- **URL**: `/api/reminders/bills/{id}/mark-paid`
- **Auth**: Required
- **Path Parameters**:
  - `id`: UUID (required)
- **Request Body**:
```json
{
  "paidDate": "YYYY-MM-DD (required)"
}
```
- **Response** (200 OK): BillResponse

### Pause Bill
- **Method**: `POST`
- **URL**: `/api/reminders/bills/{id}/pause`
- **Auth**: Required
- **Path Parameters**:
  - `id`: UUID (required)
- **Response** (200 OK): BillResponse

### Resume Bill
- **Method**: `POST`
- **URL**: `/api/reminders/bills/{id}/resume`
- **Auth**: Required
- **Path Parameters**:
  - `id`: UUID (required)
- **Response** (200 OK): BillResponse

### Delete Bill
- **Method**: `DELETE`
- **URL**: `/api/reminders/bills/{id}`
- **Auth**: Required
- **Path Parameters**:
  - `id`: UUID (required)
- **Response** (204 No Content)

---

## Receipt Scanning

### Scan Receipt
- **Method**: `POST`
- **URL**: `/api/receipts/scan`
- **Auth**: Required
- **Request Body**:
```json
{
  "imageData": "string (required, base64 encoded image)"
}
```
- **Response** (200 OK):
```json
{
  "amount": "number",
  "merchant": "string",
  "date": "YYYY-MM-DD",
  "category": "string",
  "ocrText": "string",
  "expenseRequest": {
    "description": "string",
    "merchant": "string",
    "amount": "number",
    "transactionDate": "YYYY-MM-DD",
    "category": "string",
    "paymentMethod": "string"
  }
}
```
- **Response** (500 Internal Server Error): If OCR processing fails

### Scan and Create Expense
- **Method**: `POST`
- **URL**: `/api/receipts/scan-and-create`
- **Auth**: Required
- **Request Body**: Same as Scan Receipt
- **Response** (201 Created): ExpenseResponse
- **Response** (500 Internal Server Error): If OCR processing fails

---

## User Preferences

### Get User Preferences
- **Method**: `GET`
- **URL**: `/api/preferences`
- **Auth**: Required
- **Response** (200 OK):
```json
{
  "userId": "UUID",
  "darkMode": "boolean",
  "currency": "string",
  "dateFormat": "string",
  "language": "string"
}
```

### Update User Preferences
- **Method**: `PUT`
- **URL**: `/api/preferences`
- **Auth**: Required
- **Request Body**:
```json
{
  "darkMode": "boolean (optional)",
  "currency": "string (optional)",
  "dateFormat": "string (optional)",
  "language": "string (optional)"
}
```
- **Response** (200 OK): UserPreferencesResponse

---

## Error Responses

All endpoints may return the following error responses:

### 400 Bad Request
```json
{
  "timestamp": "ISO 8601 datetime",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation error message",
  "path": "/api/endpoint"
}
```

### 401 Unauthorized
```json
{
  "timestamp": "ISO 8601 datetime",
  "status": 401,
  "error": "Unauthorized",
  "message": "JWT token is missing or invalid",
  "path": "/api/endpoint"
}
```

### 403 Forbidden
```json
{
  "timestamp": "ISO 8601 datetime",
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied",
  "path": "/api/endpoint"
}
```

### 404 Not Found
```json
{
  "timestamp": "ISO 8601 datetime",
  "status": 404,
  "error": "Not Found",
  "message": "Resource not found",
  "path": "/api/endpoint"
}
```

### 500 Internal Server Error
```json
{
  "timestamp": "ISO 8601 datetime",
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred",
  "path": "/api/endpoint"
}
```

---

## CORS Configuration

The API allows requests from the following origins:
- `http://localhost:5173`
- `http://localhost:3000`
- `http://localhost:3001`
- `http://localhost:8080`
- `http://127.0.0.1:5173`
- `http://127.0.0.1:3000`
- `http://127.0.0.1:3001`

Allowed methods: `GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`, `PATCH`

---

## Rate Limiting

The API implements rate limiting to prevent abuse. Rate limits are configured per endpoint and may vary. If rate limit is exceeded, the API will return:

- **Status**: `429 Too Many Requests`
- **Response**: Rate limit exceeded message

---

## Notes

1. **User Isolation**: All endpoints automatically filter data by the authenticated user. Users can only access their own data.

2. **Date Formats**: All dates should be in `YYYY-MM-DD` format (ISO 8601 date format).

3. **UUID Format**: All IDs are UUIDs in the format: `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx`

4. **Decimal Precision**: All monetary amounts are represented as numbers with decimal precision (e.g., `100.50`).

5. **CSRF Protection**: CSRF protection is disabled for all `/api/**` endpoints as the application uses stateless JWT authentication.

6. **PDF Export**: The PDF export endpoint returns binary data. Frontend should handle it as a file download.

---

## Example Frontend Integration

### TypeScript/JavaScript Example

```typescript
const API_BASE_URL = 'http://localhost:8080';

// Helper function to get auth token
function getAuthToken(): string | null {
  return localStorage.getItem('accessToken');
}

// Helper function to make authenticated requests
async function apiRequest<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<T> {
  const token = getAuthToken();
  if (!token) {
    throw new Error('Not authenticated');
  }

  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
      ...options.headers,
    },
  });

  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: 'Unknown error' }));
    throw new Error(error.message || `HTTP ${response.status}`);
  }

  return response.json();
}

// Example: Create expense
async function createExpense(expense: {
  description: string;
  merchant?: string;
  amount: number;
  transactionDate: string;
  category?: string;
  paymentMethod?: string;
}) {
  return apiRequest<ExpenseResponse>('/api/expenses', {
    method: 'POST',
    body: JSON.stringify(expense),
  });
}

// Example: Export PDF
async function exportExpensesPdf(start?: string, end?: string) {
  const token = getAuthToken();
  if (!token) {
    throw new Error('Not authenticated');
  }

  const params = new URLSearchParams();
  if (start) params.append('start', start);
  if (end) params.append('end', end);

  const url = `${API_BASE_URL}/api/expenses/export/pdf${params.toString() ? `?${params.toString()}` : ''}`;
  
  const response = await fetch(url, {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    throw new Error(`Failed to export PDF: ${response.status}`);
  }

  const blob = await response.blob();
  const fileUrl = window.URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = fileUrl;
  a.download = 'transactions.pdf';
  document.body.appendChild(a);
  a.click();
  a.remove();
  window.URL.revokeObjectURL(fileUrl);
}
```

---

## Summary

This API provides comprehensive personal finance management capabilities including:
- **Authentication**: JWT-based user registration and login
- **Expenses**: Create, list, analyze, and export expenses
- **Budgets**: Create and manage budgets with daily limits and coaching
- **Savings Goals**: Track savings progress with goals
- **Investments**: Manage investment portfolio
- **Split Bills**: Split expenses among multiple users
- **Bill Reminders**: Track recurring bills and payments
- **Receipt Scanning**: OCR-based receipt scanning and expense creation
- **User Preferences**: Manage user settings and preferences
- **Offline Sync**: Push/pull synchronization for offline-first support

All endpoints are secured with JWT authentication and automatically filter data by the authenticated user.

---

## Offline Sync Endpoints

### Push Local Changes
- **Method**: `POST`
- **URL**: `/api/sync/push`
- **Auth**: Required
- **Description**: Push local changes (create, update, delete) to the server. Handles conflict detection and resolution.
- **Request Body**:
```json
{
  "lastSyncAt": "2025-01-15T10:30:00Z",
  "deviceId": "mobile-device-123",
  "expenses": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "operation": "create",
      "clientUpdatedAt": "2025-01-15T11:00:00Z",
      "data": {
        "description": "Lunch",
        "merchant": "Restaurant",
        "amount": 25.50,
        "transactionDate": "2025-01-15",
        "paymentMethod": "Credit Card",
        "category": "Food"
      }
    },
    {
      "id": "660e8400-e29b-41d4-a716-446655440001",
      "operation": "update",
      "clientUpdatedAt": "2025-01-15T11:05:00Z",
      "data": {
        "description": "Updated description",
        "amount": 30.00
      }
    },
    {
      "id": "770e8400-e29b-41d4-a716-446655440002",
      "operation": "delete",
      "clientUpdatedAt": "2025-01-15T11:10:00Z",
      "data": null
    }
  ],
  "budgets": [],
  "bills": []
}
```
- **Response** (200 OK):
```json
{
  "serverSyncAt": "2025-01-15T12:00:00Z",
  "processedCount": 2,
  "conflictCount": 1,
  "conflicts": [
    {
      "entityType": "expense",
      "entityId": "660e8400-e29b-41d4-a716-446655440001",
      "reason": "server_updated_after_client",
      "serverUpdatedAt": "2025-01-15T11:30:00Z",
      "clientUpdatedAt": "2025-01-15T11:05:00Z"
    }
  ],
  "results": [
    {
      "entityType": "expense",
      "entityId": "550e8400-e29b-41d4-a716-446655440000",
      "operation": "create",
      "success": true,
      "message": "Created successfully",
      "serverId": "550e8400-e29b-41d4-a716-446655440000"
    },
    {
      "entityType": "expense",
      "entityId": "660e8400-e29b-41d4-a716-446655440001",
      "operation": "update",
      "success": false,
      "message": "Conflict: Server has newer version",
      "serverId": null
    },
    {
      "entityType": "expense",
      "entityId": "770e8400-e29b-41d4-a716-446655440002",
      "operation": "delete",
      "success": true,
      "message": "Deleted successfully",
      "serverId": null
    }
  ]
}
```

**Operation Types:**
- `create`: Create a new entity
- `update`: Update an existing entity
- `delete`: Delete an entity

**Conflict Reasons:**
- `server_updated_after_client`: Server has a newer version than the client's last sync
- `entity_deleted_on_server`: Entity was deleted on server but client is trying to update/delete it

---

### Pull Server Changes
- **Method**: `GET`
- **URL**: `/api/sync/pull`
- **Auth**: Required
- **Description**: Pull all changes from the server since the last sync timestamp.
- **Query Parameters**:
  - `lastSyncAt` (optional): ISO 8601 timestamp of last sync. If not provided, uses stored metadata.
  - `deviceId` (optional): Device identifier for multi-device sync tracking.
- **Example**: `/api/sync/pull?lastSyncAt=2025-01-15T10:30:00Z&deviceId=mobile-device-123`
- **Response** (200 OK):
```json
{
  "serverSyncAt": "2025-01-15T12:00:00Z",
  "lastSyncAt": "2025-01-15T10:30:00Z",
  "totalChanges": 5,
  "expenses": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "description": "Coffee",
      "merchant": "Starbucks",
      "category": "Food",
      "amount": 5.50,
      "transactionDate": "2025-01-15",
      "paymentMethod": "Credit Card",
      "createdAt": "2025-01-15T11:00:00Z",
      "updatedAt": "2025-01-15T11:00:00Z"
    }
  ],
  "budgets": [
    {
      "id": "660e8400-e29b-41d4-a716-446655440001",
      "name": "January 2025",
      "amount": 5000.00,
      "monthYear": "2025-01-01",
      "active": true,
      "createdAt": "2025-01-01T00:00:00Z",
      "updatedAt": "2025-01-15T11:30:00Z"
    }
  ],
  "bills": [],
  "deletedExpenses": [],
  "deletedBudgets": [],
  "deletedBills": []
}
```

---

### Get Sync Status
- **Method**: `GET`
- **URL**: `/api/sync/status`
- **Auth**: Required
- **Description**: Get the current sync status including last sync timestamp and pending changes count.
- **Response** (200 OK):
```json
{
  "lastSyncAt": "2025-01-15T10:30:00Z",
  "hasUnsyncedChanges": true,
  "pendingChangesCount": 0
}
```

---

## Sync Workflow

### Typical Offline-First Sync Flow

1. **Initial Sync (Pull)**:
   ```
   GET /api/sync/pull
   → Returns all existing data
   → Store in local IndexedDB
   → Save serverSyncAt as lastSyncAt
   ```

2. **Make Changes Offline**:
   - User creates/updates/deletes expenses, budgets, bills
   - Store changes in local IndexedDB with operation type
   - Mark as "pending sync"

3. **When Online (Push)**:
   ```
   POST /api/sync/push
   {
     "lastSyncAt": "<stored timestamp>",
     "expenses": [<pending changes>],
     "budgets": [<pending changes>],
     "bills": [<pending changes>]
   }
   → Server processes changes
   → Returns conflicts if any
   → Update local data with server responses
   ```

4. **After Push (Pull)**:
   ```
   GET /api/sync/pull?lastSyncAt=<new serverSyncAt>
   → Get any server-side changes
   → Merge with local data
   → Update lastSyncAt
   ```

### Conflict Resolution Strategy

The sync system uses **Last-Write-Wins** with conflict detection:

- **No Conflict**: Client's change is applied if server hasn't updated the entity since client's last sync
- **Conflict Detected**: 
  - Server version is newer → Conflict reported, client change rejected
  - Entity deleted on server → Conflict reported, client change rejected
  - Client can resolve conflicts by:
    1. Accepting server version (discard local change)
    2. Forcing client version (requires manual resolution endpoint - future enhancement)
    3. Merging changes (requires manual merge - future enhancement)

### Best Practices

1. **Always pull before push** to minimize conflicts
2. **Handle conflicts gracefully** - show user both versions and let them choose
3. **Use deviceId** for multi-device scenarios to track per-device sync state
4. **Store lastSyncAt** locally and send it with every push request
5. **Batch operations** - send multiple changes in a single push request
6. **Handle network failures** - retry failed syncs with exponential backoff

---

All endpoints are secured with JWT authentication and automatically filter data by the authenticated user.

