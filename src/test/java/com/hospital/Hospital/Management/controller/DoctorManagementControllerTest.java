package com.hospital.Hospital.Management.controller;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.Hospital.Management.dto.DoctorProfileDto;
import com.hospital.Hospital.Management.dto.RescheduleRequestDto;
import com.hospital.Hospital.Management.dto.WeeklyAvailabilityRequestDto;
import com.hospital.Hospital.Management.service.DoctorManagementService;

@WebMvcTest(DoctorManagementController.class)
class DoctorManagementControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DoctorManagementService doctorService;

    @InjectMocks
    private DoctorManagementController doctorController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(doctorController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testUpdateProfile() throws Exception {
        DoctorProfileDto profileDto = new DoctorProfileDto();
       
        profileDto.setSpecialization("Cardiology");

        mockMvc.perform(put("/api/doctors/1/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profileDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Profile updated successfully for doctor ID: 1"));
    }

    @Test
    void testSetWeeklyAvailability() throws Exception {
        WeeklyAvailabilityRequestDto requestDto = new WeeklyAvailabilityRequestDto();
       

        when(doctorService.setWeeklyAvailability(eq(1L), any(WeeklyAvailabilityRequestDto.class)));

        mockMvc.perform(put("/api/doctors/1/availability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Availability updated"));
    }

    @Test
    void testRescheduleAppointment() throws Exception {
        RescheduleRequestDto requestDto = new RescheduleRequestDto();
        requestDto.setNewAppointmentDateTime(LocalDateTime.now().plusDays(1));

       

        when(doctorService.rescheduleAppointment(eq(1L), eq(10L), any(LocalDateTime.class)));

        mockMvc.perform(put("/api/doctors/1/appointments/10/reschedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appointmentId").value(10));
    }

    @Test
    void testGetAppointmentHistory() throws Exception {
      
       
        when(doctorService.getAppointmentHistory(eq(1L), eq(null)));

        mockMvc.perform(get("/api/doctors/1/appointments/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].appointmentId").value(5));
    }
}
