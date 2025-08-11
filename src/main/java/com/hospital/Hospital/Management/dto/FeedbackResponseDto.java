package com.hospital.Hospital.Management.dto;

import com.hospital.Hospital.Management.model.Feedback.FeedbackCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for feedback response data.
 * 
 * This DTO represents feedback information returned to clients, including
 * comprehensive feedback details with related appointment and user information.
 * It provides a complete view of feedback while maintaining appropriate
 * data exposure based on user roles and permissions.
 * 
 * Usage Contexts:
 * - Patient viewing their own feedback history
 * - Doctors viewing feedback they've received
 * - Administrators managing feedback and reviews
 * - Analytics and reporting systems
 * - API responses for feedback-related operations
 * 
 * Security Considerations:
 * - Patient personal information filtered based on viewer permissions
 * - Administrative fields (admin notes, review status) only visible to admins
 * - Doctor information included for transparency and context
 * - Appointment details limited to essential information
 * 
 * @author Hospital Management Team
 * @version 1.0
 * @since 2025-08-05
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackResponseDto {

    /**
     * Unique identifier of the feedback record.
     * Used for feedback management operations and references.
     */
    private Long id;

    /**
     * Patient's rating of the appointment (1-5 stars).
     * 
     * Rating interpretation:
     * - 1-2: Poor satisfaction, may require follow-up
     * - 3: Average satisfaction, room for improvement
     * - 4-5: Good to excellent satisfaction, positive experience
     */
    private Integer rating;

    /**
     * Patient's detailed comment about the appointment experience.
     * Contains specific feedback, suggestions, or concerns.
     * 
     * Content may include:
     * - Specific compliments or areas of excellence
     * - Constructive suggestions for improvement
     * - Detailed description of experience
     * - Specific concerns or issues encountered
     */
    private String comment;

    /**
     * Category of feedback for organizational purposes.
     * Helps classify feedback into specific service areas.
     */
    private FeedbackCategory category;

    /**
     * Whether the patient would recommend this doctor to others.
     * Key metric for Net Promoter Score and doctor reputation.
     */
    private Boolean wouldRecommend;

    /**
     * Indicates if this feedback has been reviewed by hospital administration.
     * Used for workflow management and quality assurance processes.
     */
    private Boolean isReviewed;

    /**
     * Administrative notes added during feedback review process.
     * Only visible to administrators for internal quality management.
     * 
     * Note: This field should be filtered based on user role
     * and only displayed to administrative users.
     */
    private String adminNotes;

    /**
     * Timestamp when the feedback was submitted.
     * Used for chronological ordering and trend analysis.
     */
    private LocalDateTime createdAt;

    /**
     * Timestamp of last feedback update.
     * Tracks when feedback was last modified or reviewed.
     */
    private LocalDateTime updatedAt;

    // ===============================================================================
    // APPOINTMENT INFORMATION
    // ===============================================================================

    /**
     * ID of the appointment for which this feedback was provided.
     * Links feedback to specific appointment for context and validation.
     */
    private Long appointmentId;

    /**
     * Date and time of the appointment.
     * Provides context for when the rated service occurred.
     */
    private LocalDateTime appointmentDateTime;

    /**
     * Status of the appointment when feedback was submitted.
     * Should typically be COMPLETED for valid feedback.
     */
    private String appointmentStatus;

    // ===============================================================================
    // PATIENT INFORMATION
    // ===============================================================================

    /**
     * ID of the patient who submitted the feedback.
     * Used for feedback ownership verification and history tracking.
     */
    private Long patientId;

    /**
     * Name of the patient who submitted the feedback.
     * 
     * Privacy Note: Full name display should be controlled based on
     * viewer permissions and privacy settings. May show initials
     * or limited information for non-administrative users.
     */
    private String patientName;

    /**
     * Email of the patient (for administrative purposes).
     * 
     * Security Note: This field should only be visible to
     * administrative users and filtered for other roles.
     */
    private String patientEmail;

    // ===============================================================================
    // DOCTOR INFORMATION
    // ===============================================================================

    /**
     * ID of the doctor who received this feedback.
     * Used for doctor performance tracking and analysis.
     */
    private Long doctorId;

    /**
     * Name of the doctor who received the feedback.
     * Provides transparency about which healthcare provider was rated.
     */
    private String doctorName;

    /**
     * Specialization of the doctor (e.g., "Cardiology", "General Practice").
     * Provides additional context for feedback interpretation.
     */
    private String doctorSpecialization;

    // ===============================================================================
    // UTILITY METHODS FOR DISPLAY AND ANALYSIS
    // ===============================================================================

    /**
     * Gets a formatted star rating string for display purposes.
     * 
     * @return String representation of rating (e.g., "★★★★☆ (4/5)")
     */
    public String getFormattedRating() {
        if (rating == null) return "No rating";
        
        StringBuilder stars = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            stars.append(i <= rating ? "★" : "☆");
        }
        return String.format("%s (%d/5)", stars.toString(), rating);
    }

    /**
     * Gets the display name for the feedback category.
     * 
     * @return Human-readable category name, or "General" if no category specified
     */
    public String getCategoryDisplayName() {
        return category != null ? category.getDisplayName() : "General";
    }

    /**
     * Determines if this feedback indicates a positive experience.
     * 
     * @return true if rating is 4 or 5 stars, false otherwise
     */
    public boolean isPositiveFeedback() {
        return rating != null && rating >= 4;
    }

    /**
     * Determines if this feedback indicates a negative experience requiring attention.
     * 
     * @return true if rating is 2 stars or below, false otherwise
     */
    public boolean isNegativeFeedback() {
        return rating != null && rating <= 2;
    }

    /**
     * Gets a summary description of the feedback sentiment.
     * 
     * @return String describing feedback sentiment ("Excellent", "Good", "Average", "Poor", "Very Poor")
     */
    public String getSentimentDescription() {
        if (rating == null) return "No rating";
        
        return switch (rating) {
            case 1 -> "Very Poor";
            case 2 -> "Poor";
            case 3 -> "Average";
            case 4 -> "Good";
            case 5 -> "Excellent";
            default -> "Invalid rating";
        };
    }

    /**
     * Checks if feedback has substantial content beyond just rating.
     * 
     * @return true if feedback includes meaningful comment or recommendation
     */
    public boolean hasDetailedFeedback() {
        return (comment != null && !comment.trim().isEmpty()) || wouldRecommend != null;
    }

    /**
     * Gets the recommendation status as a display-friendly string.
     * 
     * @return "Yes", "No", or "Not specified" based on recommendation value
     */
    public String getRecommendationDisplay() {
        if (wouldRecommend == null) return "Not specified";
        return wouldRecommend ? "Yes" : "No";
    }

    /**
     * Generates a brief summary of the feedback for quick overview.
     * 
     * @return String summarizing rating, recommendation, and comment presence
     */
    public String getFeedbackSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append(getFormattedRating());
        
        if (wouldRecommend != null) {
            summary.append(" | Would recommend: ").append(getRecommendationDisplay());
        }
        
        if (comment != null && !comment.trim().isEmpty()) {
            summary.append(" | Has detailed comment");
        }
        
        return summary.toString();
    }
}
