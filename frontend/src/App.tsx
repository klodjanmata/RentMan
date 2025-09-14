import React from 'react';
import { Routes, Route } from 'react-router-dom';
import { AuthProvider } from './contexts/AuthContext';
import { ProtectedRoute } from './components/ProtectedRoute';
import { Layout } from './components/Layout';
import { HomePage } from './pages/HomePage';
import { LoginPage } from './pages/LoginPage';
import { RegisterPage } from './pages/RegisterPage';
import { SearchPage } from './pages/SearchPage';
import { VehicleDetailsPage } from './pages/VehicleDetailsPage';
import { DashboardPage } from './pages/DashboardPage';
import { CompanyDashboardPage } from './pages/CompanyDashboardPage';
import { ProfilePage } from './pages/ProfilePage';
import { ReservationsPage } from './pages/ReservationsPage';
import { FleetManagementPage } from './pages/FleetManagementPage';
import { EmployeeManagementPage } from './pages/EmployeeManagementPage';
import { MaintenancePage } from './pages/MaintenancePage';
import { DefectsPage } from './pages/DefectsPage';
import { InvoicesPage } from './pages/InvoicesPage';
import { ReportsPage } from './pages/ReportsPage';
import { CompanyRegisterPage } from './pages/CompanyRegisterPage';

function App() {
  return (
    <AuthProvider>
      <Routes>
        {/* Public Routes */}
        <Route path="/" element={<Layout />}>
          <Route index element={<HomePage />} />
          <Route path="login" element={<LoginPage />} />
          <Route path="register" element={<RegisterPage />} />
          <Route path="register-company" element={<CompanyRegisterPage />} />
          <Route path="search" element={<SearchPage />} />
          <Route path="vehicles/:id" element={<VehicleDetailsPage />} />
        </Route>

        {/* Protected Routes */}
        <Route path="/" element={<ProtectedRoute><Layout /></ProtectedRoute>}>
          <Route path="dashboard" element={<DashboardPage />} />
          <Route path="profile" element={<ProfilePage />} />
          <Route path="reservations" element={<ReservationsPage />} />
          
          {/* Company Management Routes */}
          <Route path="company/dashboard" element={<CompanyDashboardPage />} />
          <Route path="company/fleet" element={<FleetManagementPage />} />
          <Route path="company/employees" element={<EmployeeManagementPage />} />
          <Route path="company/maintenance" element={<MaintenancePage />} />
          <Route path="company/defects" element={<DefectsPage />} />
          <Route path="company/invoices" element={<InvoicesPage />} />
          <Route path="company/reports" element={<ReportsPage />} />
        </Route>
      </Routes>
    </AuthProvider>
  );
}

export default App;
