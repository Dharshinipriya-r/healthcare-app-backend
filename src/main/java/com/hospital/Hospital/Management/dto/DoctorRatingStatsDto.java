package com.hospital.Hospital.Management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Data Transfer Object for doctor rating statistics and performance metrics.
 * 
 * This DTO aggregates comprehensive performance data for individual doctors
 * based on patient feedback. It provides statistical analysis and metrics
 * essential for performance evaluation, quality improvement, and recognition
 * programs within the hospital management system.
 * 
 * Key Metrics Included:
 * - Overall rating statistics (average, total count, distribution)
 * - Net Promoter Score (NPS) based on patient recommendations
 * - Rating distribution across all star levels (1-5)
 * - Recommendation statistics and patient loyalty indicators
 * - Performance trends and comparative analysis data
 * 
 * Usage Contexts:
 * - Doctor performance dashboards and reports
 * - Administrative quality review and assessment
 * - Recognition and improvement program identification
 * - Patient portal doctor selection and information
 * - Hospital-wide quality metrics and benchmarking
 * 
 * @author Hospital Management Team
 * @version 1.0
 * @since 2025-08-05
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorRatingStatsDto {

    // ===============================================================================
    // DOCTOR IDENTIFICATION
    // ===============================================================================

    /**
     * Unique identifier of the doctor.
     * Links statistics to specific healthcare provider.
     */
    private Long doctorId;

    /**
     * Full name of the doctor.
     * Used for display in reports and dashboards.
     */
    private String doctorName;

    /**
     * Medical specialization of the doctor.
     * Provides context for performance analysis within specialty groups.
     */
    private String specialization;

    // ===============================================================================
    // OVERALL RATING STATISTICS
    // ===============================================================================

    /**
     * Average rating across all patient feedback.
     * 
     * Scale: 1.0 to 5.0 (decimal precision for accurate averaging)
     * - 4.5-5.0: Excellent performance, exceeds expectations
     * - 4.0-4.4: Good performance, meets expectations well
     * - 3.5-3.9: Satisfactory performance, room for improvement
     * - 3.0-3.4: Below average, requires attention
     * - Below 3.0: Poor performance, immediate improvement needed
     */
    private Double averageRating;

    /**
     * Total number of feedback submissions received.
     * 
     * Statistical significance indicators:
     * - 1-10: Limited data, interpret with caution
     * - 11-30: Moderate sample size, generally reliable
     * - 31-100: Good sample size, statistically significant
     * - 100+: Excellent sample size, highly reliable metrics
     */
    private Long totalFeedbackCount;

    /**
     * Distribution of ratings across all star levels.
     * 
     * Map structure:
     * - Key: Rating value (1, 2, 3, 4, 5)
     * - Value: Count of feedback with that rating
     * 
     * Example: {1=2, 2=1, 3=5, 4=15, 5=12} means:
     * - 2 patients gave 1 star, 1 gave 2 stars, etc.
     * 
     * Used for:
     * - Visual representation in charts and graphs
     * - Understanding rating distribution patterns
     * - Identifying performance consistency or variability
     */
    private Map<Integer, Long> ratingDistribution;

    // ===============================================================================
    // RECOMMENDATION STATISTICS (NET PROMOTER SCORE)
    // ===============================================================================

    /**
     * Number of patients who would recommend this doctor.
     * Used for calculating patient loyalty and satisfaction levels.
     */
    private Long recommendCount;

    /**
     * Number of patients who would NOT recommend this doctor.
     * Indicates areas requiring attention and improvement.
     */
    private Long notRecommendCount;

    /**
     * Number of patients who were neutral/undecided about recommendation.
     * Represents patients who didn't provide recommendation data.
     */
    private Long neutralCount;

    /**
     * Net Promoter Score calculated from recommendation data.
     * 
     * NPS Calculation:
     * NPS = (Recommend Count - Not Recommend Count) / Total Responses × 100
     * 
     * NPS Interpretation:
     * - 70 to 100: Excellent (patients are loyal promoters)
     * - 50 to 69: Good (generally positive patient sentiment)
     * - 30 to 49: Average (mixed patient sentiment)
     * - 0 to 29: Poor (more detractors than promoters)
     * - Below 0: Critical (significant patient satisfaction issues)
     * 
     * Note: null if insufficient recommendation data available
     */
    private Double netPromoterScore;

    // ===============================================================================
    // PERFORMANCE INDICATORS
    // ===============================================================================

    /**
     * Percentage of ratings that are 4 or 5 stars (positive feedback).
     * 
     * Calculation: (4-star count + 5-star count) / total feedback × 100
     * 
     * Benchmarks:
     * - 90%+: Exceptional performance
     * - 80-89%: Very good performance
     * - 70-79%: Good performance
     * - 60-69%: Satisfactory performance
     * - Below 60%: Needs improvement
     */
    private Double positiveRatingPercentage;

    /**
     * Percentage of ratings that are 1 or 2 stars (negative feedback).
     * 
     * Calculation: (1-star count + 2-star count) / total feedback × 100
     * 
     * Attention thresholds:
     * - 15%+: Requires immediate attention
     * - 10-14%: Monitor closely, investigate issues
     * - 5-9%: Normal range, continue monitoring
     * - Below 5%: Excellent patient satisfaction
     */
    private Double negativeRatingPercentage;

    /**
     * Star rating for display purposes (1-5 whole stars based on average).
     * 
     * Calculation: Rounds average rating to nearest whole number
     * Used for visual star displays in user interfaces.
     */
    private Integer displayStars;

    // ===============================================================================
    // PERFORMANCE CLASSIFICATION
    // ===============================================================================

    /**
     * Performance level classification based on overall metrics.
     * 
     * Classifications:
     * - EXCELLENT: Average ≥ 4.5, Positive % ≥ 90%, NPS ≥ 70
     * - VERY_GOOD: Average ≥ 4.0, Positive % ≥ 80%, NPS ≥ 50
     * - GOOD: Average ≥ 3.5, Positive % ≥ 70%, NPS ≥ 30
     * - SATISFACTORY: Average ≥ 3.0, Positive % ≥ 60%, NPS ≥ 0
     * - NEEDS_IMPROVEMENT: Below satisfactory thresholds
     * 
     * Used for:
     * - Performance dashboards and color coding
     * - Recognition program eligibility
     * - Improvement program targeting
     * - Administrative reporting and alerts
     */
    private PerformanceLevel performanceLevel;

    /**
     * Indicates whether this doctor has sufficient feedback for reliable statistics.
     * 
     * Criteria for statistical reliability:
     * - Minimum 10 feedback submissions
     * - Feedback received within reasonable time period
     * - Balanced mix of different appointment types/patients
     * 
     * Used to determine if statistics should be displayed publicly
     * or used for administrative decisions.
     */
    private Boolean hasReliableStats;

    // ===============================================================================
    // UTILITY METHODS FOR ANALYSIS AND DISPLAY
    // ===============================================================================

    /**
     * Gets formatted average rating for display (e.g., "4.3/5.0").
     * 
     * @return Formatted rating string, or "No data" if no feedback exists
     */
    public String getFormattedAverageRating() {
        if (averageRating == null) return "No data";
        return String.format("%.1f/5.0", averageRating);
    }

    /**
     * Gets star representation of average rating for visual display.
     * 
     * @return String with filled and empty stars (e.g., "★★★★☆")
     */
    public String getStarDisplay() {
        if (displayStars == null) return "☆☆☆☆☆";
        
        StringBuilder stars = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            stars.append(i <= displayStars ? "★" : "☆");
        }
        return stars.toString();
    }

    /**
     * Gets formatted Net Promoter Score for display.
     * 
     * @return Formatted NPS string (e.g., "+65 NPS"), or "Insufficient data" if null
     */
    public String getFormattedNPS() {
        if (netPromoterScore == null) return "Insufficient data";
        return String.format("%+.0f NPS", netPromoterScore);
    }

    /**
     * Determines if performance metrics indicate excellence worthy of recognition.
     * 
     * @return true if doctor meets excellence criteria, false otherwise
     */
    public boolean isExcellentPerformance() {
        return performanceLevel == PerformanceLevel.EXCELLENT;
    }

    /**
     * Determines if performance metrics indicate need for improvement support.
     * 
     * @return true if doctor needs improvement support, false otherwise
     */
    public boolean needsImprovementSupport() {
        return performanceLevel == PerformanceLevel.NEEDS_IMPROVEMENT;
    }

    /**
     * Gets percentage of patients who would recommend this doctor.
     * 
     * @return Recommendation percentage, or null if no recommendation data
     */
    public Double getRecommendationPercentage() {
        if (recommendCount == null || notRecommendCount == null) return null;
        
        long totalRecommendationResponses = recommendCount + notRecommendCount;
        if (totalRecommendationResponses == 0) return null;
        
        return (double) recommendCount / totalRecommendationResponses * 100;
    }

    /**
     * Performance level enumeration for categorizing doctor performance.
     */
    public enum PerformanceLevel {
        EXCELLENT("Excellent"),
        VERY_GOOD("Very Good"),
        GOOD("Good"),
        SATISFACTORY("Satisfactory"),
        NEEDS_IMPROVEMENT("Needs Improvement");

        private final String displayName;

        PerformanceLevel(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
