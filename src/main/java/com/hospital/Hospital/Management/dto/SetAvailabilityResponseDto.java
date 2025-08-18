package com.hospital.Hospital.Management.dto;

import lombok.Builder;
import lombok.Data;


@Data
@Builder 
public class SetAvailabilityResponseDto {

   
    private Long doctorId;

    
    private String doctorName;

    private String message;

   
    private int slotsCreated;
}