package com.hospital.Hospital.Management.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

/**
 * A flexible DTO to handle responses from the booking endpoint.
 * It can represent a successful booking, an unavailable slot, or a waitlist offer.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // This hides null fields in the JSON response
public class BookingResponseDto {

    private boolean success;
    private String message;

    // Fields for a SUCCESSFUL booking
    private AppointmentResponseDto appointmentDetails;

    // Fields for a FAILED booking (waitlist offer)
    private Boolean waitlistAvailable;
}