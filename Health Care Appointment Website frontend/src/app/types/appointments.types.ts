export type SlotStatus = 'AVAILABLE' | 'BOOKED';

export interface TimeSlotDto {
  startTime: string;  
  endTime: string;    
  status: SlotStatus;
}

export interface DoctorAvailabilityMap {
  [yyyyMmDd: string]: TimeSlotDto[];
}

export interface DoctorSearchResult {
  id: number;
  fullName: string;
  specialization: string;
  location: string;
  rating: number;
  availability: DoctorAvailabilityMap;
}

export interface AppointmentRequestDto {
  doctorId: number;
  appointmentDateTime: string; 
}

export interface AppointmentDetails {
  id: number;
  patientId: number;
  patientName: string;
  doctorId: number;
  doctorName: string;
  appointmentDateTime: string;
  status: 'SCHEDULED' | 'CANCELLED' | 'COMPLETED' | string;
  createdAt: string; 
}

export interface AppointmentBookResponse {
  success: boolean;
  message: string;
  appointmentDetails?: AppointmentDetails;
  waitlistAvailable?: boolean;
}

export interface ApiMessageResponse {
  success: boolean;
  message: string;
}

export interface WaitlistJoinResponse {
  success: boolean;
  message: string;
  data?: {
    waitlistId: number;
    patientId: number;
    patientName: string;
    preferredDate: string; 
    requestedAt: string;   
  };
}