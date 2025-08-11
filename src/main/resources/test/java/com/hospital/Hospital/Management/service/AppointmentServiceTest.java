package com.hospital.Hospital.Management.service;

import com.hospital.Hospital.Management.dto.AppointmentRequestDto;
import com.hospital.Hospital.Management.dto.AppointmentResponseDto;
import com.hospital.Hospital.Management.model.*;
import com.hospital.Hospital.Management.repository.AppointmentRepository;
import com.hospital.Hospital.Management.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AppointmentService Unit Tests")
class AppointmentServiceTest {

    @Mock private AppointmentRepository appointmentRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private AppointmentService appointmentService;

    private User patient, anotherPatient, doctor;
    private Appointment appointment;

    @BeforeEach
    void setUp() {
        patient = User.builder().id(1L).email("patient@test.com").roles(Set.of(Role.ROLE_PATIENT)).build();
        anotherPatient = User.builder().id(3L).email("other.patient@test.com").roles(Set.of(Role.ROLE_PATIENT)).build();
        doctor = User.builder().id(2L).email("doctor@test.com").roles(Set.of(Role.ROLE_DOCTOR)).build();
        appointment = Appointment.builder().id(100L).patient(patient).doctor(doctor).appointmentDateTime(LocalDateTime.now().plusDays(3)).status(AppointmentStatus.SCHEDULED).build();
    }

    @Test
    @DisplayName("Should successfully book an appointment")
    void bookAppointment_Success() {
        // ... test code from previous guide ...
        AppointmentRequestDto requestDto = new AppointmentRequestDto();
        requestDto.setDoctorId(doctor.getId());
        requestDto.setAppointmentDateTime(LocalDateTime.now().plusDays(5));
        when(userRepository.findByEmail(patient.getEmail())).thenReturn(Optional.of(patient));
        when(userRepository.findById(doctor.getId())).thenReturn(Optional.of(doctor));
        when(appointmentRepository.findByDoctorAndAppointmentDateTime(any(), any())).thenReturn(Optional.empty());
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
        AppointmentResponseDto response = appointmentService.bookAppointment(requestDto, patient.getEmail());
        assertNotNull(response);
        assertEquals(doctor.getId(), response.getDoctorId());
        verify(appointmentRepository, times(1)).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Patient should successfully cancel their own appointment")
    void cancelAppointment_Success_ByPatient() throws AccessDeniedException {
        // ... test code from previous guide ...
        when(appointmentRepository.findById(appointment.getId())).thenReturn(Optional.of(appointment));
        when(userRepository.findByEmail(patient.getEmail())).thenReturn(Optional.of(patient));
        appointmentService.cancelAppointment(appointment.getId(), patient.getEmail());
        assertEquals(AppointmentStatus.CANCELLED_BY_PATIENT, appointment.getStatus());
        verify(appointmentRepository, times(1)).save(appointment);
    }

    @Test
    @DisplayName("Should fail to cancel appointment due to lack of authorization")
    void cancelAppointment_Failure_AccessDenied() {
        // ... test code from previous guide ...
        when(appointmentRepository.findById(appointment.getId())).thenReturn(Optional.of(appointment));
        when(userRepository.findByEmail(anotherPatient.getEmail())).thenReturn(Optional.of(anotherPatient));
        assertThrows(AccessDeniedException.class, () -> appointmentService.cancelAppointment(appointment.getId(), anotherPatient.getEmail()));
    }
}