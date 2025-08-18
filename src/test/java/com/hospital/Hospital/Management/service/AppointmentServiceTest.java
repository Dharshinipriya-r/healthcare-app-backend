package com.hospital.Hospital.Management.service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hospital.Hospital.Management.dto.AppointmentRequestDto;
import com.hospital.Hospital.Management.dto.AppointmentResponseDto;
import com.hospital.Hospital.Management.dto.BookingResponseDto;
import com.hospital.Hospital.Management.model.Appointment;
import com.hospital.Hospital.Management.model.AppointmentStatus;
import com.hospital.Hospital.Management.model.DayOfWeek;
import com.hospital.Hospital.Management.model.DoctorAvailability;
import com.hospital.Hospital.Management.model.User;
import com.hospital.Hospital.Management.repository.AppointmentRepository;
import com.hospital.Hospital.Management.repository.DoctorAvailabilityRepository;
import com.hospital.Hospital.Management.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock private AppointmentRepository appointmentRepository;
    @Mock private UserRepository userRepository;
    @Mock private DoctorAvailabilityRepository availabilityRepository;
    @Mock private EmailService emailService;

    @InjectMocks
    private AppointmentService appointmentService;

    private User patient;
    private User doctor;
    private Appointment appointment;

    @BeforeEach
    void setUp() {
        patient = new User();
        patient.setId(1L);
        patient.setEmail("patient@example.com");
        patient.setFullName("Patient One");

        doctor = new User();
        doctor.setId(2L);
        doctor.setEmail("doctor@example.com");
        doctor.setFullName("Doctor Strange");

        appointment = Appointment.builder()
                .id(100L)
                .patient(patient)
                .doctor(doctor)
                .appointmentDateTime(LocalDateTime.now().plusDays(1))
                .status(AppointmentStatus.SCHEDULED)
                .build();
    }

    @Test
    void testBookAppointment_Success() {
        AppointmentRequestDto requestDto = new AppointmentRequestDto();
        requestDto.setDoctorId(doctor.getId());
        requestDto.setAppointmentDateTime(LocalDateTime.now().plusDays(1));

        when(userRepository.findByEmail(patient.getEmail())).thenReturn(Optional.of(patient));
        when(userRepository.findById(doctor.getId())).thenReturn(Optional.of(doctor));
        when(appointmentRepository.findByDoctorAndAppointmentDateTime(doctor, requestDto.getAppointmentDateTime()))
                .thenReturn(Optional.empty());
        when(availabilityRepository.findByDoctorId(doctor.getId()))
                .thenReturn(List.of(new DoctorAvailability(doctor, DayOfWeek.MONDAY, requestDto.getAppointmentDateTime().toLocalTime().minusHours(1), requestDto.getAppointmentDateTime().toLocalTime().plusHours(1))));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        BookingResponseDto response = appointmentService.bookAppointment(requestDto, patient.getEmail());

        assertTrue(response.isSuccess());
        assertEquals("Appointment booked successfully!", response.getMessage());
        verify(appointmentRepository, times(1)).save(any(Appointment.class));
    }

    @Test
    void testBookAppointment_SlotAlreadyBooked() {
        AppointmentRequestDto requestDto = new AppointmentRequestDto();
        requestDto.setDoctorId(doctor.getId());
        requestDto.setAppointmentDateTime(appointment.getAppointmentDateTime());

        when(userRepository.findByEmail(patient.getEmail())).thenReturn(Optional.of(patient));
        when(userRepository.findById(doctor.getId())).thenReturn(Optional.of(doctor));
        when(appointmentRepository.findByDoctorAndAppointmentDateTime(doctor, requestDto.getAppointmentDateTime()))
                .thenReturn(Optional.of(appointment));

        BookingResponseDto response = appointmentService.bookAppointment(requestDto, patient.getEmail());

        assertFalse(response.isSuccess());
        assertTrue(response.isWaitlistAvailable());
    }

    @Test
    void testCancelAppointment_Unauthorized() {
        when(appointmentRepository.findById(appointment.getId())).thenReturn(Optional.of(appointment));
        when(userRepository.findByEmail("another@example.com")).thenReturn(Optional.of(new User()));

       
    }

    @Test
    void testRescheduleAppointment_Success() throws Exception {
        LocalDateTime newDateTime = LocalDateTime.now().plusDays(2);

        when(appointmentRepository.findById(appointment.getId())).thenReturn(Optional.of(appointment));
        when(userRepository.findByEmail(patient.getEmail())).thenReturn(Optional.of(patient));
        when(appointmentRepository.findByDoctorAndAppointmentDateTime(doctor, newDateTime))
                .thenReturn(Optional.empty());
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        AppointmentResponseDto response = appointmentService.rescheduleAppointment(appointment.getId(), patient.getEmail(), newDateTime);

        assertEquals(appointment.getId(), response.getId());
        verify(emailService, times(1)).sendAppointmentRescheduleByPatientEmail(any(), any());
    }
}
