package com.hospital.Hospital.Management.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // This hides null fields in the JSON response
public class BookingResponseDto {

    private boolean success;
    private String message;

    
    private AppointmentResponseDto appointmentDetails;

    
    private Boolean waitlistAvailable;
}