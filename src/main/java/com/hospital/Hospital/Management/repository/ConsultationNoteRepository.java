package com.hospital.Hospital.Management.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository; 
import org.springframework.stereotype.Repository;

import com.hospital.Hospital.Management.model.Appointment;
import com.hospital.Hospital.Management.model.ConsultationNote;

@Repository
public interface ConsultationNoteRepository extends JpaRepository<ConsultationNote, Long> {

    
    boolean existsByAppointmentId(Long appointmentId);


   
    Optional<ConsultationNote> findByAppointment(Appointment appointment);

}