package com.hospital.Hospital.Management.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hospital.Hospital.Management.model.DoctorAvailability;

@Repository
public interface DoctorAvailabilityRepository extends JpaRepository<DoctorAvailability, Long> {
    void deleteByDoctorId(Long doctorId);
    List<DoctorAvailability> findByDoctorId(Long doctorId);
}