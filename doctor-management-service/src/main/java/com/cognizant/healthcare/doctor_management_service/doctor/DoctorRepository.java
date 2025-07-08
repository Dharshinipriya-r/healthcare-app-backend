package com.cognizant.healthcare.doctor_management_service.doctor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository // Marks this as a Spring Data repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    // That's it!
}