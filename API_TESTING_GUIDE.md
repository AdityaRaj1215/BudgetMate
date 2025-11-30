# API Testing Guide - Postman/API Client

## ✅ Application Status
Your application is **running successfully** on `http://localhost:8080`

## 🔧 Fix for 403 Forbidden Error

### Issue
You're getting `403 Forbidden` because:
1. **Wrong URL**: You're using `/api/users/expenses` but the correct endpoint is `/api/expenses`
2. Spring Security was blocking some requests

### Solution
Use the correct endpoint: **`http://localhost:8080/api/expenses`**

---

## 📝 Correct API Endpoints

### Create Expense
```http
POST http://localhost:8080/api/expenses
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

### Create Expense with Budget Coach
```http
POST http://localhost:8080/api/expenses/with-coach
Content-Type: application/json

{
  "description": "Lunch",
  "merchant": "Swiggy",
  "amount": 250.00,
  "transactionDate": "2025-11-25"
}
```

### List All Expenses
```http
GET http://localhost:8080/api/expenses
```

### Get Heatmap
```http
GET http://localhost:8080/api/expenses/heatmap?start=2025-11-01&end=2025-11-30
```

---

## 🧪 Quick Test in Postman

1. **Method**: `POST`
2. **URL**: `http://localhost:8080/api/expenses`
3. **Headers**: 
   - `Content-Type: application/json`
4. **Body** (raw JSON):
```json
{
  "description": "Dinner at restaurant",
  "merchant": "Restaurant ABC",
  "amount": 500.00,
  "transactionDate": "2025-11-25",
  "category": "Food",
  "paymentMethod": "UPI"
}
```

5. **Expected Response**: `201 Created` with expense details

---

## 🔍 All Available Endpoints

### Expenses
- `POST /api/expenses` - Create expense
- `POST /api/expenses/with-coach` - Create with budget coach message
- `GET /api/expenses` - List all expenses
- `GET /api/expenses/heatmap?start=DATE&end=DATE` - Get heatmap
- `GET /api/expenses/analytics/categories?start=DATE&end=DATE` - Category spending
- `GET /api/expenses/analytics/patterns?start=DATE&end=DATE` - Spending patterns
- `GET /api/expenses/analytics/weekly?start=DATE&end=DATE` - Weekly patterns
- `GET /api/expenses/analytics/recurring?start=DATE&end=DATE&category=CATEGORY` - Recurring expenses
- `GET /api/expenses/analytics/comparison?month1Start=DATE&month1End=DATE&month2Start=DATE&month2End=DATE` - Monthly comparison

### Budgets
- `POST /api/budgets` - Create budget
- `GET /api/budgets` - List budgets
- `GET /api/budgets/current` - Get current budget
- `GET /api/budgets/daily-limit?date=DATE` - Get daily limit
- `GET /api/budgets/coach?date=DATE` - Get coach message

### Savings Goals
- `POST /api/savings-goals` - Create savings goal
- `GET /api/savings-goals` - List goals
- `POST /api/savings-goals/{id}/add?amount=AMOUNT` - Add to goal

### Investments
- `POST /api/investments` - Create investment
- `GET /api/investments` - List investments
- `GET /api/investments?type=FD` - Filter by type

### Receipts
- `POST /api/receipts/scan` - Scan receipt (OCR)
- `POST /api/receipts/scan-and-create` - Scan and create expense

### Split Bills
- `POST /api/split-bills/groups` - Create expense group
- `POST /api/split-bills/split` - Split expense
- `GET /api/split-bills/groups/{id}` - Get group with balances

### Bills/Reminders
- `POST /api/reminders/bills` - Create bill
- `GET /api/reminders/bills` - List bills
- `POST /api/reminders/bills/{id}/mark-paid` - Mark bill as paid

### Preferences
- `GET /api/preferences/{userId}` - Get preferences
- `PUT /api/preferences/{userId}` - Update preferences

---

## ⚠️ Common Issues

### 403 Forbidden
- **Fix**: Use correct endpoint `/api/expenses` (not `/api/users/expenses`)
- **Fix**: Security config updated to allow all requests

### 400 Bad Request
- Check JSON format is valid
- Ensure required fields are present
- Check date format: `YYYY-MM-DD`

### 404 Not Found
- Verify endpoint URL is correct
- Check server is running on port 8080

---

## 🎯 Test Sequence

1. **Create an expense**:
   ```
   POST http://localhost:8080/api/expenses
   ```

2. **List expenses**:
   ```
   GET http://localhost:8080/api/expenses
   ```

3. **Create a budget**:
   ```
   POST http://localhost:8080/api/budgets
   {
     "name": "November Budget",
     "amount": 30000.00,
     "monthYear": "2025-11-01"
   }
   ```

4. **Create expense with coach**:
   ```
   POST http://localhost:8080/api/expenses/with-coach
   ```

---

**Your server is ready!** 🚀





