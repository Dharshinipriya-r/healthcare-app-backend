package com.cognizant.healthcare.doctor_management_service.doctor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service // Marks this class as a Service component
public class DoctorService {

    @Autowired // This is Dependency Injection. Spring will provide an instance of DoctorRepository.
    private DoctorRepository doctorRepository;

    // Get a list of all doctors
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    // Get a single doctor by their ID
    public Optional<Doctor> getDoctorById(Long id) {
        return doctorRepository.findById(id);
    }

    // Create a new doctor profile
    public Doctor createDoctor(Doctor doctor) {
        // Here you could add validation logic later
        return doctorRepository.save(doctor);
    }

    // Update a doctor's schedule
    public Optional<Doctor> updateDoctorSchedule(Long id, String newSchedule) {
        // Find the existing doctor
        Optional<Doctor> doctorOptional = doctorRepository.findById(id);

        if (doctorOptional.isPresent()) {
            Doctor doctorToUpdate = doctorOptional.get();
            doctorToUpdate.setWeeklySchedule(newSchedule);
            // Save the updated doctor back to the database
            return Optional.of(doctorRepository.save(doctorToUpdate));
        } else {
            // Doctor not found
            return Optional.empty();
        }
    }
}