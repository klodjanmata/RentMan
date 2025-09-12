export interface Company {
  id: number;
  companyName: string;
  businessRegistrationNumber: string;
  taxId: string;
  email: string;
  phoneNumber: string;
  websiteUrl?: string;
  description?: string;
  streetAddress: string;
  city: string;
  state: string;
  postalCode: string;
  country: string;
  status: CompanyStatus;
  isVerified: boolean;
  isFeatured: boolean;
  subscriptionPlan: SubscriptionPlan;
  subscriptionStartDate: string;
  subscriptionEndDate: string;
  monthlyFee: number;
  commissionRate: number;
  totalRevenueGenerated: number;
  totalPlatformCommission: number;
  createdAt: string;
  updatedAt: string;
}

export enum CompanyStatus {
  PENDING = 'PENDING',
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  SUSPENDED = 'SUSPENDED',
  CLOSED = 'CLOSED'
}

export enum SubscriptionPlan {
  BASIC = 'BASIC',
  PROFESSIONAL = 'PROFESSIONAL',
  ENTERPRISE = 'ENTERPRISE',
  CUSTOM = 'CUSTOM'
}
