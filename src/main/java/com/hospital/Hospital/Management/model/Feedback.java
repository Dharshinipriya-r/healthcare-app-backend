package com.hospital.Hospital.Management.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Feedback entity representing patient feedback and ratings for completed appointments.
 * 
 * This entity captures patient feedback after appointments, allowing the hospital to:
 * - Collect patient satisfaction ratings (1-5 stars)
 * - Gather detailed feedback comments and suggestions
 * - Track feedback per doctor for performance evaluation
 * - Link feedback to specific appointments for context
 * - Monitor patient experience and service quality
 * 
 * Key Features:
 * - Star rating system (1-5 scale) for quantitative feedback
 * - Text comments for qualitative feedback and detailed suggestions
 * - Direct relationship to Appointment for traceability
 * - Patient and Doctor references for comprehensive feedback tracking
 * - Timestamp tracking for feedback submission and updates
 * - Support for feedback categorization (service, treatment, facilities)
 * 
 * Business Use Cases:
 * - Doctor performance evaluation and improvement
 * - Hospital service quality monitoring
 * - Patient satisfaction reporting and analytics
 * - Administrative oversight of patient experience
 * - Quality improvement initiatives based on feedback trends
 * 
 * Database Relationships:
 * - Many-to-One with Appointment (one appointment can have one feedback)
 * - Many-to-One with User (Patient who provided feedback)
 * - Many-to-One with User (Doctor who received feedback)
 * 
 * @author Hospital Management Team
 * @version 1.0
 * @since 2025-08-05
 */
@Entity
@Table(name = "feedbacks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feedback {

    /**
     * Unique identifier for the feedback record.
     * Auto-generated primary key for database operations.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Rating score provided by the patient (1-5 stars).
     * 
     * Rating Scale:
     * - 1 Star: Very Poor - Extremely unsatisfied
     * - 2 Stars: Poor - Unsatisfied with significant issues
     * - 3 Stars: Average - Acceptable but room for improvement
     * - 4 Stars: Good - Satisfied with minor areas for improvement
     * - 5 Stars: Excellent - Extremely satisfied, exceeds expectations
     * 
     * Validation: Must be between 1 and 5 (inclusive)
     * Database: Stored as INTEGER, indexed for performance
     */
    @Column(name = "rating", nullable = false)
    private Integer rating;

    /**
     * Detailed text comment provided by the patient.
     * 
     * This field captures:
     * - Detailed feedback about the appointment experience
     * - Specific compliments or complaints
     * - Suggestions for service improvement
     * - Comments about doctor's bedside manner
     * - Feedback about hospital facilities and staff
     * - Any other relevant patient observations
     * 
     * Features:
     * - Optional field (can be null for rating-only feedback)
     * - TEXT type for extended comments (up to 65,535 characters)
     * - Supports multilingual feedback
     */
    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    /**
     * Category of feedback for better organization and analysis.
     * 
     * Categories help classify feedback into different areas:
     * - MEDICAL_CARE: Feedback about treatment quality and medical expertise
     * - COMMUNICATION: Doctor-patient communication and bedside manner
     * - FACILITY: Hospital facilities, cleanliness, and environment
     * - STAFF_SERVICE: Reception, nursing, and support staff service
     * - APPOINTMENT_PROCESS: Scheduling, waiting times, and appointment flow
     * - OVERALL_EXPERIENCE: General satisfaction with entire visit
     * 
     * Benefits:
     * - Enables targeted improvement initiatives
     * - Facilitates departmental performance tracking
     * - Supports detailed analytics and reporting
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private FeedbackCategory category;

    /**
     * Reference to the appointment for which this feedback was provided.
     * 
     * Relationship Details:
     * - Many-to-One: Multiple feedback entries can theoretically link to one appointment
     * - Lazy Loading: Appointment details loaded only when accessed
     * - Foreign Key: appointment_id in feedbacks table
     * - Cascade: No cascading operations to prevent accidental data loss
     * 
     * Business Rules:
     * - Feedback can only be submitted for COMPLETED appointments
     * - One feedback per appointment per patient (enforced at service level)
     * - Appointment must exist and belong to the patient providing feedback
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    /**
     * Reference to the patient who provided this feedback.
     * 
     * Relationship Details:
     * - Many-to-One: One patient can provide multiple feedback entries
     * - Lazy Loading: Patient details loaded only when accessed
     * - Foreign Key: patient_id in feedbacks table
     * 
     * Security and Privacy:
     * - Patient identity for feedback ownership verification
     * - Used for feedback retrieval and patient history
     * - Supports anonymous feedback options if configured
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    /**
     * Reference to the doctor who received this feedback.
     * 
     * Relationship Details:
     * - Many-to-One: One doctor can receive multiple feedback entries
     * - Lazy Loading: Doctor details loaded only when accessed
     * - Foreign Key: doctor_id in feedbacks table
     * 
     * Performance Tracking:
     * - Enables doctor-specific feedback analysis
     * - Supports performance evaluation and improvement
     * - Facilitates doctor rating calculations and comparisons
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private User doctor;

    /**
     * Indicates whether the patient would recommend this doctor to others.
     * 
     * This boolean field captures recommendation intent:
     * - true: Patient would recommend the doctor to family/friends
     * - false: Patient would not recommend the doctor
     * - null: Patient neutral or undecided about recommendation
     * 
     * Business Value:
     * - Key metric for doctor performance evaluation
     * - Important for hospital reputation and patient referrals
     * - Used in calculating Net Promoter Score (NPS) for doctors
     */
    @Column(name = "would_recommend")
    private Boolean wouldRecommend;

    /**
     * Administrative flag indicating if this feedback has been reviewed by hospital staff.
     * 
     * Review Process:
     * - false: Feedback submitted but not yet reviewed by administrators
     * - true: Feedback has been reviewed and any necessary actions taken
     * 
     * Use Cases:
     * - Quality assurance and follow-up on negative feedback
     * - Recognition of positive feedback and excellent service
     * - Administrative workflow management
     * - Compliance with feedback response policies
     */
    @Column(name = "is_reviewed", nullable = false)
    @Builder.Default
    private Boolean isReviewed = false;

    /**
     * Administrative notes added by hospital staff during feedback review.
     * 
     * This field stores:
     * - Actions taken in response to feedback
     * - Notes for follow-up with patient or doctor
     * - Resolution details for complaints
     * - Recognition notes for positive feedback
     * - Internal comments for quality improvement
     * 
     * Access Control:
     * - Only accessible to hospital administrators
     * - Not visible to patients or doctors
     * - Used for internal quality management processes
     */
    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;

    /**
     * Timestamp when the feedback was initially created.
     * Automatically set when the feedback record is first saved to the database.
     * 
     * Used for:
     * - Tracking feedback submission timing
     * - Analyzing feedback patterns over time
     * - Sorting feedback by submission date
     * - Audit trails and historical analysis
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the feedback was last updated.
     * Automatically updated whenever the feedback record is modified.
     * 
     * Used for:
     * - Tracking when feedback was last reviewed or updated
     * - Audit trails for feedback modifications
     * - Administrative workflow management
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Validation method to ensure rating is within acceptable range.
     * Called before persisting or updating the feedback entity.
     * 
     * @throws IllegalArgumentException if rating is not between 1 and 5
     */
    @PrePersist
    @PreUpdate
    private void validateRating() {
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5 (inclusive)");
        }
    }

    /**
     * Enumeration defining feedback categories for better organization and analysis.
     * 
     * Categories help classify feedback into specific areas of hospital operations,
     * enabling targeted improvement initiatives and detailed performance tracking.
     */
    public enum FeedbackCategory {
        /**
         * Feedback about medical treatment quality, diagnosis accuracy, and clinical care.
         */
        MEDICAL_CARE("Medical Care"),

        /**
         * Feedback about doctor-patient communication, bedside manner, and explanation quality.
         */
        COMMUNICATION("Communication"),

        /**
         * Feedback about hospital facilities, cleanliness, comfort, and physical environment.
         */
        FACILITY("Facility"),

        /**
         * Feedback about reception, nursing, and support staff service quality.
         */
        STAFF_SERVICE("Staff Service"),

        /**
         * Feedback about appointment scheduling, waiting times, and process efficiency.
         */
        APPOINTMENT_PROCESS("Appointment Process"),

        /**
         * General feedback about overall patient experience and satisfaction.
         */
        OVERALL_EXPERIENCE("Overall Experience");

        private final String displayName;

        FeedbackCategory(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
