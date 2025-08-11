// Corrected version
package com.hospital.Hospital.Management.model;

public enum AppointmentStatus {
    SCHEDULED,              // Initial state when a patient books
    CONFIRMED_BY_DOCTOR,    // New state: When a doctor accepts/confirms the booking
    COMPLETED,              // When the consultation is over
    CANCELLED_BY_PATIENT,   // When the patient cancels
    CANCELLED_BY_DOCTOR,    // When the doctor declines/cancels
    NO_SHOW                 // If the patient did not show up
}