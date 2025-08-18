package com.hospital.Hospital.Management.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hospital.Hospital.Management.model.Appointment;
import com.hospital.Hospital.Management.model.AppointmentStatus;
import com.hospital.Hospital.Management.model.User;


@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long>, JpaSpecificationExecutor<Appointment> {


    List<Appointment> findByPatient(User patient);

    Optional<Appointment> findByDoctorAndAppointmentDateTime(User doctor, LocalDateTime appointmentDateTime);

    List<Appointment> findByDoctorAndStatusIn(User doctor, List<AppointmentStatus> statuses);

    List<Appointment> findByDoctorAndPatientAndStatus(User doctor, User patient, AppointmentStatus status);

    Optional<Appointment> findByIdAndDoctorId(Long appointmentId, Long doctorId);

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

  
    List<Appointment> findByPatientAndStatusInAndAppointmentDateTimeBetween(
            User patient, List<AppointmentStatus> statuses, LocalDateTime start, LocalDateTime end);

    List<Appointment> findAllByStatusInAndAppointmentDateTimeBetween(
            List<AppointmentStatus> statuses, LocalDateTime start, LocalDateTime end);

    List<Appointment> findByAppointmentDateTimeBetweenAndStatus(
            LocalDateTime startDateTime, LocalDateTime endDateTime, AppointmentStatus status);

    

    List<Appointment> findByPatientAndDoctor(User patient, User doctor);
}