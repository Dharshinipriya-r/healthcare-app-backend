package com.hospital.Hospital.Management.repository;

import com.hospital.Hospital.Management.model.User;
import com.hospital.Hospital.Management.model.WaitlistEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WaitlistRepository extends JpaRepository<WaitlistEntry, Long> {

    List<WaitlistEntry> findByDoctorOrderByCreatedAtAsc(User doctor);

    List<WaitlistEntry> findByDoctorAndPreferredDateOrderByCreatedAtAsc(User doctor, LocalDate preferredDate);
}