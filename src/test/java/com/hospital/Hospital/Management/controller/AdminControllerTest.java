package com.hospital.Hospital.Management.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.Hospital.Management.dto.AdminUserCreationRequest;
import com.hospital.Hospital.Management.dto.UserProfileResponse;
import com.hospital.Hospital.Management.service.AdminDashboardService;
import com.hospital.Hospital.Management.service.AdminService;

@SpringBootTest
@AutoConfigureMockMvc
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminService adminService;

    @MockBean
    private AdminDashboardService dashboardService;

    private AdminUserCreationRequest doctorRequest;
    private UserProfileResponse doctorResponse;

    @BeforeEach
    void setUp() {
        doctorRequest = new AdminUserCreationRequest();
        doctorRequest.setEmail("doctor@example.com");
        doctorRequest.setPassword("password123");
        doctorRequest.setFullName("Dr. Test");
        doctorRequest.setPhoneNumber("1234567890");
        doctorRequest.setAddress("123 Medical St");

        doctorResponse = UserProfileResponse.builder()
                .id(1L)
                .email(doctorRequest.getEmail())
                .fullName(doctorRequest.getFullName())
                .phoneNumber(doctorRequest.getPhoneNumber())
                .address(doctorRequest.getAddress()).build();
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void createDoctor_Success() throws Exception {
        when(adminService.createDoctor(any(AdminUserCreationRequest.class))).thenReturn(doctorResponse);

        mockMvc.perform(post("/api/admin/doctors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(doctorRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(doctorResponse.getId()))
                .andExpect(jsonPath("$.email").value(doctorResponse.getEmail()))
                .andExpect(jsonPath("$.fullName").value(doctorResponse.getFullName()));
    }

   
   

   
}
