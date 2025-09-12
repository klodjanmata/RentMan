export interface User {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  role: UserRole;
  status: UserStatus;
  emailVerified: boolean;
  phoneVerified: boolean;
  company?: {
    id: number;
    name: string;
  };
  permissions: {
    canManageFleet: boolean;
    canManageReservations: boolean;
    canManageEmployees: boolean;
    canViewReports: boolean;
    canManageFinances: boolean;
  };
}

export enum UserRole {
  CUSTOMER = 'CUSTOMER',
  EMPLOYEE = 'EMPLOYEE',
  COMPANY_ADMIN = 'COMPANY_ADMIN',
  ADMIN = 'ADMIN'
}

export enum UserStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  SUSPENDED = 'SUSPENDED',
  BANNED = 'BANNED'
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  phoneNumber: string;
  dateOfBirth?: string;
  driverLicenseNumber?: string;
  licenseExpiryDate?: string;
  streetAddress?: string;
  city?: string;
  state?: string;
  postalCode?: string;
  country?: string;
  role?: UserRole;
  companyId?: number;
  employeeId?: string;
  department?: string;
  hireDate?: string;
}

export interface AuthResponse {
  token: string;
  type: string;
  expiresIn: number;
  user: User;
  message?: string;
}
