package com.hospital.Hospital.Management.repository;

import com.hospital.Hospital.Management.model.ConsultationNote;
import com.hospital.Hospital.Management.model.Appointment; // Make sure this import is present
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional; // Make sure this import is present

@Repository
public interface ConsultationNoteRepository extends JpaRepository<ConsultationNote, Long> {

    /**
     * Checks if a consultation note exists for a given appointment ID.
     * This is used to prevent duplicate notes from being created for the same appointment.
     *
     * @param appointmentId The ID of the appointment to check.
     * @return true if a note exists for the appointment, false otherwise.
     */
    boolean existsByAppointmentId(Long appointmentId);


    /**
     * Finds a ConsultationNote entity based on its associated Appointment entity.
     * This method is crucial for retrieving the notes for a specific completed appointment
     * when building the doctor's appointment history.
     *
     * @param appointment The appointment entity to search by.
     * @return An Optional containing the ConsultationNote if found, otherwise an empty Optional.
     */
    Optional<ConsultationNote> findByAppointment(Appointment appointment);

}