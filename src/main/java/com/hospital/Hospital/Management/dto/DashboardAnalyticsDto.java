package com.hospital.Hospital.Management.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardAnalyticsDto {

    private long totalUsers;
    private long totalDoctors;
    private long totalPatients;
    private long totalAppointments;
    private long scheduledAppointments;
    private long completedAppointments;
    private long canceledAppointments;
}