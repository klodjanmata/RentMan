export interface Reservation {
  id: number;
  reservationNumber: string;
  vehicle: {
    id: number;
    make: string;
    model: string;
    year: number;
    type: string;
    dailyRate: number;
    imageUrls: string[];
  };
  customer: {
    id: number;
    firstName: string;
    lastName: string;
    email: string;
    phoneNumber: string;
  };
  company: {
    id: number;
    companyName: string;
    email: string;
    phoneNumber: string;
  };
  startDate: string;
  endDate: string;
  pickupDate: string;
  returnDate: string;
  pickupLocation: string;
  returnLocation: string;
  totalDays: number;
  dailyRate: number;
  totalAmount: number;
  status: ReservationStatus;
  insuranceIncluded: boolean;
  insuranceDailyRate: number;
  depositRequired: number;
  depositPaid: number;
  notes: string;
  createdAt: string;
  updatedAt: string;
}

export enum ReservationStatus {
  PENDING = 'PENDING',
  CONFIRMED = 'CONFIRMED',
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED',
  NO_SHOW = 'NO_SHOW'
}

export interface ReservationCreateRequest {
  vehicleId: number;
  startDate: string;
  endDate: string;
  pickupLocation: string;
  returnLocation: string;
  insuranceIncluded: boolean;
  notes?: string;
}
