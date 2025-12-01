# Frontend Integration Guide

Complete API reference and frontend integration guide for the Personal Finance Tracker backend.

## Table of Contents

1. [Base URL & Authentication](#base-url--authentication)
2. [Authentication Flow](#authentication-flow)
3. [API Endpoints](#api-endpoints)
4. [Frontend Implementation Guide](#frontend-implementation-guide)
5. [Error Handling](#error-handling)
6. [Security Considerations](#security-considerations)

---

## Base URL & Authentication

### Base URL
```
Development: http://localhost:8080
Production: https://your-domain.com
```

### Authentication
All API endpoints (except `/api/auth/**`) require JWT authentication.

**Header Format:**
```
Authorization: Bearer <jwt_token>
```

---

## Authentication Flow

### 1. User Registration

**Endpoint:** `POST /api/auth/register`

**Request:**
```json
{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "SecurePassword123!"
}
```

**Response (201 Created):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "johndoe",
  "email": "john@example.com",
  "roles": ["USER"]
}
```

**Frontend Implementation:**
```javascript
// Store token in localStorage or secure storage
localStorage.setItem('authToken', response.token);
localStorage.setItem('user', JSON.stringify({
  username: response.username,
  email: response.email,
  roles: response.roles
}));
```

### 2. User Login

**Endpoint:** `POST /api/auth/login`

**Request:**
```json
{
  "username": "johndoe",
  "password": "SecurePassword123!"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "johndoe",
  "email": "john@example.com",
  "roles": ["USER"]
}
```

### 3. Making Authenticated Requests

**Frontend Implementation (Axios Example):**
```javascript
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json'
  }
});

// Add token to all requests
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('authToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Handle token expiration
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Token expired or invalid
      localStorage.removeItem('authToken');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);
```

---

## API Endpoints

### Authentication Endpoints

#### Register User
- **POST** `/api/auth/register`
- **Auth:** Not required
- **Body:** `RegisterRequest`
- **Response:** `LoginResponse` (201)

#### Login
- **POST** `/api/auth/login`
- **Auth:** Not required
- **Body:** `LoginRequest`
- **Response:** `LoginResponse` (200)

---

### Expense Endpoints

#### Create Expense
- **POST** `/api/expenses`
- **Auth:** Required
- **Body:**
```json
{
  "description": "Lunch at restaurant",
  "merchant": "Restaurant ABC",
  "amount": 500.00,
  "transactionDate": "2024-01-15",
  "category": "Food",
  "paymentMethod": "Credit Card"
}
```
- **Response:** `ExpenseResponse` (201)

#### Create Expense with Budget Coach
- **POST** `/api/expenses/with-coach`
- **Auth:** Required
- **Body:** Same as Create Expense
- **Response:** `ExpenseCreateResponse` (201)
```json
{
  "expense": { ... },
  "coachMessage": {
    "message": "You overspent by ₹450 today.",
    "type": "WARNING",
    "overspentAmount": 450.00,
    "dailyLimit": 1000.00,
    "spentAmount": 1450.00
  }
}
```

#### List All Expenses
- **GET** `/api/expenses`
- **Auth:** Required
- **Response:** `List<ExpenseResponse>` (200)

#### Get Expense Heatmap
- **GET** `/api/expenses/heatmap?start=2024-01-01&end=2024-01-31`
- **Auth:** Required
- **Query Params:**
  - `start` (required): Start date (ISO format)
  - `end` (required): End date (ISO format)
- **Response:** `List<ExpenseHeatmapPoint>` (200)
```json
[
  {
    "date": "2024-01-15",
    "amount": 1500.00,
    "level": 2
  }
]
```

#### Get Category Spending Summary
- **GET** `/api/expenses/analytics/categories?start=2024-01-01&end=2024-01-31`
- **Auth:** Required
- **Response:** `List<CategorySpendingSummary>` (200)

#### Get Spending Patterns
- **GET** `/api/expenses/analytics/patterns?start=2024-01-01&end=2024-01-31`
- **Auth:** Required
- **Response:** `List<SpendingPattern>` (200)

#### Get Weekly Patterns
- **GET** `/api/expenses/analytics/weekly?start=2024-01-01&end=2024-01-31`
- **Auth:** Required
- **Response:** `List<SpendingPattern>` (200)

#### Get Recurring Expenses
- **GET** `/api/expenses/analytics/recurring?start=2024-01-01&end=2024-01-31&category=Food`
- **Auth:** Required
- **Query Params:**
  - `category` (optional): Filter by category
- **Response:** `List<SpendingPattern>` (200)

#### Get Monthly Comparison
- **GET** `/api/expenses/analytics/comparison?month1Start=2024-01-01&month1End=2024-01-31&month2Start=2024-02-01&month2End=2024-02-29`
- **Auth:** Required
- **Response:** `Map<String, Object>` (200)

#### Categorize Expense
- **POST** `/api/expenses/categorize`
- **Auth:** Required
- **Body:**
```json
{
  "description": "Swiggy order",
  "normalizedMerchant": "swiggy",
  "amount": 350.00
}
```
- **Response:** `ExpenseCategorizationResponse` (200)

---

### Budget Endpoints

#### Create Budget
- **POST** `/api/budgets`
- **Auth:** Required
- **Body:**
```json
{
  "name": "January 2024 Budget",
  "amount": 30000.00,
  "monthYear": "2024-01-01"
}
```
- **Response:** `BudgetResponse` (201)

#### Update Budget
- **PUT** `/api/budgets/{id}`
- **Auth:** Required
- **Body:** `BudgetRequest`
- **Response:** `BudgetResponse` (200)

#### Delete Budget
- **DELETE** `/api/budgets/{id}`
- **Auth:** Required
- **Response:** 204 No Content

#### Deactivate Budget
- **POST** `/api/budgets/{id}/deactivate`
- **Auth:** Required
- **Response:** `BudgetResponse` (200)

#### List All Budgets
- **GET** `/api/budgets`
- **Auth:** Required
- **Response:** `List<BudgetResponse>` (200)

#### Get Budget by ID
- **GET** `/api/budgets/{id}`
- **Auth:** Required
- **Response:** `BudgetResponse` (200)

#### Get Current Budget
- **GET** `/api/budgets/current`
- **Auth:** Required
- **Response:** `BudgetResponse` (200) or 404 Not Found

#### Get Daily Spend Limit
- **GET** `/api/budgets/daily-limit?date=2024-01-15`
- **Auth:** Required
- **Query Params:**
  - `date` (optional): Date (defaults to today)
- **Response:** `DailySpendLimitResponse` (200)

#### Get Budget Coach Message
- **GET** `/api/budgets/coach?date=2024-01-15`
- **Auth:** Required
- **Query Params:**
  - `date` (optional): Date (defaults to today)
- **Response:** `CoachMessage` (200)

---

### Savings Goals Endpoints

#### Create Savings Goal
- **POST** `/api/savings-goals`
- **Auth:** Required
- **Body:**
```json
{
  "name": "New Laptop",
  "targetAmount": 70000.00,
  "targetDate": "2024-12-31"
}
```
- **Response:** `SavingsGoalResponse` (201)

#### Update Savings Goal
- **PUT** `/api/savings-goals/{id}`
- **Auth:** Required
- **Body:** `SavingsGoalRequest`
- **Response:** `SavingsGoalResponse` (200)

#### Delete Savings Goal
- **DELETE** `/api/savings-goals/{id}`
- **Auth:** Required
- **Response:** 204 No Content

#### Deactivate Savings Goal
- **POST** `/api/savings-goals/{id}/deactivate`
- **Auth:** Required
- **Response:** `SavingsGoalResponse` (200)

#### Add Amount to Goal
- **POST** `/api/savings-goals/{id}/add?amount=5000.00`
- **Auth:** Required
- **Query Params:**
  - `amount` (required): Amount to add
- **Response:** `SavingsGoalResponse` (200)

#### Withdraw Amount from Goal
- **POST** `/api/savings-goals/{id}/withdraw?amount=2000.00`
- **Auth:** Required
- **Query Params:**
  - `amount` (required): Amount to withdraw
- **Response:** `SavingsGoalResponse` (200)

#### Set Goal Amount
- **PUT** `/api/savings-goals/{id}/amount?amount=50000.00`
- **Auth:** Required
- **Query Params:**
  - `amount` (required): New amount
- **Response:** `SavingsGoalResponse` (200)

#### List All Savings Goals
- **GET** `/api/savings-goals`
- **Auth:** Required
- **Response:** `List<SavingsGoalResponse>` (200)

#### Get Savings Goal by ID
- **GET** `/api/savings-goals/{id}`
- **Auth:** Required
- **Response:** `SavingsGoalResponse` (200)

---

### Investment Endpoints

#### Create Investment
- **POST** `/api/investments`
- **Auth:** Required
- **Body:**
```json
{
  "name": "Fixed Deposit",
  "type": "FD",
  "principalAmount": 100000.00,
  "currentValue": 105000.00,
  "interestRate": 6.5,
  "startDate": "2023-01-01",
  "maturityDate": "2025-01-01",
  "notes": "Bank FD"
}
```
- **Response:** `InvestmentResponse` (201)

#### Update Investment
- **PUT** `/api/investments/{id}`
- **Auth:** Required
- **Body:** `InvestmentRequest`
- **Response:** `InvestmentResponse` (200)

#### Delete Investment
- **DELETE** `/api/investments/{id}`
- **Auth:** Required
- **Response:** 204 No Content

#### Deactivate Investment
- **POST** `/api/investments/{id}/deactivate`
- **Auth:** Required
- **Response:** `InvestmentResponse` (200)

#### Update Current Value
- **PUT** `/api/investments/{id}/current-value?currentValue=110000.00`
- **Auth:** Required
- **Query Params:**
  - `currentValue` (required): New current value
- **Response:** `InvestmentResponse` (200)

#### List All Investments
- **GET** `/api/investments?type=FD`
- **Auth:** Required
- **Query Params:**
  - `type` (optional): Filter by type (FD, SIP, RD, GOLD, CRYPTO, OTHER)
- **Response:** `List<InvestmentResponse>` (200)

#### Get Investment by ID
- **GET** `/api/investments/{id}`
- **Auth:** Required
- **Response:** `InvestmentResponse` (200)

#### Get Upcoming Maturities
- **GET** `/api/investments/maturities?start=2024-01-01&end=2024-12-31`
- **Auth:** Required
- **Query Params:**
  - `start` (required): Start date
  - `end` (required): End date
- **Response:** `List<InvestmentResponse>` (200)

---

### Bill Reminder Endpoints

#### Create Bill
- **POST** `/api/reminders/bills`
- **Auth:** Required
- **Body:**
```json
{
  "name": "Electricity Bill",
  "category": "Utilities",
  "amount": 2000.00,
  "nextDueDate": "2024-02-01",
  "frequency": "MONTHLY",
  "remindDaysBefore": 3
}
```
- **Response:** `BillResponse` (201)

#### List All Bills
- **GET** `/api/reminders/bills`
- **Auth:** Required
- **Response:** `List<BillResponse>` (200)

#### Update Bill
- **PUT** `/api/reminders/bills/{id}`
- **Auth:** Required
- **Body:** `BillRequest`
- **Response:** `BillResponse` (200)

#### Mark Bill as Paid
- **POST** `/api/reminders/bills/{id}/mark-paid`
- **Auth:** Required
- **Body:**
```json
{
  "paidDate": "2024-01-28",
  "amount": 2000.00
}
```
- **Response:** `BillResponse` (200)

#### Pause Bill
- **POST** `/api/reminders/bills/{id}/pause`
- **Auth:** Required
- **Response:** `BillResponse` (200)

#### Resume Bill
- **POST** `/api/reminders/bills/{id}/resume`
- **Auth:** Required
- **Response:** `BillResponse` (200)

#### Delete Bill
- **DELETE** `/api/reminders/bills/{id}`
- **Auth:** Required
- **Response:** 204 No Content

---

### Split Bills Endpoints

#### Create Expense Group
- **POST** `/api/split-bills/groups`
- **Auth:** Required
- **Body:**
```json
{
  "name": "Trip to Goa",
  "description": "Weekend trip expenses",
  "createdBy": "johndoe"
}
```
- **Response:** `ExpenseGroupResponse` (201)

#### Update Expense Group
- **PUT** `/api/split-bills/groups/{id}`
- **Auth:** Required
- **Body:** `ExpenseGroupRequest`
- **Response:** `ExpenseGroupResponse` (200)

#### Delete Expense Group
- **DELETE** `/api/split-bills/groups/{id}`
- **Auth:** Required
- **Response:** 204 No Content

#### List All Groups
- **GET** `/api/split-bills/groups`
- **Auth:** Required
- **Response:** `List<ExpenseGroupResponse>` (200)

#### Get Group by ID
- **GET** `/api/split-bills/groups/{id}`
- **Auth:** Required
- **Response:** `ExpenseGroupResponse` (200)

#### Split Expense
- **POST** `/api/split-bills/split`
- **Auth:** Required
- **Body:**
```json
{
  "groupId": "uuid-here",
  "expenseId": "uuid-here",
  "description": "Hotel booking",
  "paidBy": "johndoe",
  "amount": 5000.00,
  "memberShares": {
    "johndoe": 2000.00,
    "janedoe": 1500.00,
    "bob": 1500.00
  }
}
```
- **Response:** `List<ExpenseShare>` (201)

#### Create Settlement
- **POST** `/api/split-bills/settlements`
- **Auth:** Required
- **Body:**
```json
{
  "groupId": "uuid-here",
  "fromMember": "janedoe",
  "toMember": "johndoe",
  "amount": 500.00,
  "notes": "Settled via UPI"
}
```
- **Response:** `SettlementResponse` (201)

#### Mark Settlement as Settled
- **POST** `/api/split-bills/settlements/{id}/settle`
- **Auth:** Required
- **Response:** `SettlementResponse` (200)

#### Get Settlements for Group
- **GET** `/api/split-bills/settlements?groupId=uuid-here`
- **Auth:** Required
- **Query Params:**
  - `groupId` (required): Group UUID
- **Response:** `List<SettlementResponse>` (200)

#### Get Pending Settlements
- **GET** `/api/split-bills/settlements/pending?groupId=uuid-here`
- **Auth:** Required
- **Response:** `List<SettlementResponse>` (200)

#### Calculate Suggested Settlements
- **POST** `/api/split-bills/settlements/calculate?groupId=uuid-here`
- **Auth:** Required
- **Query Params:**
  - `groupId` (required): Group UUID
- **Response:** `List<SettlementResponse>` (200)

---

### Receipt Scanning Endpoints

#### Scan Receipt
- **POST** `/api/receipts/scan`
- **Auth:** Required
- **Body:**
```json
{
  "imageData": "base64-encoded-image-string"
}
```
- **Response:** `ReceiptScanResponse` (200)
```json
{
  "amount": 350.00,
  "merchant": "Swiggy",
  "date": "2024-01-15",
  "category": "Food",
  "rawText": "OCR extracted text...",
  "expenseCreateRequest": { ... }
}
```

#### Scan and Create Expense
- **POST** `/api/receipts/scan-and-create`
- **Auth:** Required
- **Body:** Same as Scan Receipt
- **Response:** `ExpenseResponse` (201)

**Frontend Implementation:**
```javascript
// Convert image file to base64
const convertToBase64 = (file) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.readAsDataURL(file);
    reader.onload = () => resolve(reader.result.split(',')[1]);
    reader.onerror = error => reject(error);
  });
};

// Scan receipt
const scanReceipt = async (imageFile) => {
  const base64Image = await convertToBase64(imageFile);
  const response = await api.post('/api/receipts/scan', {
    imageData: base64Image
  });
  return response.data;
};
```

---

### User Preferences Endpoints

#### Get User Preferences
- **GET** `/api/preferences/{userId}`
- **Auth:** Required
- **Response:** `UserPreferencesResponse` (200)
```json
{
  "id": "uuid",
  "userId": "uuid",
  "theme": "dark",
  "currency": "INR",
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-01T00:00:00Z"
}
```

#### Update User Preferences
- **PUT** `/api/preferences/{userId}`
- **Auth:** Required
- **Body:**
```json
{
  "theme": "dark",
  "currency": "USD"
}
```
- **Response:** `UserPreferencesResponse` (200)

---

## Frontend Implementation Guide

### 1. Setup API Client

**React Example:**
```javascript
// api/client.js
import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor - Add auth token
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('authToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor - Handle errors
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Unauthorized - clear auth and redirect
      localStorage.removeItem('authToken');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default apiClient;
```

### 2. Authentication Service

```javascript
// services/authService.js
import apiClient from '../api/client';

export const authService = {
  async register(username, email, password) {
    const response = await apiClient.post('/api/auth/register', {
      username,
      email,
      password,
    });
    return response.data;
  },

  async login(username, password) {
    const response = await apiClient.post('/api/auth/login', {
      username,
      password,
    });
    return response.data;
  },

  logout() {
    localStorage.removeItem('authToken');
    localStorage.removeItem('user');
  },

  isAuthenticated() {
    return !!localStorage.getItem('authToken');
  },

  getCurrentUser() {
    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
  },
};
```

### 3. Expense Service

```javascript
// services/expenseService.js
import apiClient from '../api/client';

export const expenseService = {
  async createExpense(expenseData) {
    const response = await apiClient.post('/api/expenses', expenseData);
    return response.data;
  },

  async createExpenseWithCoach(expenseData) {
    const response = await apiClient.post('/api/expenses/with-coach', expenseData);
    return response.data;
  },

  async getExpenses() {
    const response = await apiClient.get('/api/expenses');
    return response.data;
  },

  async getHeatmap(startDate, endDate) {
    const response = await apiClient.get('/api/expenses/heatmap', {
      params: { start: startDate, end: endDate },
    });
    return response.data;
  },

  async getCategorySpending(startDate, endDate) {
    const response = await apiClient.get('/api/expenses/analytics/categories', {
      params: { start: startDate, end: endDate },
    });
    return response.data;
  },
};
```

### 4. Protected Route Component

```javascript
// components/ProtectedRoute.jsx
import { Navigate } from 'react-router-dom';
import { authService } from '../services/authService';

const ProtectedRoute = ({ children }) => {
  if (!authService.isAuthenticated()) {
    return <Navigate to="/login" replace />;
  }
  return children;
};

export default ProtectedRoute;
```

### 5. Example: Expense Form Component

```javascript
// components/ExpenseForm.jsx
import { useState } from 'react';
import { expenseService } from '../services/expenseService';

const ExpenseForm = () => {
  const [formData, setFormData] = useState({
    description: '',
    merchant: '',
    amount: '',
    transactionDate: new Date().toISOString().split('T')[0],
    category: '',
    paymentMethod: '',
  });

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const response = await expenseService.createExpenseWithCoach({
        ...formData,
        amount: parseFloat(formData.amount),
      });
      
      // Show coach message if available
      if (response.coachMessage) {
        alert(response.coachMessage.message);
      }
      
      // Reset form or navigate
      setFormData({ ...formData, description: '', amount: '' });
    } catch (error) {
      console.error('Error creating expense:', error);
      alert('Failed to create expense');
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      {/* Form fields */}
    </form>
  );
};
```

---

## Error Handling

### Standard Error Response Format

```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/expenses",
  "errors": [
    {
      "field": "amount",
      "message": "Amount must be greater than zero"
    }
  ]
}
```

### Common HTTP Status Codes

- **200 OK** - Request successful
- **201 Created** - Resource created successfully
- **204 No Content** - Request successful, no content to return
- **400 Bad Request** - Invalid request data
- **401 Unauthorized** - Authentication required or token invalid
- **403 Forbidden** - Insufficient permissions
- **404 Not Found** - Resource not found
- **429 Too Many Requests** - Rate limit exceeded
- **500 Internal Server Error** - Server error

### Frontend Error Handling

```javascript
// utils/errorHandler.js
export const handleApiError = (error) => {
  if (error.response) {
    // Server responded with error
    const { status, data } = error.response;
    
    switch (status) {
      case 400:
        return data.message || 'Invalid request';
      case 401:
        return 'Please login again';
      case 403:
        return 'You do not have permission';
      case 404:
        return 'Resource not found';
      case 429:
        return 'Too many requests. Please try again later.';
      case 500:
        return 'Server error. Please try again later.';
      default:
        return 'An error occurred';
    }
  } else if (error.request) {
    // Request made but no response
    return 'Network error. Please check your connection.';
  } else {
    // Error in request setup
    return 'An unexpected error occurred';
  }
};
```

---

## Security Considerations

### 1. Token Storage
- **DO:** Store JWT tokens in `localStorage` or `sessionStorage` for web apps
- **DO:** Use secure storage (Keychain/Keystore) for mobile apps
- **DON'T:** Store tokens in cookies without `HttpOnly` flag (XSS risk)

### 2. Token Refresh
- Tokens expire after 24 hours (configurable)
- Implement token refresh logic or redirect to login on 401

### 3. CORS
- Configure allowed origins in backend
- Frontend must be from allowed origin

### 4. Rate Limiting
- Auth endpoints: 5 requests/minute
- API endpoints: 100 requests/minute
- Handle 429 errors gracefully

### 5. Input Validation
- Always validate user input on frontend
- Backend also validates - don't rely solely on frontend validation

### 6. HTTPS
- Always use HTTPS in production
- Never send sensitive data over HTTP

---

## Best Practices

### 1. Loading States
```javascript
const [loading, setLoading] = useState(false);

const fetchData = async () => {
  setLoading(true);
  try {
    const data = await api.get('/api/expenses');
    // Handle data
  } finally {
    setLoading(false);
  }
};
```

### 2. Error Boundaries
```javascript
// React Error Boundary
class ErrorBoundary extends React.Component {
  componentDidCatch(error, errorInfo) {
    // Log error to monitoring service
    console.error('Error:', error, errorInfo);
  }
  
  render() {
    if (this.state.hasError) {
      return <ErrorFallback />;
    }
    return this.props.children;
  }
}
```

### 3. Optimistic Updates
```javascript
// Update UI immediately, rollback on error
const deleteExpense = async (id) => {
  // Optimistically remove from UI
  setExpenses(expenses.filter(e => e.id !== id));
  
  try {
    await api.delete(`/api/expenses/${id}`);
  } catch (error) {
    // Rollback on error
    setExpenses(originalExpenses);
    alert('Failed to delete expense');
  }
};
```

### 4. Date Formatting
- Always use ISO 8601 format (YYYY-MM-DD) for dates
- Use `toISOString().split('T')[0]` to format dates

### 5. Amount Formatting
- Store amounts as numbers (not strings)
- Format for display: `new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' }).format(amount)`

---

## Testing

### Example Test Cases

```javascript
// tests/expenseService.test.js
import { expenseService } from '../services/expenseService';

describe('ExpenseService', () => {
  it('should create expense', async () => {
    const expense = await expenseService.createExpense({
      description: 'Test',
      amount: 100,
      transactionDate: '2024-01-15',
    });
    expect(expense.id).toBeDefined();
  });
});
```

---

## Support

For issues or questions:
1. Check error messages in browser console
2. Verify authentication token is valid
3. Check network tab for request/response details
4. Review API documentation

---

**Last Updated:** January 2024
**API Version:** 1.0





