package com.cognizant.healthcare.doctor_management_service.doctor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController // This combines @Controller and @ResponseBody, creating a RESTful controller
@RequestMapping("/api/doctors") // Base URL for all endpoints in this class
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    // Endpoint to GET all doctors
    // URL: GET http://localhost:8080/api/doctors
    @GetMapping
    public List<Doctor> getAllDoctors() {
        return doctorService.getAllDoctors();
    }

    // Endpoint to GET a single doctor by ID
    // URL: GET http://localhost:8080/api/doctors/1
    @GetMapping("/{id}")
    public ResponseEntity<Doctor> getDoctorById(@PathVariable Long id) {
        return doctorService.getDoctorById(id)
                .map(ResponseEntity::ok) // If found, return 200 OK with doctor object
                .orElse(ResponseEntity.notFound().build()); // If not found, return 404 Not Found
    }

    // Endpoint to CREATE a new doctor
    // URL: POST http://localhost:8080/api/doctors
    @PostMapping
    public Doctor createDoctor(@RequestBody Doctor doctor) {
        return doctorService.createDoctor(doctor);
    }

    // Endpoint to UPDATE a doctor's schedule
    // URL: PUT http://localhost:8080/api/doctors/1/schedule
    @PutMapping("/{id}/schedule")
    public ResponseEntity<Doctor> updateSchedule(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String schedule = payload.get("schedule");
        return doctorService.updateDoctorSchedule(id, schedule)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}