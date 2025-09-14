import axios, { AxiosInstance, AxiosResponse } from 'axios';
import { LoginRequest, RegisterRequest, User, AuthResponse } from '../types/auth';
import { Vehicle, VehicleSearchParams } from '../types/vehicle';
import { Company } from '../types/company';
import { Reservation, ReservationCreateRequest } from '../types/reservation';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

class ApiService {
  private api: AxiosInstance;

  constructor() {
    this.api = axios.create({
      baseURL: API_BASE_URL,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    // Request interceptor to add auth token
    this.api.interceptors.request.use(
      (config) => {
        const token = localStorage.getItem('token');
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
      },
      (error) => {
        return Promise.reject(error);
      }
    );

    // Response interceptor to handle auth errors
    this.api.interceptors.response.use(
      (response) => response,
      (error) => {
        if (error.response?.status === 401) {
          localStorage.removeItem('token');
          window.location.href = '/login';
        }
        return Promise.reject(error);
      }
    );
  }

  // Auth endpoints
  async login(credentials: LoginRequest): Promise<AuthResponse> {
    const response: AxiosResponse<AuthResponse> = await this.api.post('/auth/login', credentials);
    return response.data;
  }

  async register(userData: RegisterRequest): Promise<AuthResponse> {
    const response: AxiosResponse<AuthResponse> = await this.api.post('/auth/register', userData);
    return response.data;
  }

  async getCurrentUser(): Promise<User> {
    const response: AxiosResponse<{ user: User }> = await this.api.get('/auth/me');
    return response.data.user;
  }

  async logout(): Promise<void> {
    await this.api.post('/auth/logout');
  }

  async changePassword(passwordData: { currentPassword: string; newPassword: string }): Promise<void> {
    await this.api.post('/auth/change-password', passwordData);
  }

  // Vehicle endpoints
  async searchVehicles(params: VehicleSearchParams): Promise<Vehicle[]> {
    const response: AxiosResponse<Vehicle[]> = await this.api.get('/search/vehicles', { params });
    return response.data;
  }

  async getVehicle(id: string): Promise<Vehicle> {
    const response: AxiosResponse<Vehicle> = await this.api.get(`/vehicles/${id}`);
    return response.data;
  }

  async getCompanyVehicles(companyId: string): Promise<Vehicle[]> {
    const response: AxiosResponse<Vehicle[]> = await this.api.get(`/companies/${companyId}/vehicles`);
    return response.data;
  }

  // Company endpoints
  async getCompanies(): Promise<Company[]> {
    const response: AxiosResponse<Company[]> = await this.api.get('/companies');
    return response.data;
  }

  async getCompany(id: string): Promise<Company> {
    const response: AxiosResponse<Company> = await this.api.get(`/companies/${id}`);
    return response.data;
  }

  async getActiveCompanies(): Promise<Company[]> {
    const response: AxiosResponse<Company[]> = await this.api.get('/companies/active');
    return response.data;
  }

  async getFeaturedCompanies(): Promise<Company[]> {
    const response: AxiosResponse<Company[]> = await this.api.get('/companies/featured');
    return response.data;
  }

  // Reservation endpoints
  async createReservation(reservationData: ReservationCreateRequest): Promise<Reservation> {
    const response: AxiosResponse<Reservation> = await this.api.post('/reservations', reservationData);
    return response.data;
  }

  async getUserReservations(): Promise<Reservation[]> {
    const response: AxiosResponse<Reservation[]> = await this.api.get('/reservations');
    return response.data;
  }

  async getReservation(id: string): Promise<Reservation> {
    const response: AxiosResponse<Reservation> = await this.api.get(`/reservations/${id}`);
    return response.data;
  }

  async cancelReservation(id: string): Promise<void> {
    await this.api.patch(`/reservations/${id}/cancel`);
  }

  // Company management endpoints
  async getCompanyDashboard(companyId: string): Promise<any> {
    const response = await this.api.get(`/companies/${companyId}/dashboard`);
    return response.data;
  }

  async getCompanyStatistics(companyId: string): Promise<any> {
    const response = await this.api.get(`/companies/${companyId}/statistics`);
    return response.data;
  }

  async getCompanyRevenue(companyId: string, startDate?: string, endDate?: string): Promise<any> {
    const params = startDate && endDate ? { startDate, endDate } : {};
    const response = await this.api.get(`/companies/${companyId}/revenue`, { params });
    return response.data;
  }

  async createCompany(companyData: any): Promise<any> {
    const response = await this.api.post('/companies', companyData);
    return response.data;
  }

  async registerCompanyWithAdmin(companyData: any): Promise<any> {
    const response = await this.api.post('/companies/register', companyData);
    return response.data;
  }
}

export const apiService = new ApiService();

// Export individual API modules for better organization
export const authApi = {
  login: (credentials: LoginRequest) => apiService.login(credentials),
  register: (userData: RegisterRequest) => apiService.register(userData),
  getCurrentUser: () => apiService.getCurrentUser(),
  logout: () => apiService.logout(),
  changePassword: (passwordData: { currentPassword: string; newPassword: string }) => 
    apiService.changePassword(passwordData),
};

export const vehicleApi = {
  search: (params: VehicleSearchParams) => apiService.searchVehicles(params),
  getById: (id: string) => apiService.getVehicle(id),
  getByCompany: (companyId: string) => apiService.getCompanyVehicles(companyId),
};

export const companyApi = {
  getAll: () => apiService.getCompanies(),
  getById: (id: string) => apiService.getCompany(id),
  getActive: () => apiService.getActiveCompanies(),
  getFeatured: () => apiService.getFeaturedCompanies(),
  getDashboard: (id: string) => apiService.getCompanyDashboard(id),
  getStatistics: (id: string) => apiService.getCompanyStatistics(id),
  getRevenue: (id: string, startDate?: string, endDate?: string) => 
    apiService.getCompanyRevenue(id, startDate, endDate),
  create: (companyData: any) => apiService.createCompany(companyData),
  register: (companyData: any) => apiService.registerCompanyWithAdmin(companyData),
};

export const reservationApi = {
  create: (data: ReservationCreateRequest) => apiService.createReservation(data),
  getUserReservations: () => apiService.getUserReservations(),
  getById: (id: string) => apiService.getReservation(id),
  cancel: (id: string) => apiService.cancelReservation(id),
};
