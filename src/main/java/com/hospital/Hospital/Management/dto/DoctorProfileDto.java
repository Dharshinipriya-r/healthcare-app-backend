package com.hospital.Hospital.Management.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DoctorProfileDto {

    @NotBlank(message = "Specialization cannot be blank")
    private String specialization;

    @NotBlank(message = "Location cannot be blank")
    private String location;
}