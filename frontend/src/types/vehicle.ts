export interface Vehicle {
  id: number;
  make: string;
  model: string;
  year: number;
  type: VehicleType;
  status: VehicleStatus;
  dailyRate: number;
  mileage: number;
  color: string;
  fuelType: string;
  transmission: string;
  seatingCapacity: number;
  features: string[];
  description: string;
  imageUrls: string[];
  company: {
    id: number;
    companyName: string;
    email: string;
    phoneNumber: string;
    streetAddress: string;
    city: string;
    state: string;
    postalCode: string;
    country: string;
  };
  isAvailableForRental: boolean;
  isFeatured: boolean;
  currentLocation: string;
  pickupLocation: string;
  insuranceIncluded: boolean;
  insuranceDailyRate: number;
  depositRequired: number;
  minimumRentalDays: number;
  maximumRentalDays: number;
  cancellationPolicy: string;
  termsAndConditions: string;
  createdAt: string;
  updatedAt: string;
}

export enum VehicleType {
  ECONOMY = 'ECONOMY',
  COMPACT = 'COMPACT',
  INTERMEDIATE = 'INTERMEDIATE',
  STANDARD = 'STANDARD',
  FULL_SIZE = 'FULL_SIZE',
  PREMIUM = 'PREMIUM',
  LUXURY = 'LUXURY',
  SUV = 'SUV',
  MINIVAN = 'MINIVAN',
  CONVERTIBLE = 'CONVERTIBLE',
  SPORTS_CAR = 'SPORTS_CAR',
  TRUCK = 'TRUCK',
  VAN = 'VAN'
}

export enum VehicleStatus {
  AVAILABLE = 'AVAILABLE',
  RENTED = 'RENTED',
  MAINTENANCE = 'MAINTENANCE',
  OUT_OF_SERVICE = 'OUT_OF_SERVICE',
  RETIRED = 'RETIRED'
}

export interface VehicleSearchParams {
  companyId?: number;
  type?: VehicleType;
  make?: string;
  model?: string;
  minRate?: number;
  maxRate?: number;
  minYear?: number;
  maxYear?: number;
  fuelType?: string;
  transmission?: string;
  minSeating?: number;
  maxSeating?: number;
  location?: string;
  airConditioning?: boolean;
  gpsNavigation?: boolean;
  bluetooth?: boolean;
  backupCamera?: boolean;
  sunroof?: boolean;
  leatherSeats?: boolean;
  startDate?: string;
  endDate?: string;
  pickupLocation?: string;
  returnLocation?: string;
}
