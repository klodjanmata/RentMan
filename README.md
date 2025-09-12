# RentMan - Professional Car Rental Management Platform

A comprehensive, multi-company car rental platform built with Spring Boot and React. RentMan allows rental companies to manage their fleets, employees, and operations while providing customers with a unified platform to search and book vehicles across multiple providers.

## 🚀 Features

### For Customers
- **Unified Search**: Search vehicles across all registered rental companies
- **Advanced Filters**: Filter by price, vehicle type, features, location, and more
- **Easy Booking**: Simple reservation process with instant confirmation
- **User Dashboard**: Manage reservations, view history, and update profile
- **Mobile Responsive**: Access from any device with a modern, responsive design

### For Rental Companies
- **Fleet Management**: Add, update, and manage vehicle inventory
- **Employee Management**: Manage staff with role-based permissions
- **Maintenance Tracking**: Schedule and track vehicle maintenance
- **Defect Reporting**: Report and manage vehicle defects
- **Invoice Management**: Generate and manage invoices
- **Analytics Dashboard**: View business metrics and performance reports
- **Subscription Management**: Manage subscription plans and billing

### For Platform Administrators
- **Company Management**: Approve and manage rental companies
- **User Management**: Manage all users across the platform
- **Revenue Tracking**: Monitor platform revenue and commissions
- **System Analytics**: Platform-wide statistics and insights

## 🏗️ Architecture

### Backend (Spring Boot)
- **Framework**: Spring Boot 3.5.5
- **Database**: SQL Server with JPA/Hibernate
- **Security**: JWT-based authentication with Spring Security
- **API**: RESTful APIs with comprehensive validation
- **Architecture**: Multi-tenant with company-based data isolation

### Frontend (React)
- **Framework**: React 18 with TypeScript
- **UI Library**: Material-UI (MUI) for modern, responsive design
- **State Management**: React Query for server state
- **Routing**: React Router for client-side navigation
- **Forms**: React Hook Form with Yup validation

## 📁 Project Structure

```
RentMan/
├── src/main/java/com/rentman/rentman/
│   ├── controller/          # REST controllers
│   ├── service/            # Business logic services
│   ├── repository/         # Data access layer
│   ├── entity/             # JPA entities
│   ├── dto/                # Data transfer objects
│   ├── config/             # Configuration classes
│   └── security/           # Security configuration
├── frontend/               # React frontend application
│   ├── src/
│   │   ├── components/     # Reusable UI components
│   │   ├── pages/          # Page components
│   │   ├── services/       # API services
│   │   ├── types/          # TypeScript definitions
│   │   └── contexts/       # React contexts
├── API-Test/              # API testing files
└── docker-compose.yaml    # Docker configuration
```

## 🚀 Getting Started

### Prerequisites
- Java 17 or higher
- Node.js 16 or higher
- SQL Server
- Maven 3.6+

### Backend Setup

1. **Clone the repository**
```bash
git clone <repository-url>
cd RentMan
```

2. **Configure Database**
   - Install SQL Server
   - Create a database named `rentman`
   - Update `src/main/resources/application.properties` with your database credentials

3. **Run the application**
```bash
mvn spring-boot:run
```

The backend will be available at `http://localhost:8080`

### Frontend Setup

1. **Navigate to frontend directory**
```bash
cd frontend
```

2. **Install dependencies**
```bash
npm install
```

3. **Start the development server**
```bash
npm start
```

The frontend will be available at `http://localhost:3000`

## 🔧 Configuration

### Backend Configuration
Update `src/main/resources/application.properties`:

```properties
# Database Configuration
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=rentman
spring.datasource.username=your_username
spring.datasource.password=your_password

# JWT Configuration
jwt.secret=your-secret-key
jwt.expiration=86400000

# Email Configuration
spring.mail.host=smtp.gmail.com
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
```

### Frontend Configuration
Create `frontend/.env`:

```
REACT_APP_API_URL=http://localhost:8080/api
```

## 🧪 Testing

### Backend Testing
```bash
mvn test
```

### Frontend Testing
```bash
cd frontend
npm test
```

### API Testing
Use the files in the `API-Test/` directory to test the REST APIs:
- `user-api-test.http`
- `vehicle-api-tests.http`
- `reservation-api-test.http`

## 📊 Database Schema

### Core Entities
- **User**: Customers, employees, and administrators
- **Company**: Rental companies with subscription management
- **Vehicle**: Fleet vehicles with detailed specifications
- **Reservation**: Booking records with status tracking
- **Maintenance**: Vehicle maintenance records
- **Defect**: Vehicle defect reporting and tracking
- **Invoice**: Billing and financial management

### Key Relationships
- Companies have many vehicles, employees, and reservations
- Users belong to companies (for employees/admins)
- Reservations link customers, vehicles, and companies
- Maintenance and defects are linked to specific vehicles

## 🔐 Security

- **JWT Authentication**: Secure token-based authentication
- **Role-Based Access Control**: Different permissions for customers, employees, company admins, and platform admins
- **Data Isolation**: Company-based data separation for multi-tenancy
- **Input Validation**: Comprehensive validation on all inputs
- **CORS Configuration**: Proper cross-origin resource sharing setup

## 🚀 Deployment

### Docker Deployment
```bash
docker-compose up -d
```

### Manual Deployment
1. Build the backend: `mvn clean package`
2. Build the frontend: `cd frontend && npm run build`
3. Deploy the JAR file and serve the frontend build files

## 📈 Future Enhancements

- **Payment Integration**: Stripe/PayPal integration for online payments
- **Notification System**: Email and SMS notifications
- **Mobile App**: React Native mobile application
- **Advanced Analytics**: Machine learning for demand forecasting
- **API Documentation**: Swagger/OpenAPI documentation
- **Caching**: Redis for improved performance
- **File Upload**: Image upload for vehicles and documents

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🆘 Support

For support and questions, please create an issue in the repository or contact the development team.

---

**RentMan** - Professional Car Rental Management Platform
Built with ❤️ using Spring Boot and React
