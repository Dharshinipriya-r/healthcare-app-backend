package com.hospital.Hospital.Management.service;

import com.hospital.Hospital.Management.dto.*;
import com.hospital.Hospital.Management.model.ConsultationNote;
import com.hospital.Hospital.Management.model.DoctorAvailability;
import com.hospital.Hospital.Management.repository.ConsultationNoteRepository;
import com.hospital.Hospital.Management.repository.DoctorAvailabilityRepository;
import com.hospital.Hospital.Management.dto.ApiResponse;
import com.hospital.Hospital.Management.exception.ResourceNotFoundException;
import com.hospital.Hospital.Management.exception.SlotUnavailableException;
import com.hospital.Hospital.Management.model.*;
import com.hospital.Hospital.Management.repository.AppointmentRepository;
import com.hospital.Hospital.Management.repository.UserRepository;
import com.hospital.Hospital.Management.repository.WaitlistRepository; // Import WaitlistRepository
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DoctorManagementService {

    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorAvailabilityRepository availabilityRepository;
    private final ConsultationNoteRepository noteRepository;
    private final EmailService emailService;

    public DoctorManagementService(UserRepository userRepository, AppointmentRepository appointmentRepository,
                                   DoctorAvailabilityRepository availabilityRepository, ConsultationNoteRepository noteRepository,
                                   EmailService emailService) {
        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
        this.availabilityRepository = availabilityRepository;
        this.noteRepository = noteRepository;
        this.emailService = emailService;
    }

    @Transactional
    public User updateDoctorProfile(Long doctorId, DoctorProfileDto profileDto) {
        log.info("Updating profile for doctor ID: {}", doctorId);
        User doctor = findDoctorById(doctorId);
        doctor.setSpecialization(profileDto.getSpecialization());
        doctor.setLocation(profileDto.getLocation());
        return userRepository.save(doctor);
    }

    @Transactional
    public SetAvailabilityResponseDto setWeeklyAvailability(Long doctorId, WeeklyAvailabilityRequestDto request) {
        log.info("Setting weekly availability for doctor ID: {}", doctorId);
        User doctor = findDoctorById(doctorId);

        // Save slot duration
        doctor.setSlotDurationInMinutes(request.getSlotDurationInMinutes());
        userRepository.save(doctor);

        if (request.getAvailability() == null || request.getAvailability().isEmpty()) {
            availabilityRepository.deleteByDoctorId(doctorId);
            return SetAvailabilityResponseDto.builder()
                    .doctorId(doctor.getId())
                    .doctorName(doctor.getFullName())
                    .message("All availability slots have been cleared.")
                    .slotsCreated(0)
                    .build();
        }

        availabilityRepository.deleteByDoctorId(doctorId);

        List<DoctorAvailability> newAvailabilities = request.getAvailability().stream()
                .map(dto -> DoctorAvailability.builder()
                        .doctor(doctor)
                        .dayOfWeek(dto.getDayOfWeek())
                        .startTime(dto.getStartTime())
                        .endTime(dto.getEndTime())
                        .build())
                .collect(Collectors.toList());

        if (!newAvailabilities.isEmpty()) {
            availabilityRepository.saveAll(newAvailabilities);
        }

        int slotsCreated = newAvailabilities.size();
        log.info("Successfully set {} availability rules for doctor ID: {}", slotsCreated, doctorId);

        String message = slotsCreated > 0
                ? "Availability successfully set for Dr. " + doctor.getFullName()
                : "All existing availability slots have been cleared.";

        return SetAvailabilityResponseDto.builder()
                .doctorId(doctor.getId())
                .doctorName(doctor.getFullName())
                .message(message)
                .slotsCreated(slotsCreated)
                .build();
    }

    // ... other existing methods ...
    // The rest of this service file remains the same. The code below is already present in your file.

    @Transactional(readOnly = true)
    public ResponseEntity<?> getUpcomingAppointmentsForDoctor(Long doctorId) {
        log.info("Fetching upcoming appointments for doctor ID: {}", doctorId);
        User doctor = findDoctorById(doctorId);
        List<AppointmentStatus> upcomingStatuses = List.of(AppointmentStatus.SCHEDULED, AppointmentStatus.CONFIRMED_BY_DOCTOR);

        List<UpcomingAppointmentDto> appointments = appointmentRepository.findByDoctorAndStatusIn(doctor, upcomingStatuses).stream()
                .map(this::mapToUpcomingAppointmentDto)
                .collect(Collectors.toList());

        if (appointments.isEmpty()) {
            ApiResponse<Void> response = ApiResponse.<Void>builder()
                    .success(true)
                    .message("You have no upcoming appointments scheduled.")
                    .build();
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.ok(appointments);
    }

    @Transactional
    public AppointmentActionResponseDto updateAppointmentStatus(Long doctorId, Long appointmentId, AppointmentStatus newStatus) {
        log.info("Doctor ID: {} attempting to update appointment ID: {} to status: {}", doctorId, appointmentId, newStatus);

        Appointment appointment = findAppointmentByIdAndDoctorId(appointmentId, doctorId);

        List<AppointmentStatus> finalStatuses = List.of(
                AppointmentStatus.COMPLETED,
                AppointmentStatus.CANCELLED_BY_DOCTOR,
                AppointmentStatus.CANCELLED_BY_PATIENT
        );

        if (finalStatuses.contains(appointment.getStatus())) {
            log.warn("Action blocked: Attempted to change status of a finalized appointment {}. Current status: {}",
                    appointmentId, appointment.getStatus());
            throw new IllegalStateException("This appointment is already finalized (Completed or Cancelled) and cannot be modified.");
        }

        if (newStatus == AppointmentStatus.CANCELLED_BY_DOCTOR && appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            log.warn("Action blocked: Doctor {} attempted to decline an appointment that was not in SCHEDULED state.", doctorId);
            throw new IllegalStateException("Can only decline an appointment that is in SCHEDULED state.");
        }

        appointment.setStatus(newStatus);
        Appointment updatedAppointment = appointmentRepository.save(appointment);
        log.info("Appointment ID: {} status updated to {}", appointmentId, newStatus);

        try {
            if (newStatus == AppointmentStatus.CONFIRMED_BY_DOCTOR) {
                emailService.sendAppointmentConfirmationEmail(updatedAppointment);
            } else if (newStatus == AppointmentStatus.CANCELLED_BY_DOCTOR) {
                emailService.sendAppointmentCancellationEmail(updatedAppointment);
            }
        } catch (Exception e) {
            log.error("Failed to send notification for appointment {} after status change to {}. Error: {}",
                    appointmentId, newStatus, e.getMessage());
        }

        return AppointmentActionResponseDto.builder()
                .appointmentId(updatedAppointment.getId())
                .doctorId(updatedAppointment.getDoctor().getId())
                .doctorName(updatedAppointment.getDoctor().getFullName())
                .patientId(updatedAppointment.getPatient().getId())
                .patientName(updatedAppointment.getPatient().getFullName())
                .newStatus(updatedAppointment.getStatus().name())
                .message(generateResponseMessage(updatedAppointment))
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Transactional
    public AppointmentResponseDto rescheduleAppointment(Long doctorId, Long appointmentId, LocalDateTime newDateTime) {
        log.info("Doctor {} attempting to reschedule appointment {} to {}", doctorId, appointmentId, newDateTime);
        Appointment appointment = findAppointmentByIdAndDoctorId(appointmentId, doctorId);

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            log.warn("Action blocked: Doctor {} attempted to reschedule a completed appointment.", doctorId);
            throw new IllegalStateException("Cannot reschedule a completed appointment.");
        }

        User doctor = appointment.getDoctor();
        LocalDateTime oldDateTime = appointment.getAppointmentDateTime();

        appointmentRepository.findByDoctorAndAppointmentDateTime(doctor, newDateTime).ifPresent(existing -> {
            if (!existing.getId().equals(appointmentId)) {
                throw new SlotUnavailableException("The proposed new time slot is already booked.");
            }
        });

        DayOfWeek requestedDay = DayOfWeek.valueOf(newDateTime.getDayOfWeek().name());
        boolean isAvailable = availabilityRepository.findByDoctorId(doctor.getId()).stream()
                .anyMatch(avail -> avail.getDayOfWeek() == requestedDay &&
                        !newDateTime.toLocalTime().isBefore(avail.getStartTime()) &&
                        newDateTime.toLocalTime().isBefore(avail.getEndTime()));

        if (!isAvailable) {
            throw new SlotUnavailableException("The proposed new time is outside of your set working hours.");
        }

        appointment.setAppointmentDateTime(newDateTime);
        Appointment savedAppointment = appointmentRepository.save(appointment);

        try {
            emailService.sendAppointmentRescheduleEmail(savedAppointment, oldDateTime);
        } catch (Exception e) {
            log.error("Failed to send reschedule notification for appointment {}", appointmentId, e);
        }

        return mapToAppointmentResponseDto(savedAppointment);
    }

    @Transactional(readOnly = true)
    public List<AppointmentHistoryDto> getAppointmentHistory(Long doctorId, Long patientId) {
        log.info("Fetching appointment history for doctor ID: {}. Optional patient filter ID: {}", doctorId, patientId);
        User doctor = findDoctorById(doctorId);
        List<Appointment> completedAppointments;
        if (patientId != null) {
            User patient = userRepository.findById(patientId)
                    .orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID: " + patientId));
            completedAppointments = appointmentRepository.findByDoctorAndPatientAndStatus(doctor, patient, AppointmentStatus.COMPLETED);
        } else {
            completedAppointments = appointmentRepository.findByDoctorAndStatusIn(doctor, List.of(AppointmentStatus.COMPLETED));
        }
        return completedAppointments.stream()
                .map(this::mapToAppointmentHistoryDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public AddNoteResponseDto addConsultationNote(Long doctorId, Long appointmentId, ConsultationNoteDto noteDto) {
        log.info("Doctor ID: {} adding consultation note for appointment ID: {}", doctorId, appointmentId);
        Appointment appointment = findAppointmentByIdAndDoctorId(appointmentId, doctorId);
        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new IllegalStateException("Consultation notes can only be added to completed appointments.");
        }
        if (noteRepository.existsByAppointmentId(appointmentId)) {
            throw new IllegalStateException("A consultation note already exists for this appointment.");
        }
        ConsultationNote note = ConsultationNote.builder()
                .appointment(appointment)
                .diagnosis(noteDto.getDiagnosis())
                .prescription(noteDto.getPrescription())
                .treatmentDetails(noteDto.getTreatmentDetails())
                .remarks(noteDto.getRemarks())
                .build();
        ConsultationNote savedNote = noteRepository.save(note);
        log.info("Consultation note saved for appointment ID: {}", appointmentId);
        return AddNoteResponseDto.builder()
                .noteId(savedNote.getId())
                .appointmentId(appointment.getId())
                .message("Consultation note added successfully.")
                .doctorId(doctorId)
                .doctorName(appointment.getDoctor().getFullName())
                .patientId(appointment.getPatient().getId())
                .patientName(appointment.getPatient().getFullName())
                .noteDetails(noteDto)
                .build();
    }

    private String generateResponseMessage(Appointment appointment) {
        String doctorName = appointment.getDoctor().getFullName();
        String patientName = appointment.getPatient().getFullName();
        switch (appointment.getStatus()) {
            case CONFIRMED_BY_DOCTOR:
                return "Dr. " + doctorName + ", you have successfully confirmed the appointment for " + patientName + ".";
            case CANCELLED_BY_DOCTOR:
                return "Dr. " + doctorName + ", you have declined the appointment for " + patientName + ".";
            case COMPLETED:
                return "The appointment with " + patientName + " has been marked as completed.";
            default:
                return "Appointment status updated to " + appointment.getStatus().name() + ".";
        }
    }

    private User findDoctorById(Long doctorId) {
        return userRepository.findById(doctorId)
                .filter(user -> user.getRoles().contains(Role.ROLE_DOCTOR))
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + doctorId));
    }

    private Appointment findAppointmentByIdAndDoctorId(Long appointmentId, Long doctorId) {
        return appointmentRepository.findByIdAndDoctorId(appointmentId, doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + appointmentId + " for this doctor."));
    }

    private UpcomingAppointmentDto mapToUpcomingAppointmentDto(Appointment appointment) {
        return UpcomingAppointmentDto.builder()
                .appointmentId(appointment.getId())
                .patientName(appointment.getPatient().getFullName())
                .appointmentDateTime(appointment.getAppointmentDateTime())
                .status(appointment.getStatus().name())
                .build();
    }

    private AppointmentResponseDto mapToAppointmentResponseDto(Appointment appointment) {
        return AppointmentResponseDto.builder()
                .id(appointment.getId())
                .patientId(appointment.getPatient().getId())
                .patientName(appointment.getPatient().getFullName())
                .doctorId(appointment.getDoctor().getId())
                .doctorName(appointment.getDoctor().getFullName())
                .appointmentDateTime(appointment.getAppointmentDateTime())
                .status(appointment.getStatus().name())
                .createdAt(appointment.getCreatedAt())
                .build();
    }

    private AppointmentHistoryDto mapToAppointmentHistoryDto(Appointment appointment) {
        ConsultationNoteDto noteDto = noteRepository.findByAppointment(appointment)
                .map(note -> {
                    ConsultationNoteDto dto = new ConsultationNoteDto();
                    dto.setDiagnosis(note.getDiagnosis());
                    dto.setPrescription(note.getPrescription());
                    dto.setTreatmentDetails(note.getTreatmentDetails());
                    dto.setRemarks(note.getRemarks());
                    return dto;
                }).orElse(null);
        return AppointmentHistoryDto.builder()
                .appointmentId(appointment.getId())
                .doctorId(appointment.getDoctor().getId())
                .doctorName(appointment.getDoctor().getFullName())
                .patientId(appointment.getPatient().getId())
                .patientName(appointment.getPatient().getFullName())
                .appointmentDateTime(appointment.getAppointmentDateTime())
                .consultationNote(noteDto)
                .build();
    }
}