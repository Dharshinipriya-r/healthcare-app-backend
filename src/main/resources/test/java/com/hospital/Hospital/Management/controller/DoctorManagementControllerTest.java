// src/test/java/com/hospital/Hospital/Management/doctor_management_module_3/controller/DoctorManagementControllerTest.java
package com.hospital.Hospital.Management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.Hospital.Management.dto.UpcomingAppointmentDto;
import com.hospital.Hospital.Management.service.DoctorManagementService;
import com.hospital.Hospital.Management.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DoctorManagementController.class)
// Import SecurityConfig to apply security rules in tests
@Import(com.hospital.Hospital.Management.config.SecurityConfig.class)
class DoctorManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DoctorManagementService doctorService;

    // Mock UserRepository and other beans required by SecurityConfig
    @MockBean private com.hospital.Hospital.Management.service.JwtService jwtService;
    @MockBean private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    @MockBean private UserRepository userRepository;


    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "doctor@test.com", roles = {"DOCTOR"})
    void getUpcomingAppointments_whenAuthenticated_shouldReturnOk() throws Exception {
        // Given
        UpcomingAppointmentDto appointmentDto = UpcomingAppointmentDto.builder()
                .appointmentId(1L).patientName("Test Patient").appointmentDateTime(LocalDateTime.now()).status("SCHEDULED").build();
        given(doctorService.getUpcomingAppointments(1L)).willReturn(List.of(appointmentDto));

        // Mock the user lookup for @PreAuthorize
        given(userRepository.findByEmail("doctor@test.com")).willReturn(java.util.Optional.of(
                com.hospital.Hospital.Management.model.User.builder().id(1L).build()
        ));


        // When & Then
        mockMvc.perform(get("/api/doctors/1/appointments/upcoming")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].patientName").value("Test Patient"));
    }
}