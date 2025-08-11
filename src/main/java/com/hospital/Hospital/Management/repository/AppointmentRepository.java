package com.hospital.Hospital.Management.repository;

import com.hospital.Hospital.Management.model.Appointment;
import com.hospital.Hospital.Management.model.AppointmentStatus;
import com.hospital.Hospital.Management.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Appointment entities.
 * This interface contains all necessary methods for the entire application,
 * including Modules 2, 3, 5, and the Feedback system.
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long>, JpaSpecificationExecutor<Appointment> {

    // --- METHODS FOR CORE APPOINTMENT & DOCTOR MANAGEMENT ---

    List<Appointment> findByPatient(User patient);

    Optional<Appointment> findByDoctorAndAppointmentDateTime(User doctor, LocalDateTime appointmentDateTime);

    List<Appointment> findByDoctorAndStatusIn(User doctor, List<AppointmentStatus> statuses);

    List<Appointment> findByDoctorAndPatientAndStatus(User doctor, User patient, AppointmentStatus status);

    Optional<Appointment> findByIdAndDoctorId(Long appointmentId, Long doctorId);

    // --- METHOD TO PREVENT PATIENT DOUBLE-BOOKING (CORRECTED WITH @Query) ---
    /**
     * Checks if a patient already has an active (SCHEDULED or CONFIRMED) future appointment with a specific doctor.
     * This uses an explicit @Query to avoid Hibernate parsing bugs with complex method names.
     *
     * @param patient The patient making the booking.
     * @param doctor The doctor being booked.
     * @param statuses The list of active statuses to check for.
     * @param afterDateTime The current time, to only check for future appointments.
     * @return True if an active future appointment exists, false otherwise.
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN TRUE ELSE FALSE END " +
            "FROM Appointment a " +
            "WHERE a.patient = :patient " +
            "AND a.doctor = :doctor " +
            "AND a.status IN :statuses " +
            "AND a.appointmentDateTime > :afterDateTime")
    boolean existsByPatientAndDoctorAndStatusInAndAppointmentDateTimeAfter(
            @Param("patient") User patient,
            @Param("doctor") User doctor,
            @Param("statuses") List<AppointmentStatus> statuses,
            @Param("afterDateTime") LocalDateTime afterDateTime);

    // --- METHODS FOR ADVANCED QUERIES & REMINDERS ---

    List<Appointment> findByPatientAndStatusInAndAppointmentDateTimeBetween(
            User patient, List<AppointmentStatus> statuses, LocalDateTime start, LocalDateTime end);

    List<Appointment> findAllByStatusInAndAppointmentDateTimeBetween(
            List<AppointmentStatus> statuses, LocalDateTime start, LocalDateTime end);

    List<Appointment> findByAppointmentDateTimeBetweenAndStatus(
            LocalDateTime startDateTime, LocalDateTime endDateTime, AppointmentStatus status);

    // --- METHOD FOR FEEDBACK/OTHER MODULE INTEGRATION ---

    List<Appointment> findByPatientAndDoctor(User patient, User doctor);
}