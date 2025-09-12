# RentMan Frontend

A modern React TypeScript frontend for the RentMan car rental platform.

## Features

- **Modern UI**: Built with Material-UI (MUI) for a professional look
- **TypeScript**: Full type safety throughout the application
- **Authentication**: JWT-based authentication with role-based access control
- **Responsive Design**: Mobile-first design that works on all devices
- **State Management**: React Query for server state management
- **Form Handling**: React Hook Form with Yup validation
- **Routing**: React Router for client-side routing

## Tech Stack

- React 18
- TypeScript
- Material-UI (MUI)
- React Router
- React Query
- React Hook Form
- Yup
- Axios
- Day.js

## Getting Started

1. Install dependencies:
```bash
npm install
```

2. Start the development server:
```bash
npm start
```

3. Open [http://localhost:3000](http://localhost:3000) to view it in the browser.

## Available Scripts

- `npm start` - Runs the app in development mode
- `npm build` - Builds the app for production
- `npm test` - Launches the test runner
- `npm eject` - Ejects from Create React App (one-way operation)

## Project Structure

```
src/
├── components/          # Reusable UI components
├── contexts/           # React contexts (Auth, etc.)
├── pages/              # Page components
├── services/           # API services
├── types/              # TypeScript type definitions
├── App.tsx             # Main app component
└── index.tsx           # App entry point
```

## Environment Variables

Create a `.env` file in the root directory:

```
REACT_APP_API_URL=http://localhost:8080/api
```

## Features Implemented

- ✅ Authentication system with JWT
- ✅ Responsive navigation
- ✅ Vehicle search with filters
- ✅ Company listings
- ✅ Protected routes
- ✅ Modern UI components

## Features To Be Implemented

- 🔄 User registration form
- 🔄 Vehicle details page
- 🔄 Reservation system
- 🔄 Company dashboard
- 🔄 Fleet management
- 🔄 Employee management
- 🔄 Maintenance tracking
- 🔄 Defect reporting
- 🔄 Invoice management
- 🔄 Reports and analytics
- 🔄 Payment integration
- 🔄 Email notifications
