package com.hospital.Hospital.Management.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.hospital.Hospital.Management.model.Appointment;
import com.hospital.Hospital.Management.model.AppointmentStatus;
import com.hospital.Hospital.Management.model.Role;
import com.hospital.Hospital.Management.model.User;
import com.hospital.Hospital.Management.repository.AppointmentRepository;

@DataJpaTest
@DisplayName("AppointmentRepository Integration Tests")
class AppointmentRepositoryIntegrationTest {

    @Autowired private TestEntityManager entityManager;
    @Autowired private AppointmentRepository appointmentRepository;
    private User patient;
    private User doctor;

    @BeforeEach
    void setUp() {
        patient = User.builder().email("patient.db@test.com").password("pass").fullName("DB Patient").roles(Set.of(Role.ROLE_PATIENT)).enabled(true).build();
        doctor = User.builder().email("doctor.db@test.com").password("pass").fullName("DB Doctor").roles(Set.of(Role.ROLE_DOCTOR)).enabled(true).build();
        entityManager.persist(patient);
        entityManager.persist(doctor);
    }

    @Test
    @DisplayName("Should save and find an appointment by patient")
    void whenFindByPatient_thenReturnAppointmentList() {
        Appointment appointment = Appointment.builder().patient(patient).doctor(doctor).appointmentDateTime(LocalDateTime.now().plusDays(1)).status(AppointmentStatus.SCHEDULED).build();
        entityManager.persistAndFlush(appointment);
        List<Appointment> foundAppointments = appointmentRepository.findByPatient(patient);
        assertThat(foundAppointments).hasSize(1).first().isEqualTo(appointment);
    }
}