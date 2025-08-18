package com.hospital.Hospital.Management.service;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hospital.Hospital.Management.dto.AppointmentRequestDto;
import com.hospital.Hospital.Management.dto.AppointmentResponseDto;
import com.hospital.Hospital.Management.dto.BookingResponseDto;
import com.hospital.Hospital.Management.exception.ResourceNotFoundException;
import com.hospital.Hospital.Management.exception.SlotUnavailableException;
import com.hospital.Hospital.Management.model.Appointment;
import com.hospital.Hospital.Management.model.AppointmentStatus;
import com.hospital.Hospital.Management.model.DayOfWeek;
import com.hospital.Hospital.Management.model.User;
import com.hospital.Hospital.Management.model.WaitlistEntry;
import com.hospital.Hospital.Management.repository.AppointmentRepository;
import com.hospital.Hospital.Management.repository.DoctorAvailabilityRepository;
import com.hospital.Hospital.Management.repository.UserRepository;
import com.hospital.Hospital.Management.repository.WaitlistRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final DoctorAvailabilityRepository availabilityRepository;
    private final WaitlistRepository waitlistRepository; 
    private final EmailService emailService;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              UserRepository userRepository,
                              DoctorAvailabilityRepository availabilityRepository,
                              WaitlistRepository waitlistRepository, 
                              EmailService emailService) {
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.availabilityRepository = availabilityRepository;
        this.waitlistRepository = waitlistRepository; 
        this.emailService = emailService;
    }

    @Transactional
    public BookingResponseDto bookAppointment(AppointmentRequestDto requestDto, String patientEmail) {
        log.info("Attempting to book appointment for patient [{}] with doctor [{}]", patientEmail, requestDto.getDoctorId());
        User patient = userRepository.findByEmail(patientEmail).orElseThrow(() -> new UsernameNotFoundException("Patient not found"));
        User doctor = userRepository.findById(requestDto.getDoctorId()).orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + requestDto.getDoctorId()));

        LocalDateTime requestedDateTime = requestDto.getAppointmentDateTime();

        Optional<Appointment> existingBookingOpt = appointmentRepository.findByDoctorAndAppointmentDateTime(doctor, requestedDateTime);
        if (existingBookingOpt.isPresent()) {
            Appointment existingBooking = existingBookingOpt.get();
            if (existingBooking.getPatient().getId().equals(patient.getId())) {
                log.warn("Booking failed: Patient {} tried to re-book their own exact slot at {}.", patient.getId(), requestedDateTime);
                throw new SlotUnavailableException("You have already booked this exact time slot. To make changes, please cancel or reschedule.");
            } else {
                log.warn("Booking failed: Time slot {} for doctor {} is already booked by another patient. Offering waitlist.", requestedDateTime, doctor.getId());
                return BookingResponseDto.builder()
                        .success(false)
                        .message("The selected slot is already booked. Would you like to join the waitlist for this day?")
                        .waitlistAvailable(true)
                        .build();
            }
        }

        DayOfWeek requestedDay = DayOfWeek.valueOf(requestedDateTime.getDayOfWeek().name());
        boolean isGenerallyAvailable = availabilityRepository.findByDoctorId(doctor.getId()).stream()
                .anyMatch(avail -> avail.getDayOfWeek() == requestedDay &&
                        !requestedDateTime.toLocalTime().isBefore(avail.getStartTime()) &&
                        requestedDateTime.toLocalTime().isBefore(avail.getEndTime()));

        if (!isGenerallyAvailable) {
            log.warn("Booking failed: Doctor {} is not available on {} at {}", doctor.getId(), requestedDay, requestedDateTime.toLocalTime());
            throw new SlotUnavailableException("Doctor is not available for the selected day or time.");
        }

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentDateTime(requestedDateTime)
                .status(AppointmentStatus.SCHEDULED)
                .build();
        Appointment savedAppointment = appointmentRepository.save(appointment);
        log.info("Successfully booked appointment [id: {}]", savedAppointment.getId());

        return BookingResponseDto.builder()
                .success(true)
                .message("Appointment booked successfully!")
                .appointmentDetails(mapToResponseDto(savedAppointment))
                .build();
    }

    @Transactional
    public void cancelAppointment(Long appointmentId, String userEmail) throws AccessDeniedException {
        log.info("User [{}] attempting to cancel appointment [{}]", userEmail, appointmentId);
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment with ID " + appointmentId + " not found."));

        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + userEmail));

        boolean isPatientOfAppointment = appointment.getPatient().getId().equals(currentUser.getId());
        if (!isPatientOfAppointment) {
            log.warn("SECURITY ALERT: User {} attempted to cancel appointment {} they do not own.", userEmail, appointmentId);
            throw new AccessDeniedException("You are not authorized to cancel this appointment.");
        }

        List<AppointmentStatus> nonCancellableStatuses = List.of(
                AppointmentStatus.COMPLETED,
                AppointmentStatus.CANCELLED_BY_DOCTOR,
                AppointmentStatus.CANCELLED_BY_PATIENT
        );

        if (nonCancellableStatuses.contains(appointment.getStatus())) {
            log.warn("Action blocked: Patient {} attempted to cancel an appointment with status {}.",
                    userEmail, appointment.getStatus());
            throw new IllegalStateException("Cannot cancel a completed or already cancelled appointment.");
        }
        if (appointment.getStatus() == AppointmentStatus.CONFIRMED_BY_DOCTOR) {
            throw new IllegalStateException("Cannot cancel a doctor-confirmed appointment. Please contact the clinic directly.");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED_BY_PATIENT);
        appointmentRepository.saveAndFlush(appointment);
        log.info("Appointment [{}] cancelled successfully by patient.", appointmentId);

        try {
            emailService.sendCancellationByPatientToDoctorEmail(appointment);
        } catch (Exception e) {
            log.error("Failed to send patient cancellation notification for appointment {}. Error: {}", appointmentId, e.getMessage());
        }

        
        processWaitlistForCancellation(appointment);
    }

   
    @Transactional
    public AppointmentResponseDto rescheduleAppointment(Long appointmentId, String patientEmail, LocalDateTime newDateTime) throws AccessDeniedException {
        log.info("Patient [{}] attempting to reschedule appointment [{}] to {}", patientEmail, appointmentId, newDateTime);
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        User currentUser = userRepository.findByEmail(patientEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + patientEmail));

        if (!appointment.getPatient().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You can only reschedule your own appointments.");
        }

        if (appointment.getStatus() == AppointmentStatus.COMPLETED || appointment.getStatus() == AppointmentStatus.CONFIRMED_BY_DOCTOR) {
            throw new IllegalStateException("Cannot reschedule a completed or confirmed appointment. Please contact the clinic.");
        }

        User doctor = appointment.getDoctor();
        appointmentRepository.findByDoctorAndAppointmentDateTime(doctor, newDateTime)
                .ifPresent(existing -> {
                    if (!existing.getId().equals(appointmentId)) {
                        throw new SlotUnavailableException("The new time slot is already booked.");
                    }
                });

        appointment.setAppointmentDateTime(newDateTime);
        Appointment savedAppointment = appointmentRepository.save(appointment);

        try {
            emailService.sendAppointmentRescheduleByPatientEmail(savedAppointment, appointment.getAppointmentDateTime());
        } catch(Exception e) {
            log.error("Failed to send reschedule notification to doctor for appointment {}", appointmentId, e);
        }

        return mapToResponseDto(savedAppointment);
    }

    private void processWaitlistForCancellation(Appointment cancelledAppointment) {
        User doctor = cancelledAppointment.getDoctor();
        LocalDate date = cancelledAppointment.getAppointmentDateTime().toLocalDate();
        log.info("Checking waitlist for Dr. {} on date {}", doctor.getFullName(), date);

        List<WaitlistEntry> waitlistEntries = waitlistRepository.findByDoctorAndPreferredDateOrderByCreatedAtAsc(doctor, date);

        if (!waitlistEntries.isEmpty()) {
            WaitlistEntry topEntry = waitlistEntries.get(0);
            User patientToBook = topEntry.getPatient();
            LocalDateTime freedSlot = cancelledAppointment.getAppointmentDateTime();

            log.info("Found patient {} on waitlist. Booking them into freed slot at {}", patientToBook.getFullName(), freedSlot);

            Appointment newAppointment = Appointment.builder()
                    .patient(patientToBook)
                    .doctor(doctor)
                    .appointmentDateTime(freedSlot)
                    .status(AppointmentStatus.SCHEDULED) 
                    .build();
            appointmentRepository.save(newAppointment);

            try {
                emailService.sendWaitlistBookingConfirmationEmail(newAppointment);
            } catch (Exception e) {
                log.error("Failed to send waitlist booking confirmation email for new appointment {}", newAppointment.getId(), e);
            }

            waitlistRepository.delete(topEntry);
            log.info("Removed patient {} from waitlist and created new appointment {}", patientToBook.getFullName(), newAppointment.getId());
        } else {
            log.info("No patients found on the waitlist for Dr. {} on {}", doctor.getFullName(), date);
        }
    }

    
    public List<AppointmentResponseDto> getAppointmentsForPatient(String patientEmail) {
        log.info("Fetching all appointments for patient: {}", patientEmail);
        User patient = userRepository.findByEmail(patientEmail).orElseThrow(() -> new UsernameNotFoundException("Patient not found"));
        return appointmentRepository.findByPatient(patient).stream().map(this::mapToResponseDto).collect(Collectors.toList());
    }

    public List<AppointmentResponseDto> getUpcomingAppointmentsForPatient(String patientEmail) {
        log.info("Fetching UPCOMING appointments for patient: {}", patientEmail);
        User patient = userRepository.findByEmail(patientEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Patient not found with email: " + patientEmail));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime futureLimit = now.plusDays(30);
        List<AppointmentStatus> upcomingStatuses = List.of(AppointmentStatus.SCHEDULED, AppointmentStatus.CONFIRMED_BY_DOCTOR);

        return appointmentRepository.findByPatientAndStatusInAndAppointmentDateTimeBetween(
                        patient, upcomingStatuses, now, futureLimit)
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public List<AppointmentResponseDto> getAllUpcomingAppointments() {
        log.info("Fetching ALL upcoming appointments for admin/doctor view");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime futureLimit = now.plusDays(30);
        List<AppointmentStatus> upcomingStatuses = List.of(AppointmentStatus.SCHEDULED, AppointmentStatus.CONFIRMED_BY_DOCTOR);

        return appointmentRepository.findAllByStatusInAndAppointmentDateTimeBetween(
                        upcomingStatuses, now, futureLimit)
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public void sendAppointmentReminders() {
        log.info("Starting batch process to send appointment reminders.");
        LocalDateTime tomorrowStart = LocalDate.now().plusDays(1).atStartOfDay();
        LocalDateTime tomorrowEnd = tomorrowStart.plusDays(1);

        List<Appointment> appointmentsForTomorrow = appointmentRepository
                .findByAppointmentDateTimeBetweenAndStatus(tomorrowStart, tomorrowEnd, AppointmentStatus.SCHEDULED);

        log.info("Found {} appointments for tomorrow requiring reminders.", appointmentsForTomorrow.size());

        appointmentsForTomorrow.forEach(this::sendSingleReminder);
    }

    public void sendSingleAppointmentReminder(Long appointmentId) {
        log.info("Sending single reminder for appointment ID: {}", appointmentId);
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + appointmentId));

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            log.warn("Cannot send reminder for appointment {} because its status is {}", appointmentId, appointment.getStatus());
            throw new IllegalStateException("Reminder can only be sent for 'SCHEDULED' appointments.");
        }

        sendSingleReminder(appointment);
    }

    private void sendSingleReminder(Appointment appointment) {
        try {
            emailService.sendAppointmentReminderEmail(
                    appointment.getPatient().getEmail(),
                    appointment.getPatient().getFullName(),
                    appointment.getDoctor().getFullName(),
                    appointment.getAppointmentDateTime().toLocalDate().toString(),
                    appointment.getAppointmentDateTime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                    appointment.getDoctor().getLocation() != null ? appointment.getDoctor().getLocation() : "Hospital Main Campus"
            );
            log.info("Successfully sent reminder for appointment ID: {}", appointment.getId());
        } catch (Exception e) {
            log.error("Failed to send reminder for appointment ID: {}. Error: {}", appointment.getId(), e.getMessage());
        }
    }

    private AppointmentResponseDto mapToResponseDto(Appointment appointment) {
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
}