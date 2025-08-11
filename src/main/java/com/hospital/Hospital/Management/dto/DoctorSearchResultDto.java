package com.hospital.Hospital.Management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorSearchResultDto {
    private Long id;
    private String fullName;
    private String specialization;
    private String location;
    private Double rating;
    private Map<LocalDate, List<TimeSlotDto>> availability;
}