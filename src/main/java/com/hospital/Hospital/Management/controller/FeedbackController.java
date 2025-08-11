package com.hospital.Hospital.Management.controller;

import com.hospital.Hospital.Management.dto.*;
import com.hospital.Hospital.Management.model.*;
import com.hospital.Hospital.Management.repository.UserRepository;
import com.hospital.Hospital.Management.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for managing patient feedback and doctor rating operations.
 * 
 * This controller provides comprehensive endpoints for patient feedback collection,
 * doctor performance review, and administrative feedback management. It implements
 * role-based access control ensuring proper security and data privacy across all
 * feedback-related operations.
 * 
 * Endpoint Categories:
 * 1. Patient Feedback Submission - Allow patients to submit and view their feedback
 * 2. Doctor Rating Analytics - Provide doctors access to their performance metrics
 * 3. Administrative Management - Enable admin oversight and review capabilities
 * 4. Hospital Analytics - Generate system-wide feedback reports and insights
 * 
 * Security Implementation:
 * - Role-based access control with PATIENT, DOCTOR, and ADMIN roles
 * - Authentication required for all endpoints
 * - Data isolation ensuring users only access authorized information
 * - Input validation and sanitization on all requests
 * - Audit logging for administrative operations
 * 
 * Response Format:
 * - Consistent ApiResponse wrapper for all operations
 * - Detailed error messages with appropriate HTTP status codes
 * - Comprehensive success responses with relevant data
 * - Validation error details for client-side correction
 * 
 * @author Hospital Management Team
 * @version 1.0
 * @since 2025-08-05
 */
@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final UserRepository userRepository;

    // ===============================================================================
    // PATIENT FEEDBACK SUBMISSION ENDPOINTS
    // ===============================================================================

    /**
     * Allows patients to submit feedback for their completed appointments.
     * 
     * This endpoint enables authenticated patients to provide ratings and comments
     * for their completed appointments. It includes comprehensive validation to
     * ensure feedback integrity and prevent abuse of the system.
     * 
     * Business Rules:
     * - Only patients can submit feedback for their own appointments
     * - Feedback can only be submitted for COMPLETED appointments
     * - One feedback per appointment is allowed
     * - Rating must be between 1-5 stars
     * - Comments are optional but recommended
     * - Submission must occur within configured time window
     * 
     * Request Validation:
     * - Appointment ID must be valid and exist
     * - Rating must be in valid range (1-5)
     * - Comment length must not exceed limits
     * - Category must be from allowed enum values
     * 
     * Post-Submission Processing:
     * - Automatic flagging for low ratings (≤2 stars)
     * - Email notifications for patient confirmation
     * - Administrative alerts for negative feedback
     * - Performance statistics updates for doctors
     * 
     * @param feedbackRequest The feedback submission data containing rating, comment, and preferences
     * @param authentication Spring Security authentication object containing patient details
     * @return ResponseEntity<ApiResponse<FeedbackResponseDto>> confirmation of successful submission
     */
    @PostMapping("/submit")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<FeedbackResponseDto>> submitFeedback(
            @Valid @RequestBody FeedbackRequestDto feedbackRequest,
            Authentication authentication) {
        
        log.info("Feedback submission request received for appointment {} by patient {}", 
                feedbackRequest.getAppointmentId(), authentication.getName());

        try {
            String patientEmail = authentication.getName();
            FeedbackResponseDto response = feedbackService.submitFeedback(feedbackRequest, patientEmail);
            
            log.info("Feedback submitted successfully with ID: {} for patient: {}", 
                    response.getId(), patientEmail);

            return ResponseEntity.ok(ApiResponse.<FeedbackResponseDto>builder()
                    .success(true)
                    .message("Thank you for your feedback! Your input helps us improve our services.")
                    .data(response)
                    .build());

        } catch (IllegalArgumentException e) {
            log.warn("Invalid feedback submission attempt: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.<FeedbackResponseDto>builder()
                    .success(false)
                    .message(e.getMessage())
                    .build());

        } catch (IllegalStateException e) {
            log.warn("Feedback submission business rule violation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.<FeedbackResponseDto>builder()
                    .success(false)
                    .message(e.getMessage())
                    .build());

        } catch (Exception e) {
            log.error("Error processing feedback submission", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<FeedbackResponseDto>builder()
                            .success(false)
                            .message("An error occurred while processing your feedback. Please try again later.")
                            .build());
        }
    }

    /**
     * Submit test feedback without appointment validation (FOR TESTING PURPOSES ONLY).
     * 
     * This endpoint allows testing of the feedback system without requiring actual
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
     * Request Body Requirements:
     * - rating: Integer between 1-5 (required)
     * - comment: String feedback text (optional)
     * - category: Feedback category (optional)
     * - wouldRecommend: Boolean recommendation (optional)
     * - appointmentId field is ignored for test feedback
     * 
     * @param feedbackRequest The feedback submission data (appointmentId ignored)
     * @param authentication Spring Security authentication object containing patient details
     * @return ResponseEntity<ApiResponse<FeedbackResponseDto>> confirmation of successful test feedback submission
     */
    @PostMapping("/submit-test")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<FeedbackResponseDto>> submitTestFeedback(
            @Valid @RequestBody FeedbackRequestDto feedbackRequest,
            Authentication authentication) {
        
        log.info("TEST feedback submission request received by patient {}", authentication.getName());

        try {
            String patientEmail = authentication.getName();
            FeedbackResponseDto response = feedbackService.submitTestFeedback(feedbackRequest, patientEmail);
            
            log.info("TEST feedback submitted successfully with ID: {} for patient: {}", 
                    response.getId(), patientEmail);

            return ResponseEntity.ok(ApiResponse.<FeedbackResponseDto>builder()
                    .success(true)
                    .message("TEST feedback submitted successfully! This is a test submission that bypasses appointment validation.")
                    .data(response)
                    .build());

        } catch (IllegalArgumentException e) {
            log.warn("Invalid test feedback submission attempt: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.<FeedbackResponseDto>builder()
                    .success(false)
                    .message(e.getMessage())
                    .build());

        } catch (IllegalStateException e) {
            log.warn("Test feedback submission business rule violation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.<FeedbackResponseDto>builder()
                    .success(false)
                    .message(e.getMessage())
                    .build());

        } catch (Exception e) {
            log.error("Error processing test feedback submission", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<FeedbackResponseDto>builder()
                            .success(false)
                            .message("An error occurred while processing your test feedback. Please try again later.")
                            .build());
        }
    }

    /**
     * Retrieves feedback history for the authenticated patient.
     * 
     * This endpoint allows patients to view all their previously submitted feedback,
     * providing a complete history of their experience ratings and comments. The
     * response includes appointment details and submission timestamps for reference.
     * 
     * @param authentication Spring Security authentication object containing patient details
     * @return ResponseEntity containing list of patient's feedback submissions
     */
    @GetMapping("/my-feedback")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<List<FeedbackResponseDto>>> getMyFeedback(Authentication authentication) {
        
        log.info("Retrieving feedback history for patient: {}", authentication.getName());

        try {
            String patientEmail = authentication.getName();
            List<FeedbackResponseDto> feedbackHistory = feedbackService.getPatientFeedbackHistory(patientEmail);
            
            return ResponseEntity.ok(ApiResponse.<List<FeedbackResponseDto>>builder()
                    .success(true)
                    .message(String.format("Retrieved %d feedback submissions", feedbackHistory.size()))
                    .data(feedbackHistory)
                    .build());

        } catch (Exception e) {
            log.error("Error retrieving patient feedback history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<FeedbackResponseDto>>builder()
                            .success(false)
                            .message("Error retrieving your feedback history. Please try again later.")
                            .build());
        }
    }

    // ===============================================================================
    // DOCTOR RATING AND PERFORMANCE ENDPOINTS
    // ===============================================================================

    /**
     * Provides comprehensive rating statistics for the authenticated doctor.
     * 
     * This endpoint allows doctors to view their performance metrics including
     * average rating, rating distribution, patient recommendation statistics,
     * and Net Promoter Score. The data helps doctors understand patient satisfaction
     * and identify areas for improvement in their practice.
     * 
     * Statistics Included:
     * - Overall average rating with star display
     * - Rating distribution across 1-5 star scale
     * - Patient recommendation metrics (would recommend / would not recommend)
     * - Net Promoter Score calculation
     * - Performance level classification
     * - Positive/negative feedback percentages
     * - Total feedback count and reliability indicators
     * 
     * @param authentication Spring Security authentication object containing doctor details
     * @return ResponseEntity containing comprehensive doctor performance statistics
     */
    @GetMapping("/my-stats")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ApiResponse<DoctorRatingStatsDto>> getMyRatingStats(Authentication authentication) {
        
        log.info("Retrieving rating statistics for doctor: {}", authentication.getName());

        try {
            // Get doctor user details from authentication
            String doctorEmail = authentication.getName();
            
            // Note: This implementation assumes email lookup to get doctor ID
            // In a real application, you might store user ID in the authentication token
            DoctorRatingStatsDto stats = feedbackService.getDoctorRatingStatistics(getDoctorIdFromEmail(doctorEmail));
            
            return ResponseEntity.ok(ApiResponse.<DoctorRatingStatsDto>builder()
                    .success(true)
                    .message("Rating statistics retrieved successfully")
                    .data(stats)
                    .build());

        } catch (IllegalArgumentException e) {
            log.warn("Invalid doctor statistics request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.<DoctorRatingStatsDto>builder()
                    .success(false)
                    .message(e.getMessage())
                    .build());

        } catch (Exception e) {
            log.error("Error retrieving doctor rating statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<DoctorRatingStatsDto>builder()
                            .success(false)
                            .message("Error retrieving your rating statistics. Please try again later.")
                            .build());
        }
    }

    /**
     * Retrieves all feedback received by the authenticated doctor.
     * 
     * This endpoint allows doctors to view all feedback they have received from
     * patients, including ratings, comments, and appointment context. This helps
     * doctors understand patient experiences and improve their service quality.
     * 
     * @param authentication Spring Security authentication object containing doctor details
     * @return ResponseEntity containing list of doctor's received feedback
     */
    @GetMapping("/my-doctor-feedback")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ApiResponse<List<FeedbackResponseDto>>> getMyDoctorFeedback(Authentication authentication) {
        
        log.info("Retrieving feedback for doctor: {}", authentication.getName());

        try {
            String doctorEmail = authentication.getName();
            Long doctorId = getDoctorIdFromEmail(doctorEmail);
            
            List<FeedbackResponseDto> doctorFeedback = feedbackService.getDoctorFeedback(doctorId, false);
            
            return ResponseEntity.ok(ApiResponse.<List<FeedbackResponseDto>>builder()
                    .success(true)
                    .message(String.format("Retrieved %d feedback items", doctorFeedback.size()))
                    .data(doctorFeedback)
                    .build());

        } catch (Exception e) {
            log.error("Error retrieving doctor feedback", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<FeedbackResponseDto>>builder()
                            .success(false)
                            .message("Error retrieving your feedback. Please try again later.")
                            .build());
        }
    }

    /**
     * Allows doctors to view rating statistics for a specific colleague (with permissions).
     * 
     * This endpoint enables doctors to view performance statistics for other doctors
     * in the system, which can be useful for peer learning and collaboration. Access
     * may be restricted based on hospital policies and doctor relationships.
     * 
     * @param doctorId The ID of the doctor whose statistics are being requested
     * @param authentication Spring Security authentication object
     * @return ResponseEntity containing requested doctor's performance statistics
     */
    @GetMapping("/doctor/{doctorId}/stats")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DoctorRatingStatsDto>> getDoctorStats(
            @PathVariable Long doctorId,
            Authentication authentication) {
        
        log.info("Retrieving rating statistics for doctor ID: {} by user: {}", doctorId, authentication.getName());

        try {
            DoctorRatingStatsDto stats = feedbackService.getDoctorRatingStatistics(doctorId);
            
            return ResponseEntity.ok(ApiResponse.<DoctorRatingStatsDto>builder()
                    .success(true)
                    .message("Doctor rating statistics retrieved successfully")
                    .data(stats)
                    .build());

        } catch (IllegalArgumentException e) {
            log.warn("Invalid doctor statistics request for ID {}: {}", doctorId, e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.<DoctorRatingStatsDto>builder()
                    .success(false)
                    .message(e.getMessage())
                    .build());

        } catch (Exception e) {
            log.error("Error retrieving doctor rating statistics for ID: {}", doctorId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<DoctorRatingStatsDto>builder()
                            .success(false)
                            .message("Error retrieving doctor statistics. Please try again later.")
                            .build());
        }
    }

    // ===============================================================================
    // ADMINISTRATIVE FEEDBACK MANAGEMENT ENDPOINTS
    // ===============================================================================

    /**
     * Retrieves all feedback requiring administrative review.
     * 
     * This endpoint allows administrators to view feedback that has been flagged
     * for review, typically including low ratings, concerning comments, or other
     * indicators that may require administrative attention or follow-up action.
     * 
     * Review Criteria:
     * - Feedback with ratings ≤ 2 stars
     * - Comments containing negative sentiment
     * - Patient complaints or concerns
     * - Unusual patterns or suspicious submissions
     * 
     * @param authentication Spring Security authentication object for admin verification
     * @return ResponseEntity containing list of feedback requiring administrative review
     */
    @GetMapping("/admin/pending-review")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<FeedbackResponseDto>>> getFeedbackRequiringReview(
            Authentication authentication) {
        
        log.info("Admin {} retrieving feedback requiring review", authentication.getName());

        try {
            List<FeedbackResponseDto> pendingReview = feedbackService.getFeedbackRequiringReview();
            
            return ResponseEntity.ok(ApiResponse.<List<FeedbackResponseDto>>builder()
                    .success(true)
                    .message(String.format("Retrieved %d feedback items requiring review", pendingReview.size()))
                    .data(pendingReview)
                    .build());

        } catch (Exception e) {
            log.error("Error retrieving feedback requiring review", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<FeedbackResponseDto>>builder()
                            .success(false)
                            .message("Error retrieving feedback for review. Please try again later.")
                            .build());
        }
    }

    /**
     * Allows administrators to mark feedback as reviewed and add notes.
     * 
     * This endpoint enables administrators to complete the review process for
     * flagged feedback, adding administrative notes about actions taken or
     * decisions made regarding the feedback content.
     * 
     * @param feedbackId The ID of the feedback being reviewed
     * @param request Request body containing administrative notes
     * @param authentication Spring Security authentication object for admin verification
     * @return ResponseEntity containing updated feedback with review status
     */
    @PutMapping("/admin/{feedbackId}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FeedbackResponseDto>> reviewFeedback(
            @PathVariable Long feedbackId,
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        
        log.info("Admin {} reviewing feedback ID: {}", authentication.getName(), feedbackId);

        try {
            String adminNotes = request.get("adminNotes");
            String adminEmail = authentication.getName();
            
            FeedbackResponseDto reviewedFeedback = feedbackService.reviewFeedback(feedbackId, adminNotes, adminEmail);
            
            return ResponseEntity.ok(ApiResponse.<FeedbackResponseDto>builder()
                    .success(true)
                    .message("Feedback reviewed successfully")
                    .data(reviewedFeedback)
                    .build());

        } catch (IllegalArgumentException e) {
            log.warn("Invalid feedback review request for ID {}: {}", feedbackId, e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.<FeedbackResponseDto>builder()
                    .success(false)
                    .message(e.getMessage())
                    .build());

        } catch (Exception e) {
            log.error("Error reviewing feedback ID: {}", feedbackId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<FeedbackResponseDto>builder()
                            .success(false)
                            .message("Error reviewing feedback. Please try again later.")
                            .build());
        }
    }

    /**
     * Retrieves feedback with low ratings for administrative attention.
     * 
     * This endpoint allows administrators to view all feedback with ratings
     * below a specified threshold, helping identify patterns of poor patient
     * satisfaction that may require systematic intervention.
     * 
     * @param maxRating Maximum rating to consider as "low" (default: 2)
     * @param authentication Spring Security authentication object for admin verification
     * @return ResponseEntity containing list of low-rated feedback
     */
    @GetMapping("/admin/low-ratings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<FeedbackResponseDto>>> getLowRatingFeedback(
            @RequestParam(defaultValue = "2") @Min(1) @Max(5) Integer maxRating,
            Authentication authentication) {
        
        log.info("Admin {} retrieving feedback with rating <= {}", authentication.getName(), maxRating);

        try {
            List<FeedbackResponseDto> lowRatingFeedback = feedbackService.getLowRatingFeedback(maxRating);
            
            return ResponseEntity.ok(ApiResponse.<List<FeedbackResponseDto>>builder()
                    .success(true)
                    .message(String.format("Retrieved %d feedback items with rating ≤ %d", 
                            lowRatingFeedback.size(), maxRating))
                    .data(lowRatingFeedback)
                    .build());

        } catch (Exception e) {
            log.error("Error retrieving low rating feedback", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<FeedbackResponseDto>>builder()
                            .success(false)
                            .message("Error retrieving low rating feedback. Please try again later.")
                            .build());
        }
    }

    /**
     * Retrieves all feedback for a specific doctor (administrative view).
     * 
     * This endpoint allows administrators to view all feedback received by
     * a specific doctor, including sensitive administrative data not available
     * to the doctor themselves. Used for performance reviews and investigations.
     * 
     * @param doctorId The ID of the doctor whose feedback is being retrieved
     * @param authentication Spring Security authentication object for admin verification
     * @return ResponseEntity containing comprehensive doctor feedback with admin data
     */
    @GetMapping("/admin/doctor/{doctorId}/feedback")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<FeedbackResponseDto>>> getDoctorFeedbackAdmin(
            @PathVariable Long doctorId,
            Authentication authentication) {
        
        log.info("Admin {} retrieving all feedback for doctor ID: {}", authentication.getName(), doctorId);

        try {
            List<FeedbackResponseDto> doctorFeedback = feedbackService.getDoctorFeedback(doctorId, true);
            
            return ResponseEntity.ok(ApiResponse.<List<FeedbackResponseDto>>builder()
                    .success(true)
                    .message(String.format("Retrieved %d feedback items for doctor", doctorFeedback.size()))
                    .data(doctorFeedback)
                    .build());

        } catch (IllegalArgumentException e) {
            log.warn("Invalid doctor feedback request for ID {}: {}", doctorId, e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.<List<FeedbackResponseDto>>builder()
                    .success(false)
                    .message(e.getMessage())
                    .build());

        } catch (Exception e) {
            log.error("Error retrieving doctor feedback for ID: {}", doctorId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<FeedbackResponseDto>>builder()
                            .success(false)
                            .message("Error retrieving doctor feedback. Please try again later.")
                            .build());
        }
    }

    // ===============================================================================
    // HOSPITAL ANALYTICS AND REPORTING ENDPOINTS
    // ===============================================================================

    /**
     * Generates comprehensive hospital-wide feedback analytics for a specified time period.
     * 
     * This endpoint provides administrators with detailed analytics about patient
     * feedback across the entire hospital system, including performance trends,
     * department comparisons, and key performance indicators for quality improvement.
     * 
     * Analytics Include:
     * - Overall patient satisfaction metrics
     * - Rating distribution and trends
     * - Department and doctor performance comparisons
     * - Category-based feedback analysis
     * - Patient recommendation statistics
     * - Identifies top performers and areas needing attention
     * 
     * @param startDate Start date for analytics period (format: yyyy-MM-dd'T'HH:mm:ss)
     * @param endDate End date for analytics period (format: yyyy-MM-dd'T'HH:mm:ss)
     * @param authentication Spring Security authentication object for admin verification
     * @return ResponseEntity containing comprehensive hospital feedback analytics
     */
    @GetMapping("/admin/analytics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getHospitalAnalytics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Authentication authentication) {
        
        log.info("Admin {} requesting hospital analytics for period: {} to {}", 
                authentication.getName(), startDate, endDate);

        try {
            // Validate date range
            if (startDate.isAfter(endDate)) {
                return ResponseEntity.badRequest().body(ApiResponse.<Map<String, Object>>builder()
                        .success(false)
                        .message("Start date must be before end date")
                        .build());
            }

            Map<String, Object> analytics = feedbackService.getHospitalFeedbackAnalytics(startDate, endDate);
            
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .success(true)
                    .message("Hospital analytics generated successfully")
                    .data(analytics)
                    .build());

        } catch (Exception e) {
            log.error("Error generating hospital analytics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Map<String, Object>>builder()
                            .success(false)
                            .message("Error generating analytics. Please try again later.")
                            .build());
        }
    }

    // ===============================================================================
    // UTILITY AND HELPER METHODS
    // ===============================================================================

    /**
     * Helper method to extract doctor ID from email address.
     * 
     * This method looks up the doctor's database ID using their email address
     * from the authentication context. It includes validation to ensure the
     * user has doctor role privileges.
     * 
     * @param doctorEmail The email address of the doctor
     * @return The database ID of the doctor
     * @throws IllegalArgumentException if doctor not found or invalid role
     */
    private Long getDoctorIdFromEmail(String doctorEmail) {
        try {
            // Use userRepository to look up user by email
            User doctor = userRepository.findByEmail(doctorEmail)
                    .orElseThrow(() -> new IllegalArgumentException("Doctor not found: " + doctorEmail));
            
            // Verify user has DOCTOR role
            if (!doctor.getRoles().contains(Role.ROLE_DOCTOR)) {
                throw new IllegalArgumentException("User is not authorized as a doctor");
            }
            
            return doctor.getId();
            
        } catch (Exception e) {
            log.error("Error retrieving doctor ID for email: {}", doctorEmail, e);
            throw new IllegalArgumentException("Unable to retrieve doctor information", e);
        }
    }

    /**
     * Global exception handler for controller-specific exceptions.
     * 
     * This method handles validation errors and other exceptions that might
     * occur during request processing, providing consistent error responses
     * to clients.
     * 
     * @param e The exception that occurred
     * @return ResponseEntity with appropriate error message and status code
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGlobalException(Exception e) {
        log.error("Unhandled exception in FeedbackController", e);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message("An unexpected error occurred. Please contact support if the problem persists.")
                        .build());
    }
}
