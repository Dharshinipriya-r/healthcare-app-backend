package com.hospital.Hospital.Management.repository;

import com.hospital.Hospital.Management.model.Feedback;
import com.hospital.Hospital.Management.model.User;
import com.hospital.Hospital.Management.model.Appointment;
import com.hospital.Hospital.Management.model.Feedback.FeedbackCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Feedback entities providing comprehensive data access methods.
 * 
 * This repository extends JpaRepository to provide standard CRUD operations and includes
 * custom query methods for feedback management, analytics, and reporting. It supports
 * various feedback retrieval patterns needed for patient satisfaction tracking,
 * doctor performance evaluation, and administrative oversight.
 * 
 * Key Functionality:
 * - Basic CRUD operations for feedback management
 * - Doctor-specific feedback retrieval and analytics
 * - Patient feedback history and tracking
 * - Administrative feedback management and review
 * - Rating statistics and performance metrics
 * - Feedback filtering by category, rating, and time period
 * 
 * Query Categories:
 * - Doctor Performance: Methods to retrieve and analyze doctor-specific feedback
 * - Patient History: Methods to track patient feedback patterns
 * - Administrative: Methods for feedback review and management
 * - Analytics: Methods for statistical analysis and reporting
 * - Validation: Methods to ensure data integrity and business rules
 * 
 * @author Hospital Management Team
 * @version 1.0
 * @since 2025-08-05
 */
@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    // ===============================================================================
    // DOCTOR PERFORMANCE AND RATING METHODS
    // ===============================================================================

    /**
     * Retrieves all feedback records for a specific doctor.
     * 
     * This method is essential for doctor performance evaluation, allowing
     * administrators and the doctor themselves to view all patient feedback.
     * Used for performance reviews, improvement planning, and recognition.
     * 
     * @param doctor The doctor user entity for whom to retrieve feedback
     * @return List of feedback records for the specified doctor, ordered by creation date (newest first)
     */
    List<Feedback> findByDoctorOrderByCreatedAtDesc(User doctor);

    /**
     * Retrieves feedback for a specific doctor within a date range.
     * 
     * Useful for generating periodic performance reports, quarterly reviews,
     * or analyzing feedback trends over specific time periods.
     * 
     * @param doctor The doctor user entity
     * @param startDate The start of the date range (inclusive)
     * @param endDate The end of the date range (inclusive)
     * @return List of feedback records within the specified date range
     */
    List<Feedback> findByDoctorAndCreatedAtBetweenOrderByCreatedAtDesc(
            User doctor, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Calculates the average rating for a specific doctor.
     * 
     * This method provides a quick way to get a doctor's overall performance
     * rating based on all patient feedback. Essential for performance dashboards,
     * doctor rankings, and quality metrics.
     * 
     * @param doctor The doctor user entity
     * @return Average rating as a Double (null if no feedback exists)
     */
    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.doctor = :doctor")
    Double calculateAverageRatingForDoctor(@Param("doctor") User doctor);

    /**
     * Counts total feedback entries for a specific doctor.
     * 
     * Provides the total number of feedback responses a doctor has received,
     * useful for understanding feedback volume and statistical significance.
     * 
     * @param doctor The doctor user entity
     * @return Total count of feedback entries for the doctor
     */
    Long countByDoctor(User doctor);

    /**
     * Retrieves feedback for a doctor filtered by rating range.
     * 
     * Allows filtering feedback by rating levels, useful for identifying
     * specific areas of excellence (high ratings) or concern (low ratings).
     * 
     * @param doctor The doctor user entity
     * @param minRating Minimum rating (inclusive)
     * @param maxRating Maximum rating (inclusive)
     * @return List of feedback records within the rating range
     */
    List<Feedback> findByDoctorAndRatingBetweenOrderByCreatedAtDesc(
            User doctor, Integer minRating, Integer maxRating);

    /**
     * Retrieves feedback for a doctor by specific category.
     * 
     * Enables targeted analysis of doctor performance in specific areas
     * such as communication, medical care, or overall experience.
     * 
     * @param doctor The doctor user entity
     * @param category The feedback category to filter by
     * @return List of feedback records for the specified category
     */
    List<Feedback> findByDoctorAndCategoryOrderByCreatedAtDesc(User doctor, FeedbackCategory category);

    // ===============================================================================
    // PATIENT FEEDBACK HISTORY METHODS
    // ===============================================================================

    /**
     * Retrieves all feedback submitted by a specific patient.
     * 
     * Allows patients to view their feedback history and enables
     * administrators to understand individual patient satisfaction patterns.
     * 
     * @param patient The patient user entity
     * @return List of feedback records submitted by the patient, ordered by creation date
     */
    List<Feedback> findByPatientOrderByCreatedAtDesc(User patient);

    /**
     * Checks if feedback exists for a specific appointment.
     * 
     * Prevents duplicate feedback submission and helps enforce business
     * rules about one feedback per appointment.
     * 
     * @param appointment The appointment entity
     * @return Optional containing existing feedback if found, empty otherwise
     */
    Optional<Feedback> findByAppointment(Appointment appointment);

    /**
     * Checks if a patient has already submitted feedback for a specific appointment.
     * 
     * Additional validation to ensure feedback integrity and prevent
     * multiple feedback submissions from the same patient for one appointment.
     * 
     * @param patient The patient user entity
     * @param appointment The appointment entity
     * @return Optional containing existing feedback if found, empty otherwise
     */
    Optional<Feedback> findByPatientAndAppointment(User patient, Appointment appointment);

    // ===============================================================================
    // ADMINISTRATIVE AND REVIEW METHODS
    // ===============================================================================

    /**
     * Retrieves all feedback that requires administrative review.
     * 
     * Essential for administrative workflow management, ensuring all
     * feedback receives appropriate attention and follow-up.
     * 
     * @param isReviewed Whether to find reviewed (true) or unreviewed (false) feedback
     * @return List of feedback records matching the review status
     */
    List<Feedback> findByIsReviewedOrderByCreatedAtAsc(Boolean isReviewed);

    /**
     * Retrieves feedback with low ratings that may require attention.
     * 
     * Helps administrators quickly identify negative feedback that may
     * require immediate attention or follow-up actions.
     * 
     * @param maxRating Maximum rating to consider as "low" (typically 2 or 3)
     * @return List of feedback records with ratings at or below the threshold
     */
    List<Feedback> findByRatingLessThanEqualOrderByCreatedAtDesc(Integer maxRating);

    /**
     * Retrieves unreviewed feedback with low ratings for priority attention.
     * 
     * Combines low rating filter with review status to identify feedback
     * requiring urgent administrative attention.
     * 
     * @param maxRating Maximum rating threshold
     * @param isReviewed Review status (typically false for urgent attention)
     * @return List of unreviewed feedback with low ratings
     */
    List<Feedback> findByRatingLessThanEqualAndIsReviewedOrderByCreatedAtDesc(
            Integer maxRating, Boolean isReviewed);

    // ===============================================================================
    // ANALYTICS AND REPORTING METHODS
    // ===============================================================================

    /**
     * Retrieves all feedback within a specific date range for reporting.
     * 
     * Essential for generating periodic reports, analyzing trends over time,
     * and creating administrative dashboards with historical data.
     * 
     * @param startDate The start of the date range (inclusive)
     * @param endDate The end of the date range (inclusive)
     * @return List of feedback records within the specified date range
     */
    List<Feedback> findByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Retrieves feedback by category for categorical analysis.
     * 
     * Enables analysis of feedback trends in specific categories,
     * helping identify systematic issues or areas of excellence.
     * 
     * @param category The feedback category to analyze
     * @return List of feedback records for the specified category
     */
    List<Feedback> findByCategoryOrderByCreatedAtDesc(FeedbackCategory category);

    /**
     * Calculates overall average rating across all feedback.
     * 
     * Provides hospital-wide satisfaction metric for executive dashboards
     * and quality improvement initiatives.
     * 
     * @return Overall average rating across all feedback
     */
    @Query("SELECT AVG(f.rating) FROM Feedback f")
    Double calculateOverallAverageRating();

    /**
     * Counts feedback by rating value for distribution analysis.
     * 
     * Helps understand the distribution of ratings (how many 1-star,
     * 2-star, etc.) for statistical analysis and reporting.
     * 
     * @param rating The specific rating value to count
     * @return Count of feedback records with the specified rating
     */
    Long countByRating(Integer rating);

    /**
     * Retrieves the most recent feedback for quick overview.
     * 
     * Useful for administrative dashboards showing recent patient
     * feedback and satisfaction trends.
     * 
     * @param limit The maximum number of recent feedback records to retrieve
     * @return List of most recent feedback records
     */
    @Query("SELECT f FROM Feedback f ORDER BY f.createdAt DESC LIMIT :limit")
    List<Feedback> findRecentFeedback(@Param("limit") int limit);

    // ===============================================================================
    // RECOMMENDATION AND SATISFACTION METRICS
    // ===============================================================================

    /**
     * Counts feedback where patients would recommend the doctor.
     * 
     * Essential for calculating Net Promoter Score (NPS) and understanding
     * patient loyalty and satisfaction levels.
     * 
     * @param doctor The doctor user entity
     * @param wouldRecommend Recommendation status (true for positive recommendations)
     * @return Count of feedback with the specified recommendation status
     */
    Long countByDoctorAndWouldRecommend(User doctor, Boolean wouldRecommend);

    /**
     * Retrieves feedback with recommendation data for NPS calculation.
     * 
     * Gets all feedback that includes recommendation data (not null)
     * for a specific doctor, used in Net Promoter Score calculations.
     * 
     * @param doctor The doctor user entity
     * @return List of feedback records with non-null recommendation data
     */
    @Query("SELECT f FROM Feedback f WHERE f.doctor = :doctor AND f.wouldRecommend IS NOT NULL")
    List<Feedback> findByDoctorWithRecommendation(@Param("doctor") User doctor);

    // ===============================================================================
    // VALIDATION AND BUSINESS RULE METHODS
    // ===============================================================================

    /**
     * Checks if feedback exists for a completed appointment.
     * 
     * Used to enforce business rules about feedback submission only
     * being allowed for completed appointments.
     * 
     * @param appointmentId The appointment ID to check
     * @return True if feedback exists for the appointment, false otherwise
     */
    @Query("SELECT COUNT(f) > 0 FROM Feedback f WHERE f.appointment.id = :appointmentId")
    Boolean existsByAppointmentId(@Param("appointmentId") Long appointmentId);

    /**
     * Custom query to find feedback requiring follow-up.
     * 
     * Identifies feedback that may require administrative follow-up based
     * on low ratings, specific keywords in comments, or other criteria.
     * 
     * @return List of feedback records requiring follow-up attention
     */
    @Query("SELECT f FROM Feedback f WHERE f.rating <= 2 OR f.comment LIKE '%complaint%' OR f.comment LIKE '%problem%'")
    List<Feedback> findFeedbackRequiringFollowUp();
}
