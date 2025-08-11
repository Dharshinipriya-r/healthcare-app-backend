// src/main/java/com/hospital/Hospital/Management/doctor_management_module_3/repository/DoctorAvailabilityRepository.java
package com.hospital.Hospital.Management.repository;

import com.hospital.Hospital.Management.model.DoctorAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DoctorAvailabilityRepository extends JpaRepository<DoctorAvailability, Long> {
    void deleteByDoctorId(Long doctorId);
    List<DoctorAvailability> findByDoctorId(Long doctorId);
}