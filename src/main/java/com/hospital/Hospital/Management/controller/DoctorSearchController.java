package com.hospital.Hospital.Management.controller;

import com.hospital.Hospital.Management.dto.ApiResponse;
import com.hospital.Hospital.Management.dto.DoctorSearchResultDto;
import com.hospital.Hospital.Management.model.User;
import com.hospital.Hospital.Management.repository.UserRepository;
import com.hospital.Hospital.Management.service.DoctorSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/doctors")
public class DoctorSearchController {

    private final DoctorSearchService doctorSearchService;
    private final UserRepository userRepository;

    public DoctorSearchController(DoctorSearchService doctorSearchService, UserRepository userRepository) {
        this.doctorSearchService = doctorSearchService;
        this.userRepository = userRepository;
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> searchDoctors(
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) String location,
            @RequestParam(required = false, defaultValue = "0.0") double minRating) {

        List<DoctorSearchResultDto> doctors = doctorSearchService.findDoctorsByCriteria(specialization, location, minRating);

        if (doctors.isEmpty()) {
            return ResponseEntity.ok(new ApiResponse(true, "No doctors are available based on your criteria.", null));
        }

        return ResponseEntity.ok(doctors);
    }

    @GetMapping("/debug/all-profiles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllDoctorProfilesForDebug() {
        List<User> allUsers = userRepository.findAll();
        List<User> doctorsOnly = allUsers.stream()
                .filter(user -> user.getRoles().stream()
                        .anyMatch(role -> role.name().equals("ROLE_DOCTOR")))
                .collect(Collectors.toList());
        return ResponseEntity.ok(doctorsOnly);
    }
}