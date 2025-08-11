# Feedback Collection System Documentation

## Overview

The Feedback Collection System is a comprehensive post-appointment rating and review system designed to collect patient feedback, track doctor performance, and provide administrative oversight for quality improvement in the hospital management system.

## System Architecture

### Core Components

1. **Feedback.java** - Entity model for storing patient feedback
2. **FeedbackRepository.java** - Data access layer with analytics capabilities
3. **FeedbackService.java** - Business logic layer for feedback operations
4. **FeedbackController.java** - REST API endpoints for feedback management
5. **FeedbackRequestDto.java** - Data transfer object for feedback submission
6. **FeedbackResponseDto.java** - Data transfer object for feedback display
7. **DoctorRatingStatsDto.java** - Data transfer object for doctor performance metrics

## Features

### Patient Features
- ✅ Submit feedback for completed appointments
- ✅ Rate doctors on 1-5 star scale
- ✅ Provide optional comments and recommendations
- ✅ Categorize feedback by experience type
- ✅ View personal feedback history
- ✅ Automatic validation and business rule enforcement

### Doctor Features
- ✅ View personal rating statistics and performance metrics
- ✅ Access all received feedback with patient comments
- ✅ Performance level classification (Excellent, Very Good, Good, etc.)
- ✅ Net Promoter Score calculation
- ✅ Rating distribution analysis
- ✅ Patient recommendation statistics

### Administrative Features
- ✅ Review flagged negative feedback
- ✅ Add administrative notes to feedback
- ✅ Monitor low-rating alerts
- ✅ Generate hospital-wide analytics reports
- ✅ Track doctor performance across time periods
- ✅ Identify top performers and areas needing attention

## Database Schema

### Feedback Entity Structure
```sql
feedback (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rating INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    category VARCHAR(50),
    would_recommend BOOLEAN,
    is_reviewed BOOLEAN DEFAULT FALSE,
    admin_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    appointment_id BIGINT NOT NULL,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    
    FOREIGN KEY (appointment_id) REFERENCES appointment(id),
    FOREIGN KEY (patient_id) REFERENCES user(id),
    FOREIGN KEY (doctor_id) REFERENCES user(id),
    
    UNIQUE KEY unique_feedback_per_appointment (appointment_id)
);
```

### Indexes for Performance
```sql
CREATE INDEX idx_feedback_doctor_created ON feedback(doctor_id, created_at);
CREATE INDEX idx_feedback_patient_created ON feedback(patient_id, created_at);
CREATE INDEX idx_feedback_rating ON feedback(rating);
CREATE INDEX idx_feedback_review_status ON feedback(is_reviewed);
CREATE INDEX idx_feedback_category ON feedback(category);
```

## API Endpoints

### Patient Endpoints

#### Submit Feedback
- **POST** `/api/feedback/submit`
- **Authentication**: Required (PATIENT role)
- **Request Body**: FeedbackRequestDto
- **Response**: ApiResponse<FeedbackResponseDto>

```json
{
  "appointmentId": 123,
  "rating": 5,
  "comment": "Excellent care and very professional",
  "category": "OVERALL_EXPERIENCE",
  "wouldRecommend": true
}
```

#### Get My Feedback History
- **GET** `/api/feedback/my-feedback`
- **Authentication**: Required (PATIENT role)
- **Response**: ApiResponse<List<FeedbackResponseDto>>

### Doctor Endpoints

#### Get My Rating Statistics
- **GET** `/api/feedback/my-stats`
- **Authentication**: Required (DOCTOR role)
- **Response**: ApiResponse<DoctorRatingStatsDto>

#### Get My Received Feedback
- **GET** `/api/feedback/my-doctor-feedback`
- **Authentication**: Required (DOCTOR role)
- **Response**: ApiResponse<List<FeedbackResponseDto>>

#### Get Other Doctor Statistics
- **GET** `/api/feedback/doctor/{doctorId}/stats`
- **Authentication**: Required (DOCTOR or ADMIN role)
- **Response**: ApiResponse<DoctorRatingStatsDto>

### Administrative Endpoints

#### Get Feedback Requiring Review
- **GET** `/api/feedback/admin/pending-review`
- **Authentication**: Required (ADMIN role)
- **Response**: ApiResponse<List<FeedbackResponseDto>>

#### Review Feedback
- **PUT** `/api/feedback/admin/{feedbackId}/review`
- **Authentication**: Required (ADMIN role)
- **Request Body**: {"adminNotes": "Follow-up action taken"}
- **Response**: ApiResponse<FeedbackResponseDto>

#### Get Low Rating Feedback
- **GET** `/api/feedback/admin/low-ratings?maxRating=2`
- **Authentication**: Required (ADMIN role)
- **Response**: ApiResponse<List<FeedbackResponseDto>>

#### Get Doctor Feedback (Admin View)
- **GET** `/api/feedback/admin/doctor/{doctorId}/feedback`
- **Authentication**: Required (ADMIN role)
- **Response**: ApiResponse<List<FeedbackResponseDto>>

#### Hospital Analytics
- **GET** `/api/feedback/admin/analytics?startDate=2025-01-01T00:00:00&endDate=2025-12-31T23:59:59`
- **Authentication**: Required (ADMIN role)
- **Response**: ApiResponse<Map<String, Object>>

## Business Rules

### Feedback Submission Rules
1. **Appointment Status**: Only COMPLETED appointments can receive feedback
2. **One Feedback Per Appointment**: Each appointment can only have one feedback submission
3. **Patient Ownership**: Patients can only submit feedback for their own appointments
4. **Time Window**: Feedback must be submitted within 30 days of appointment completion
5. **Rating Range**: Rating must be between 1-5 stars (inclusive)
6. **Comment Length**: Optional comments limited to 1000 characters

### Automatic Review Flagging
Feedback is automatically flagged for administrative review when:
- Rating is ≤ 2 stars
- Comment contains negative sentiment indicators
- Patient explicitly requests follow-up

### Performance Metrics Calculation

#### Doctor Performance Levels
- **EXCELLENT**: Average rating ≥ 4.5, Positive % ≥ 90%, NPS ≥ 70%
- **VERY_GOOD**: Average rating ≥ 4.0, Positive % ≥ 80%, NPS ≥ 50%
- **GOOD**: Average rating ≥ 3.5, Positive % ≥ 70%, NPS ≥ 30%
- **SATISFACTORY**: Average rating ≥ 3.0, Positive % ≥ 60%, NPS ≥ 0%
- **NEEDS_IMPROVEMENT**: Below satisfactory thresholds

#### Net Promoter Score (NPS)
```
NPS = (Recommend Count - Not Recommend Count) / Total Responses × 100
```

#### Rating Categories
- **Positive**: Ratings ≥ 4 stars
- **Neutral**: Rating = 3 stars
- **Negative**: Ratings ≤ 2 stars

## Email Notifications

### Patient Notifications
- **Thank You Email**: Sent automatically after feedback submission
- **Feedback Request**: Sent 24-48 hours after appointment completion

### Administrative Notifications
- **Low Rating Alert**: Sent when feedback with ≤ 2 stars is submitted
- **Weekly Summary**: Performance digest for hospital administrators

## Security Features

### Role-Based Access Control
- **PATIENT**: Can submit and view own feedback only
- **DOCTOR**: Can view own performance statistics and received feedback
- **ADMIN**: Can access all feedback, analytics, and administrative functions

### Data Privacy
- Patient email addresses only visible to administrators
- Sensitive feedback comments protected with appropriate access controls
- Administrative notes restricted to admin users only

### Input Validation
- Rating range validation (1-5)
- Comment length limits and content sanitization
- Appointment ownership verification
- Business rule enforcement at service layer

## Analytics and Reporting

### Doctor Performance Dashboard
- Average rating with star display
- Rating distribution histogram
- Patient recommendation percentage
- Net Promoter Score trending
- Performance level classification
- Feedback volume and reliability indicators

### Hospital-Wide Analytics
- Overall patient satisfaction metrics
- Department performance comparisons
- Category-based feedback analysis
- Trend analysis over time periods
- Top performing doctors identification
- Areas requiring attention alerts

## Configuration

### Email Settings
```properties
# Feedback email configuration
feedback.email.enabled=true
feedback.email.admin-address=admin@hospital.com
feedback.email.thank-you-template=feedback-thank-you
feedback.email.low-rating-template=low-rating-alert
```

### Business Rule Configuration
```properties
# Feedback business rules
feedback.submission.window-days=30
feedback.comment.max-length=1000
feedback.auto-review.rating-threshold=2
feedback.stats.minimum-feedback-count=10
```

## Integration Points

### Appointment System Integration
- Feedback collection triggered by appointment completion
- Appointment data used for context and validation
- Integration with appointment email notifications

### User Management Integration
- User authentication and role verification
- Patient-doctor relationship validation
- User data for personalized communications

### Email Service Integration
- Automated thank you emails to patients
- Administrative alerts for low ratings
- Feedback request reminders

## Performance Considerations

### Database Optimization
- Proper indexing on frequently queried columns
- Efficient queries for analytics calculations
- Pagination for large result sets
- Connection pooling for concurrent operations

### Caching Strategy
- Doctor performance statistics caching
- Frequently accessed feedback lists
- Analytics result caching for dashboard performance

### Scalability Features
- Asynchronous email processing
- Batch operations for bulk analytics
- Efficient query design for large datasets
- Stateless service design for horizontal scaling

## Testing Strategy

### Unit Testing
- Service layer business logic validation
- Repository query testing
- DTO validation and conversion testing
- Utility method verification

### Integration Testing
- API endpoint functionality
- Database transaction integrity
- Email service integration
- Security and authorization testing

### Performance Testing
- Analytics query performance
- Large dataset handling
- Concurrent user scenarios
- Email processing throughput

## Deployment Considerations

### Database Migration
```sql
-- Add feedback table to existing schema
-- Add indexes for performance
-- Populate initial data if needed
-- Verify foreign key constraints
```

### Application Configuration
- Update application.properties with feedback settings
- Configure email templates and settings
- Set up administrative user permissions
- Verify security role configurations

### Monitoring and Alerting
- Monitor feedback submission rates
- Track email delivery success rates
- Alert on unusual feedback patterns
- Monitor system performance metrics

## Future Enhancements

### Planned Features
- Advanced sentiment analysis for comments
- Automated feedback request scheduling
- Mobile app integration for feedback submission
- Multi-language support for international patients
- Voice-to-text feedback capture
- Integration with patient satisfaction surveys

### Analytics Enhancements
- Predictive analytics for patient satisfaction
- Comparative benchmarking with industry standards
- Advanced reporting with data visualization
- Real-time dashboard updates
- Custom report generation

### Integration Opportunities
- Integration with quality management systems
- Patient portal integration
- Hospital information system connectivity
- Third-party analytics platform integration
- Social media sentiment monitoring

---

## Support and Maintenance

For technical support or questions about the Feedback Collection System, please contact the Hospital Management Development Team.

**System Version**: 1.0  
**Last Updated**: January 8, 2025  
**Next Review**: March 8, 2025
