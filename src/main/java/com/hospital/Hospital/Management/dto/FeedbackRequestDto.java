package com.hospital.Hospital.Management.dto;

import com.hospital.Hospital.Management.model.Feedback.FeedbackCategory;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for creating new feedback submissions.
 * 
 * This DTO handles patient feedback submission after completed appointments,
 * including rating, comments, and recommendation data. It includes comprehensive
 * validation to ensure data quality and business rule compliance.
 * 
 * Validation Rules:
 * - Rating must be between 1 and 5 (star rating system)
 * - Appointment ID is required and must be valid
 * - Comments are optional but limited in length
 * - Category selection is optional but must be valid enum value
 * - Recommendation is optional boolean field
 * 
 * Security Considerations:
 * - Patient authentication verified before submission
 * - Appointment ownership validated (patient can only rate their own appointments)
 * - Duplicate feedback prevention (one feedback per appointment per patient)
 * - Content filtering for inappropriate language (handled at service level)
 * 
 * @author Hospital Management Team
 * @version 1.0
 * @since 2025-08-05
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackRequestDto {

    /**
     * The appointment ID for which feedback is being submitted.
     * 
     * Business Rules:
     * - Must correspond to a COMPLETED appointment
     * - Appointment must belong to the authenticated patient
     * - No existing feedback should exist for this appointment
     * - Appointment must have occurred in the past
     * 
     * Validation:
     * - Required field (not null)
     * - Must be positive integer
     */
    @NotNull(message = "Appointment ID is required")
    @Positive(message = "Appointment ID must be a positive number")
    private Long appointmentId;

    /**
     * Patient's rating of the appointment experience (1-5 stars).
     * 
     * Rating Scale:
     * - 1 Star: Very Poor - Extremely unsatisfied, significant problems
     * - 2 Stars: Poor - Unsatisfied, several issues that need addressing
     * - 3 Stars: Average - Acceptable service, some room for improvement
     * - 4 Stars: Good - Satisfied, minor areas for enhancement
     * - 5 Stars: Excellent - Extremely satisfied, exceeds expectations
     * 
     * Validation:
     * - Required field (not null)
     * - Must be integer between 1 and 5 (inclusive)
     */
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1 star")
    @Max(value = 5, message = "Rating must be at most 5 stars")
    private Integer rating;

    /**
     * Optional detailed comment about the appointment experience.
     * 
     * Comment Guidelines:
     * - Detailed feedback about specific aspects of care
     * - Suggestions for improvement
     * - Compliments for exceptional service
     * - Specific concerns or issues experienced
     * - Constructive criticism for quality improvement
     * 
     * Content Policy:
     * - Should be respectful and constructive
     * - No personal attacks or inappropriate language
     * - Focus on professional service aspects
     * - Specific examples preferred over general statements
     * 
     * Validation:
     * - Optional field (can be null or empty)
     * - Maximum length of 2000 characters for detailed feedback
     * - Trimmed to remove leading/trailing whitespace
     */
    @Size(max = 2000, message = "Comment must not exceed 2000 characters")
    private String comment;

    /**
     * Category of feedback for organizational and analytical purposes.
     * 
     * Available Categories:
     * - MEDICAL_CARE: Quality of medical treatment and clinical expertise
     * - COMMUNICATION: Doctor-patient communication and explanation quality
     * - FACILITY: Hospital facilities, cleanliness, and environment
     * - STAFF_SERVICE: Reception, nursing, and support staff interactions
     * - APPOINTMENT_PROCESS: Scheduling, waiting times, and process efficiency
     * - OVERALL_EXPERIENCE: General satisfaction with entire visit
     * 
     * Benefits of Categorization:
     * - Enables targeted improvement initiatives
     * - Facilitates departmental performance tracking
     * - Supports detailed analytics and trend analysis
     * - Helps prioritize quality improvement efforts
     * 
     * Validation:
     * - Optional field (can be null)
     * - Must be valid FeedbackCategory enum value if provided
     */
    private FeedbackCategory category;

    /**
     * Whether the patient would recommend this doctor to others.
     * 
     * Recommendation Intent:
     * - true: Patient would recommend doctor to family/friends
     * - false: Patient would not recommend doctor
     * - null: Patient neutral/undecided about recommendation
     * 
     * Business Value:
     * - Key metric for Net Promoter Score (NPS) calculation
     * - Important indicator of patient loyalty and satisfaction
     * - Used for doctor performance evaluation and recognition
     * - Helps with hospital reputation and patient referral patterns
     * 
     * Validation:
     * - Optional field (can be null)
     * - Boolean value when provided
     */
    private Boolean wouldRecommend;

    /**
     * Validates that at least some substantive feedback is provided.
     * 
     * This ensures that feedback submissions contain meaningful content,
     * either through rating with context (comment) or clear recommendation.
     * 
     * @return true if feedback contains sufficient content, false otherwise
     */
    public boolean hasSubstantiveFeedback() {
        return rating != null && (
            (comment != null && !comment.trim().isEmpty()) ||
            wouldRecommend != null
        );
    }

    /**
     * Determines if this feedback should be flagged for administrative review.
     * 
     * Criteria for Review Flagging:
     * - Rating of 2 stars or below (poor satisfaction)
     * - Contains keywords suggesting problems or complaints
     * - Indicates patient would not recommend doctor
     * - Very detailed negative comments
     * 
     * @return true if feedback should be flagged for review, false otherwise
     */
    public boolean shouldFlagForReview() {
        return rating != null && rating <= 2 ||
               (wouldRecommend != null && !wouldRecommend) ||
               (comment != null && (
                   comment.toLowerCase().contains("complaint") ||
                   comment.toLowerCase().contains("problem") ||
                   comment.toLowerCase().contains("terrible") ||
                   comment.toLowerCase().contains("awful") ||
                   comment.length() > 500 && rating != null && rating <= 3
               ));
    }

    /**
     * Gets the trimmed comment text for storage.
     * 
     * @return trimmed comment text, or null if comment is null or empty
     */
    public String getTrimmedComment() {
        return comment != null && !comment.trim().isEmpty() ? comment.trim() : null;
    }
}
