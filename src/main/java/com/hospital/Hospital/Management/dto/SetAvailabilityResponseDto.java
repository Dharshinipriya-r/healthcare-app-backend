package com.hospital.Hospital.Management.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Data Transfer Object (DTO) for the response when a doctor sets their availability.
 * This provides a clear, structured confirmation message back to the client.
 */
@Data
@Builder // The builder pattern makes it easy to construct this object in the service layer.
public class SetAvailabilityResponseDto {

    /**
     * The unique ID of the doctor whose availability was set.
     */
    private Long doctorId;

    /**
     * The full name of the doctor.
     */
    private String doctorName;

    /**
     * A user-friendly success message.
     */
    private String message;

    /**
     * The total number of availability time slots that were created in the database.
     */
    private int slotsCreated;
}