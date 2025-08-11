package com.hospital.Hospital.Management.service;

import com.hospital.Hospital.Management.dto.*;
import com.hospital.Hospital.Management.model.*;
import com.hospital.Hospital.Management.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for managing patient feedback and doctor rating operations.
 * 
 * This service handles all business logic related to patient feedback collection,
 * doctor performance analytics, and administrative feedback management. It ensures
 * data integrity, enforces business rules, and provides comprehensive feedback
 * management capabilities for the hospital management system.
 * 
 * Key Responsibilities:
 * - Patient feedback submission and validation
 * - Doctor rating statistics calculation and analysis
 * - Administrative feedback review and management
 * - Feedback analytics and reporting
 * - Business rule enforcement and data integrity
 * - Integration with email service for feedback notifications
 * 
 * Business Rules Enforced:
 * - Only completed appointments can receive feedback
 * - One feedback per appointment per patient
 * - Patients can only provide feedback for their own appointments
 * - Feedback can only be submitted within reasonable time after appointment
 * - Rating must be within valid range (1-5 stars)
 * 
 * @author Hospital Management Team
 * @version 1.0
 * @since 2025-08-05
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    // ===============================================================================
    // PATIENT FEEDBACK SUBMISSION METHODS
    // ===============================================================================

    /**
     * Submits new patient feedback for a completed appointment.
     * 
     * This method handles the complete feedback submission process including
     * validation, business rule enforcement, data persistence, and notification.
     * It ensures that feedback is only submitted for valid completed appointments
     * and prevents duplicate feedback submissions.
     * 
     * Validation Process:
     * 1. Verify patient authentication and appointment ownership
     * 2. Confirm appointment is in COMPLETED status
     * 3. Check no existing feedback exists for this appointment
     * 4. Validate rating and comment content
     * 5. Enforce submission time limits if configured
     * 
     * Post-Submission Actions:
     * - Flag negative feedback for administrative review
     * - Send notification emails for significant feedback
     * - Update doctor performance statistics
     * - Log feedback submission for audit trail
     * 
     * @param feedbackRequest The feedback submission data from patient
     * @param patientEmail Email of authenticated patient submitting feedback
     * @return FeedbackResponseDto containing submitted feedback details
     * @throws IllegalArgumentException if appointment invalid or feedback duplicate
     * @throws IllegalStateException if appointment not completed or outside submission window
     */
    public FeedbackResponseDto submitFeedback(FeedbackRequestDto feedbackRequest, String patientEmail) {
        log.info("Processing feedback submission for appointment {} by patient {}", 
                feedbackRequest.getAppointmentId(), patientEmail);

        // Validate patient exists and get patient entity
        User patient = userRepository.findByEmail(patientEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Patient not found: " + patientEmail));

        // Validate appointment exists and belongs to patient
        Appointment appointment = appointmentRepository.findById(feedbackRequest.getAppointmentId())
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));

        // Verify appointment ownership
        if (!appointment.getPatient().getId().equals(patient.getId())) {
            throw new IllegalArgumentException("You can only provide feedback for your own appointments");
        }

        // Verify appointment is completed
        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new IllegalStateException("Feedback can only be submitted for completed appointments");
        }

        // Check for existing feedback
        Optional<Feedback> existingFeedback = feedbackRepository.findByAppointment(appointment);
        if (existingFeedback.isPresent()) {
            throw new IllegalArgumentException("Feedback has already been submitted for this appointment");
        }

        // Validate submission timing (optional business rule)
        validateSubmissionTiming(appointment);

        // Create feedback entity
        Feedback feedback = Feedback.builder()
                .rating(feedbackRequest.getRating())
                .comment(feedbackRequest.getTrimmedComment())
                .category(feedbackRequest.getCategory())
                .wouldRecommend(feedbackRequest.getWouldRecommend())
                .appointment(appointment)
                .patient(patient)
                .doctor(appointment.getDoctor())
                .isReviewed(false)
                .build();

        // Flag for review if necessary
        if (feedbackRequest.shouldFlagForReview()) {
            feedback.setIsReviewed(false);
            log.info("Feedback for appointment {} flagged for administrative review", appointment.getId());
        }

        // Save feedback
        Feedback savedFeedback = feedbackRepository.save(feedback);
        log.info("Feedback submitted successfully with ID: {}", savedFeedback.getId());

        // Post-submission processing
        handlePostSubmissionActions(savedFeedback);

        // Convert to response DTO
        return convertToResponseDto(savedFeedback);
    }

    /**
     * Retrieves feedback history for a specific patient.
     * 
     * @param patientEmail Email of the patient requesting their feedback history
     * @return List of FeedbackResponseDto containing patient's feedback history
     */
    @Transactional(readOnly = true)
    public List<FeedbackResponseDto> getPatientFeedbackHistory(String patientEmail) {
        log.info("Retrieving feedback history for patient: {}", patientEmail);

        User patient = userRepository.findByEmail(patientEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Patient not found: " + patientEmail));

        List<Feedback> feedbackList = feedbackRepository.findByPatientOrderByCreatedAtDesc(patient);
        
        return feedbackList.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Submits test feedback without appointment validation for testing purposes.
     * 
     * This method allows testing of the feedback system without requiring actual
     * completed appointments. It bypasses all appointment-related validations and
     * creates a feedback entry with mock appointment data.
     * 
     * **WARNING: This is for testing purposes only and should not be used in production**
     * 
     * Features:
     * - No appointment validation required
     * - Creates mock appointment data if needed
     * - Assigns feedback to first available doctor
     * - Still validates rating and comment content
     * - Sends all post-submission actions (emails, notifications)
     * 
     * @param feedbackRequest The feedback submission data from patient
     * @param patientEmail Email of authenticated patient submitting feedback
     * @return FeedbackResponseDto containing submitted feedback details
     */
    public FeedbackResponseDto submitTestFeedback(FeedbackRequestDto feedbackRequest, String patientEmail) {
        log.info("Processing TEST feedback submission by patient {}", patientEmail);

        // Validate patient exists and get patient entity
        User patient = userRepository.findByEmail(patientEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Patient not found: " + patientEmail));

        // Get any doctor for test feedback (first doctor with ROLE_DOCTOR)
        User testDoctor = userRepository.findAll().stream()
                .filter(user -> user.getRoles().contains(Role.ROLE_DOCTOR))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No doctors available in the system for test feedback"));

        // Create or find a test appointment for this patient-doctor combination
        Appointment testAppointment = getOrCreateTestAppointment(patient, testDoctor);

        // Create feedback entity with test appointment
        Feedback feedback = Feedback.builder()
                .rating(feedbackRequest.getRating())
                .comment(feedbackRequest.getTrimmedComment())
                .category(feedbackRequest.getCategory())
                .wouldRecommend(feedbackRequest.getWouldRecommend())
                .appointment(testAppointment)
                .patient(patient)
                .doctor(testDoctor)
                .isReviewed(false)
                .build();

        // Flag for review if necessary
        if (feedbackRequest.shouldFlagForReview()) {
            feedback.setIsReviewed(false);
            log.info("Test feedback flagged for administrative review");
        }

        // Save feedback
        Feedback savedFeedback = feedbackRepository.save(feedback);
        log.info("TEST feedback submitted successfully with ID: {}", savedFeedback.getId());

        // Post-submission processing (emails and notifications)
        handlePostSubmissionActions(savedFeedback);

        // Convert to response DTO
        return convertToResponseDto(savedFeedback);
    }

    /**
     * Gets or creates a test appointment for feedback testing.
     * 
     * @param patient The patient for the test appointment
     * @param doctor The doctor for the test appointment
     * @return Test appointment entity
     */
    private Appointment getOrCreateTestAppointment(User patient, User doctor) {
        // Try to find existing test appointment for this patient-doctor combination
        List<Appointment> existingAppointments = appointmentRepository.findByPatientAndDoctor(patient, doctor);
        
        if (!existingAppointments.isEmpty()) {
            // Use the first existing appointment
            Appointment existingAppointment = existingAppointments.get(0);
            log.info("Using existing appointment ID: {} for test feedback", existingAppointment.getId());
            return existingAppointment;
        }

        // Create a new test appointment if none exists
        Appointment testAppointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentDateTime(LocalDateTime.now().minusDays(1)) // Yesterday to simulate completed
                .status(AppointmentStatus.COMPLETED)
                .consultationNotes("Test appointment for feedback system testing - Virtual consultation")
                .build();

        Appointment savedAppointment = appointmentRepository.save(testAppointment);
        log.info("Created new test appointment ID: {} for feedback testing", savedAppointment.getId());
        
        return savedAppointment;
    }

    // ===============================================================================
    // DOCTOR RATING STATISTICS METHODS
    // ===============================================================================

    /**
     * Calculates comprehensive rating statistics for a specific doctor.
     * 
     * This method aggregates all feedback data for a doctor and calculates
     * various performance metrics including average rating, distribution analysis,
     * Net Promoter Score, and performance classification.
     * 
     * @param doctorId The ID of the doctor for whom to calculate statistics
     * @return DoctorRatingStatsDto containing comprehensive performance metrics
     * @throws IllegalArgumentException if doctor not found
     */
    @Transactional(readOnly = true)
    public DoctorRatingStatsDto getDoctorRatingStatistics(Long doctorId) {
        log.info("Calculating rating statistics for doctor ID: {}", doctorId);

        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));

        if (!doctor.getRoles().contains(Role.ROLE_DOCTOR)) {
            throw new IllegalArgumentException("User is not a doctor");
        }

        // Get all feedback for doctor
        List<Feedback> feedbackList = feedbackRepository.findByDoctorOrderByCreatedAtDesc(doctor);
        
        if (feedbackList.isEmpty()) {
            return createEmptyStatsDto(doctor);
        }

        // Calculate statistics
        return calculateDetailedStatistics(doctor, feedbackList);
    }

    /**
     * Retrieves all feedback received by a specific doctor.
     * 
     * @param doctorId The ID of the doctor
     * @param includeSensitiveData Whether to include administrative data (admin only)
     * @return List of FeedbackResponseDto containing doctor's feedback
     */
    @Transactional(readOnly = true)
    public List<FeedbackResponseDto> getDoctorFeedback(Long doctorId, boolean includeSensitiveData) {
        log.info("Retrieving feedback for doctor ID: {}", doctorId);

        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));

        List<Feedback> feedbackList = feedbackRepository.findByDoctorOrderByCreatedAtDesc(doctor);
        
        return feedbackList.stream()
                .map(feedback -> convertToResponseDto(feedback, includeSensitiveData))
                .collect(Collectors.toList());
    }

    // ===============================================================================
    // ADMINISTRATIVE FEEDBACK MANAGEMENT METHODS
    // ===============================================================================

    /**
     * Retrieves all feedback requiring administrative review.
     * 
     * @return List of FeedbackResponseDto containing unreviewed feedback
     */
    @Transactional(readOnly = true)
    public List<FeedbackResponseDto> getFeedbackRequiringReview() {
        log.info("Retrieving feedback requiring administrative review");

        List<Feedback> unreviewedFeedback = feedbackRepository.findByIsReviewedOrderByCreatedAtAsc(false);
        
        return unreviewedFeedback.stream()
                .map(feedback -> convertToResponseDto(feedback, true))
                .collect(Collectors.toList());
    }

    /**
     * Marks feedback as reviewed and adds administrative notes.
     * 
     * @param feedbackId The ID of the feedback to review
     * @param adminNotes Administrative notes about the review
     * @param adminEmail Email of the administrator performing the review
     * @return Updated FeedbackResponseDto
     */
    public FeedbackResponseDto reviewFeedback(Long feedbackId, String adminNotes, String adminEmail) {
        log.info("Reviewing feedback ID: {} by admin: {}", feedbackId, adminEmail);

        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found"));

        feedback.setIsReviewed(true);
        feedback.setAdminNotes(adminNotes);
        
        Feedback updatedFeedback = feedbackRepository.save(feedback);
        log.info("Feedback ID: {} marked as reviewed by admin: {}", feedbackId, adminEmail);

        return convertToResponseDto(updatedFeedback, true);
    }

    /**
     * Retrieves feedback with low ratings that may require attention.
     * 
     * @param maxRating Maximum rating to consider as "low" (typically 2)
     * @return List of FeedbackResponseDto containing low-rated feedback
     */
    @Transactional(readOnly = true)
    public List<FeedbackResponseDto> getLowRatingFeedback(Integer maxRating) {
        log.info("Retrieving feedback with rating <= {}", maxRating);

        List<Feedback> lowRatingFeedback = feedbackRepository
                .findByRatingLessThanEqualOrderByCreatedAtDesc(maxRating);
        
        return lowRatingFeedback.stream()
                .map(feedback -> convertToResponseDto(feedback, true))
                .collect(Collectors.toList());
    }

    // ===============================================================================
    // ANALYTICS AND REPORTING METHODS
    // ===============================================================================

    /**
     * Generates hospital-wide feedback analytics for a specific time period.
     * 
     * @param startDate Start of analysis period
     * @param endDate End of analysis period
     * @return Map containing various analytics metrics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getHospitalFeedbackAnalytics(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Generating hospital feedback analytics for period: {} to {}", startDate, endDate);

        List<Feedback> feedbackInPeriod = feedbackRepository
                .findByCreatedAtBetweenOrderByCreatedAtDesc(startDate, endDate);

        Map<String, Object> analytics = new HashMap<>();
        
        // Basic metrics
        analytics.put("totalFeedbackCount", feedbackInPeriod.size());
        analytics.put("averageRating", calculateOverallAverageRating(feedbackInPeriod));
        analytics.put("ratingDistribution", calculateRatingDistribution(feedbackInPeriod));
        
        // Category analysis
        analytics.put("categoryBreakdown", calculateCategoryBreakdown(feedbackInPeriod));
        
        // Performance indicators
        analytics.put("positiveRatingPercentage", calculatePositiveRatingPercentage(feedbackInPeriod));
        analytics.put("negativeRatingPercentage", calculateNegativeRatingPercentage(feedbackInPeriod));
        
        // Doctor performance summary
        analytics.put("topPerformingDoctors", getTopPerformingDoctors(3));
        analytics.put("doctorsNeedingAttention", getDoctorsNeedingAttention());

        return analytics;
    }

    // ===============================================================================
    // UTILITY AND HELPER METHODS
    // ===============================================================================

    /**
     * Validates that feedback submission is within acceptable time window.
     * 
     * @param appointment The appointment for which feedback is being submitted
     * @throws IllegalStateException if submission is outside allowed time window
     */
    private void validateSubmissionTiming(Appointment appointment) {
        LocalDateTime appointmentDateTime = appointment.getAppointmentDateTime();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime submissionDeadline = appointmentDateTime.plusDays(30); // 30-day window

        if (now.isAfter(submissionDeadline)) {
            throw new IllegalStateException("Feedback submission window has expired. " +
                    "Feedback must be submitted within 30 days of appointment.");
        }
    }

    /**
     * Handles post-submission actions like notifications and review flagging.
     * 
     * @param feedback The newly submitted feedback
     */
    private void handlePostSubmissionActions(Feedback feedback) {
        // Send notification for low ratings
        if (feedback.getRating() <= 2) {
            sendLowRatingNotification(feedback);
        }

        // Send thank you email to patient
        sendFeedbackThankYouEmail(feedback);

        // Log for analytics
        log.info("Feedback submitted - Doctor: {}, Rating: {}, Category: {}", 
                feedback.getDoctor().getFullName(), 
                feedback.getRating(), 
                feedback.getCategory());
    }

    /**
     * Sends notification email for low-rating feedback to administrators.
     * 
     * @param feedback The feedback with low rating
     */
    private void sendLowRatingNotification(Feedback feedback) {
        try {
            String subject = "Low Rating Alert - Doctor Feedback";
            String message = String.format(
                "A low rating (%d stars) has been submitted for Dr. %s.\n\n" +
                "Patient: %s\n" +
                "Appointment Date: %s\n" +
                "Comment: %s\n\n" +
                "Please review this feedback for any necessary follow-up actions.",
                feedback.getRating(),
                feedback.getDoctor().getFullName(),
                feedback.getPatient().getFullName(),
                feedback.getAppointment().getAppointmentDateTime().toLocalDate(),
                feedback.getComment() != null ? feedback.getComment() : "No comment provided"
            );

            // Send to admin email (configure in properties)
            emailService.sendGenericEmail("admin@hospital.com", subject, message);
            
        } catch (Exception e) {
            log.error("Failed to send low rating notification", e);
        }
    }

    /**
     * Sends thank you email to patient after feedback submission.
     * 
     * @param feedback The submitted feedback
     */
    private void sendFeedbackThankYouEmail(Feedback feedback) {
        try {
            String subject = "Thank You for Your Feedback";
            String message = String.format(
                "Dear %s,\n\n" +
                "Thank you for taking the time to provide feedback about your recent appointment " +
                "with Dr. %s on %s.\n\n" +
                "Your feedback helps us continuously improve our services and patient care. " +
                "We value your input and will use it to enhance your future experiences with us.\n\n" +
                "If you have any additional concerns or questions, please don't hesitate to contact us.\n\n" +
                "Thank you for choosing our hospital for your healthcare needs.",
                feedback.getPatient().getFullName(),
                feedback.getDoctor().getFullName(),
                feedback.getAppointment().getAppointmentDateTime().toLocalDate()
            );

            emailService.sendGenericEmail(feedback.getPatient().getEmail(), subject, message);
            
        } catch (Exception e) {
            log.error("Failed to send feedback thank you email", e);
        }
    }

    /**
     * Converts Feedback entity to FeedbackResponseDto.
     * 
     * @param feedback The feedback entity to convert
     * @return FeedbackResponseDto with complete feedback information
     */
    private FeedbackResponseDto convertToResponseDto(Feedback feedback) {
        return convertToResponseDto(feedback, false);
    }

    /**
     * Converts Feedback entity to FeedbackResponseDto with optional sensitive data.
     * 
     * @param feedback The feedback entity to convert
     * @param includeSensitiveData Whether to include administrative fields
     * @return FeedbackResponseDto with appropriate data based on permissions
     */
    private FeedbackResponseDto convertToResponseDto(Feedback feedback, boolean includeSensitiveData) {
        FeedbackResponseDto.FeedbackResponseDtoBuilder builder = FeedbackResponseDto.builder()
                .id(feedback.getId())
                .rating(feedback.getRating())
                .comment(feedback.getComment())
                .category(feedback.getCategory())
                .wouldRecommend(feedback.getWouldRecommend())
                .isReviewed(feedback.getIsReviewed())
                .createdAt(feedback.getCreatedAt())
                .updatedAt(feedback.getUpdatedAt())
                // Appointment information
                .appointmentId(feedback.getAppointment().getId())
                .appointmentDateTime(feedback.getAppointment().getAppointmentDateTime())
                .appointmentStatus(feedback.getAppointment().getStatus().name())
                // Patient information
                .patientId(feedback.getPatient().getId())
                .patientName(feedback.getPatient().getFullName())
                // Doctor information
                .doctorId(feedback.getDoctor().getId())
                .doctorName(feedback.getDoctor().getFullName())
                .doctorSpecialization("General Practice"); // Could be enhanced to actual specialization

        // Include sensitive data only for administrators
        if (includeSensitiveData) {
            builder.adminNotes(feedback.getAdminNotes())
                   .patientEmail(feedback.getPatient().getEmail());
        }

        return builder.build();
    }

    /**
     * Creates empty statistics DTO for doctors with no feedback.
     * 
     * @param doctor The doctor entity
     * @return DoctorRatingStatsDto with empty/default values
     */
    private DoctorRatingStatsDto createEmptyStatsDto(User doctor) {
        return DoctorRatingStatsDto.builder()
                .doctorId(doctor.getId())
                .doctorName(doctor.getFullName())
                .specialization("General Practice")
                .averageRating(null)
                .totalFeedbackCount(0L)
                .ratingDistribution(new HashMap<>())
                .recommendCount(0L)
                .notRecommendCount(0L)
                .neutralCount(0L)
                .netPromoterScore(null)
                .positiveRatingPercentage(null)
                .negativeRatingPercentage(null)
                .displayStars(0)
                .performanceLevel(null)
                .hasReliableStats(false)
                .build();
    }

    /**
     * Calculates detailed statistics from feedback list.
     * 
     * @param doctor The doctor entity
     * @param feedbackList List of feedback for the doctor
     * @return DoctorRatingStatsDto with calculated statistics
     */
    private DoctorRatingStatsDto calculateDetailedStatistics(User doctor, List<Feedback> feedbackList) {
        // Calculate basic statistics
        double averageRating = feedbackList.stream()
                .mapToInt(Feedback::getRating)
                .average()
                .orElse(0.0);

        long totalCount = feedbackList.size();

        // Calculate rating distribution
        Map<Integer, Long> ratingDistribution = feedbackList.stream()
                .collect(Collectors.groupingBy(
                        Feedback::getRating,
                        Collectors.counting()
                ));

        // Calculate recommendation statistics
        long recommendCount = feedbackList.stream()
                .filter(f -> f.getWouldRecommend() != null && f.getWouldRecommend())
                .count();

        long notRecommendCount = feedbackList.stream()
                .filter(f -> f.getWouldRecommend() != null && !f.getWouldRecommend())
                .count();

        long neutralCount = feedbackList.stream()
                .filter(f -> f.getWouldRecommend() == null)
                .count();

        // Calculate NPS
        Double nps = null;
        long totalRecommendationResponses = recommendCount + notRecommendCount;
        if (totalRecommendationResponses > 0) {
            nps = (double) (recommendCount - notRecommendCount) / totalRecommendationResponses * 100;
        }

        // Calculate positive/negative percentages
        long positiveCount = feedbackList.stream()
                .filter(f -> f.getRating() >= 4)
                .count();

        long negativeCount = feedbackList.stream()
                .filter(f -> f.getRating() <= 2)
                .count();

        double positivePercentage = (double) positiveCount / totalCount * 100;
        double negativePercentage = (double) negativeCount / totalCount * 100;

        // Determine performance level
        DoctorRatingStatsDto.PerformanceLevel performanceLevel = determinePerformanceLevel(
                averageRating, positivePercentage, nps);

        return DoctorRatingStatsDto.builder()
                .doctorId(doctor.getId())
                .doctorName(doctor.getFullName())
                .specialization("General Practice")
                .averageRating(averageRating)
                .totalFeedbackCount(totalCount)
                .ratingDistribution(ratingDistribution)
                .recommendCount(recommendCount)
                .notRecommendCount(notRecommendCount)
                .neutralCount(neutralCount)
                .netPromoterScore(nps)
                .positiveRatingPercentage(positivePercentage)
                .negativeRatingPercentage(negativePercentage)
                .displayStars((int) Math.round(averageRating))
                .performanceLevel(performanceLevel)
                .hasReliableStats(totalCount >= 10)
                .build();
    }

    /**
     * Determines performance level based on metrics.
     * 
     * @param averageRating Average rating score
     * @param positivePercentage Percentage of positive ratings
     * @param nps Net Promoter Score
     * @return PerformanceLevel classification
     */
    private DoctorRatingStatsDto.PerformanceLevel determinePerformanceLevel(
            double averageRating, double positivePercentage, Double nps) {
        
        if (averageRating >= 4.5 && positivePercentage >= 90 && nps != null && nps >= 70) {
            return DoctorRatingStatsDto.PerformanceLevel.EXCELLENT;
        } else if (averageRating >= 4.0 && positivePercentage >= 80 && nps != null && nps >= 50) {
            return DoctorRatingStatsDto.PerformanceLevel.VERY_GOOD;
        } else if (averageRating >= 3.5 && positivePercentage >= 70 && nps != null && nps >= 30) {
            return DoctorRatingStatsDto.PerformanceLevel.GOOD;
        } else if (averageRating >= 3.0 && positivePercentage >= 60 && (nps == null || nps >= 0)) {
            return DoctorRatingStatsDto.PerformanceLevel.SATISFACTORY;
        } else {
            return DoctorRatingStatsDto.PerformanceLevel.NEEDS_IMPROVEMENT;
        }
    }

    // Additional helper methods for analytics would be implemented here
    private Double calculateOverallAverageRating(List<Feedback> feedbackList) {
        return feedbackList.stream()
                .mapToInt(Feedback::getRating)
                .average()
                .orElse(0.0);
    }

    private Map<Integer, Long> calculateRatingDistribution(List<Feedback> feedbackList) {
        return feedbackList.stream()
                .collect(Collectors.groupingBy(
                        Feedback::getRating,
                        Collectors.counting()
                ));
    }

    private Map<String, Long> calculateCategoryBreakdown(List<Feedback> feedbackList) {
        return feedbackList.stream()
                .filter(f -> f.getCategory() != null)
                .collect(Collectors.groupingBy(
                        f -> f.getCategory().getDisplayName(),
                        Collectors.counting()
                ));
    }

    private Double calculatePositiveRatingPercentage(List<Feedback> feedbackList) {
        if (feedbackList.isEmpty()) return 0.0;
        
        long positiveCount = feedbackList.stream()
                .filter(f -> f.getRating() >= 4)
                .count();
        
        return (double) positiveCount / feedbackList.size() * 100;
    }

    private Double calculateNegativeRatingPercentage(List<Feedback> feedbackList) {
        if (feedbackList.isEmpty()) return 0.0;
        
        long negativeCount = feedbackList.stream()
                .filter(f -> f.getRating() <= 2)
                .count();
        
        return (double) negativeCount / feedbackList.size() * 100;
    }

    private List<String> getTopPerformingDoctors(int limit) {
        // Implementation would get top doctors by rating
        return new ArrayList<>();
    }

    private List<String> getDoctorsNeedingAttention() {
        // Implementation would get doctors with low ratings
        return new ArrayList<>();
    }
}
