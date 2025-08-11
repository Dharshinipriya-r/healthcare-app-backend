// src/test/java/com/hospital/Hospital/Management/doctor_management_module_3/service/DoctorManagementServiceTest.java
package com.hospital.Hospital.Management.service;

import com.hospital.Hospital.Management.dto.UpcomingAppointmentDto;
import com.hospital.Hospital.Management.model.*;
import com.hospital.Hospital.Management.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorManagementServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private DoctorAvailabilityRepository availabilityRepository;
    @Mock
    private ConsultationNoteRepository noteRepository;

    @InjectMocks
    private DoctorManagementService doctorService;

    private User doctor;
    private User patient;

    @BeforeEach
    void setUp() {
        doctor = User.builder().id(1L).fullName("Dr. Strange").email("strange@test.com").roles(Set.of(Role.ROLE_DOCTOR)).build();
        patient = User.builder().id(2L).fullName("Patient Zero").email("patient@test.com").roles(Set.of(Role.ROLE_PATIENT)).build();
    }

    @Test
    void getUpcomingAppointments_shouldReturnUpcomingAppointments() {
        // Given
        Appointment appointment1 = Appointment.builder().id(101L).doctor(doctor).patient(patient)
                .appointmentDateTime(LocalDateTime.now().plusDays(1)).status(AppointmentStatus.SCHEDULED).build();
        List<Appointment> appointments = Collections.singletonList(appointment1);
        List<AppointmentStatus> upcomingStatuses = List.of(AppointmentStatus.SCHEDULED, AppointmentStatus.CONFIRMED_BY_DOCTOR);

        when(userRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.findByDoctorAndStatusIn(doctor, upcomingStatuses)).thenReturn(appointments);

        // When
        List<UpcomingAppointmentDto> result = doctorService.getUpcomingAppointments(1L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAppointmentId()).isEqualTo(101L);
        assertThat(result.get(0).getPatientName()).isEqualTo("Patient Zero");
        verify(appointmentRepository, times(1)).findByDoctorAndStatusIn(any(User.class), anyList());
    }
}