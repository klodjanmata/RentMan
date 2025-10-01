# Platform Admin Guide

## Overview
The Platform Admin system allows you to manage and approve companies that register on the RentMan platform. This is essential for the system to function properly, as companies must be approved before their vehicles appear in search results.

## Quick Start

### 1. Create Platform Admin Account

**Option A: Using API Test File (Recommended for first-time setup)**

1. Open `API-Test/platform-admin-api-test.http`
2. Find "STEP 1: Register Platform Admin"
3. Modify the request with your details:
```json
{
  "firstName": "Your",
  "lastName": "Name",
  "email": "admin@example.com",
  "password": "YourSecurePassword",
  "phoneNumber": "+1234567890"
}
```
4. Execute the request
5. Save the JWT token from the response

**Option B: Using cURL**
```bash
curl -X POST http://localhost:8080/api/auth/register-platform-admin \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Admin",
    "lastName": "User",
    "email": "admin@rentman.com",
    "password": "admin123",
    "phoneNumber": "+1234567890"
  }'
```

### 2. Login as Platform Admin

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@rentman.com",
    "password": "admin123"
  }'
```

Copy the JWT token from the response.

### 3. Access the Platform Admin Dashboard

**Frontend:**
Navigate to: `http://localhost:3000/platform-admin`

**Backend API:**
Use the token to access admin endpoints:
```bash
GET http://localhost:8080/api/platform-admin/dashboard
Authorization: Bearer YOUR_TOKEN_HERE
```

## Approving Your First Company

### Problem
When a company registers with 4 vehicles, the vehicles don't appear in search results because the company status is `PENDING_APPROVAL`.

### Solution
1. Login as platform admin
2. Get pending companies:
```bash
GET http://localhost:8080/api/platform-admin/companies/pending
Authorization: Bearer YOUR_TOKEN
```

3. Note the company ID from the response
4. Approve the company:
```bash
POST http://localhost:8080/api/platform-admin/companies/{companyId}/approve
Authorization: Bearer YOUR_TOKEN
```

5. The company status changes to `ACTIVE`, and vehicles will now appear in search results!

## Platform Admin Features

### Dashboard Statistics
- Total companies
- Pending approvals
- Active companies
- Total users

### Company Management
- **Approve** - Change company status from PENDING_APPROVAL to ACTIVE
- **Reject** - Mark company as REJECTED with reason
- **Suspend** - Temporarily suspend an active company
- **Reactivate** - Restore a suspended company

### API Endpoints

#### GET /api/platform-admin/dashboard
Get dashboard statistics and overview

#### GET /api/platform-admin/companies/pending
Get all companies awaiting approval

#### GET /api/platform-admin/companies
Get all companies (with optional status filter)

#### POST /api/platform-admin/companies/{id}/approve
Approve a pending company

#### POST /api/platform-admin/companies/{id}/reject
Reject a company with reason

#### POST /api/platform-admin/companies/{id}/suspend
Suspend an active company

#### POST /api/platform-admin/companies/{id}/reactivate
Reactivate a suspended company

## Frontend Dashboard

Access the Platform Admin Dashboard at: `/platform-admin`

Features:
- View all pending company approvals
- One-click approve/reject buttons
- Detailed company information
- Real-time statistics
- Action confirmations

## Security Notes

⚠️ **Important for Production:**

1. **Secure the Registration Endpoint**
   - Add a secret key check
   - Disable after creating the first admin
   - Move to admin-only endpoint

2. **Add Authorization Checks**
   - Currently implemented in backend
   - Verify JWT token role is ADMIN
   - All admin endpoints are protected

3. **Audit Logging**
   - Who approved/rejected companies
   - When actions were taken
   - Already tracked via `approvedBy` and `approvedAt` fields

## Testing Workflow

1. **Register a test company** (as a company owner)
2. **Login as platform admin**
3. **View pending companies**
4. **Approve the test company**
5. **Verify vehicles appear in search**

## Troubleshooting

### Vehicles Not Showing in Search
- Check company status: must be `ACTIVE`
- Check vehicle status: must be `AVAILABLE`
- Use the platform admin dashboard to approve the company

### Cannot Access Admin Dashboard
- Verify you're logged in as a user with `ADMIN` role
- Check JWT token is valid and not expired
- Ensure backend is running on port 8080

### API Returns 403 Forbidden
- User role must be `ADMIN` (Platform Administrator)
- Not `COMPANY_ADMIN` (which is for company-level administration)
- Re-login to get fresh token

## Database Schema

### User Role Enum
```java
CUSTOMER         // Regular customers
EMPLOYEE         // Company employees
COMPANY_ADMIN    // Company administrators
ADMIN            // Platform administrators ← Required for platform admin features
```

### Company Status Enum
```java
PENDING_APPROVAL // Default status when company registers
ACTIVE          // Approved and operational
SUSPENDED       // Temporarily suspended
INACTIVE        // Not operational
REJECTED        // Application rejected
```

## Future Enhancements

- Email notifications on approval/rejection
- Document verification system
- Multi-level approval workflow
- Company application review notes
- Automated verification checks

