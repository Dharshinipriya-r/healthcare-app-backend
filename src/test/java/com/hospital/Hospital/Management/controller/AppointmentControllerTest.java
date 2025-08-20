package com.hospital.Hospital.Management.controller;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.Hospital.Management.dto.AppointmentRequestDto;
import com.hospital.Hospital.Management.dto.AppointmentResponseDto;
import com.hospital.Hospital.Management.service.AppointmentService;

@SpringBootTest
@AutoConfigureMockMvc
public class AppointmentControllerTest {


    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AppointmentService appointmentService;

    private AppointmentRequestDto appointmentRequest;
    private AppointmentResponseDto appointmentResponse;

    @BeforeEach
    void setUp() {
        LocalDateTime appointmentTime = LocalDateTime.now().plusDays(1);
        
        appointmentRequest = new AppointmentRequestDto();
        appointmentRequest.setDoctorId(1L);
        appointmentRequest.setAppointmentDateTime(appointmentTime);
        

        appointmentResponse = AppointmentResponseDto.builder()
                .id(1L)
                .doctorId(1L)
                .doctorName("Dr. Test")
                .patientName("Test Patient")
                .build();
    }

}
