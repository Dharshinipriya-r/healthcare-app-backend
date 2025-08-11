# Test Feedback System - Usage Guide

## Overview

The Test Feedback System allows you to submit feedback without requiring an actual completed appointment. This is perfect for testing the feedback functionality, demonstrations, and development purposes.

## 🚨 Important Note

**This test feedback system is for testing purposes only!** It bypasses normal business validations and should not be used in production for real patient feedback.

## Features

### ✅ **No Appointment Required**
- Submit feedback without booking a real appointment
- System automatically creates test appointments when needed
- Uses existing appointments between patient-doctor combinations when available

### ✅ **Full Feedback Functionality** 
- All normal feedback features work: ratings, comments, categories, recommendations
- Email notifications are sent (thank you emails, low-rating alerts)
- Data is stored in the database like regular feedback
- Doctor statistics are updated

### ✅ **Automatic Test Data Creation**
- Automatically assigns feedback to the first available doctor in the system
- Creates mock completed appointments with proper status
- Reuses existing patient-doctor appointment combinations when possible

## API Endpoints

### Submit Test Feedback
```http
POST /api/feedback/test
Authorization: Bearer <patient-jwt-token>
Content-Type: application/json

{
    "rating": 5,
    "comment": "Excellent service! The doctor was very professional and helpful.",
    "category": "OVERALL_EXPERIENCE", 
    "wouldRecommend": true
}
```

**Roles Required:** PATIENT

**Request Body:**
- `rating` (Integer, 1-5): Star rating for the doctor
- `comment` (String, optional): Detailed feedback comment
- `category` (String): Feedback category (see categories below)
- `wouldRecommend` (Boolean, optional): Whether patient would recommend the doctor

**Response:**
```json
{
    "success": true,
    "message": "Test feedback submitted successfully",
    "data": {
        "id": 123,
        "rating": 5,
        "comment": "Excellent service! The doctor was very professional and helpful.",
        "category": "OVERALL_EXPERIENCE",
        "wouldRecommend": true,
        "isReviewed": false,
        "createdAt": "2025-08-05T14:30:00",
        "appointmentId": 456,
        "appointmentDateTime": "2025-08-04T10:00:00",
        "patientId": 1,
        "patientName": "John Doe",
        "doctorId": 2,
        "doctorName": "Dr. Jane Smith",
        "doctorSpecialization": "General Practice"
    }
}
```

## Feedback Categories

The following categories are available for feedback:

- `OVERALL_EXPERIENCE` - General feedback about the entire visit
- `DOCTOR_PROFESSIONALISM` - Doctor's behavior and professionalism  
- `COMMUNICATION` - How well the doctor communicated
- `WAIT_TIME` - Feedback about waiting time
- `FACILITY_CLEANLINESS` - Hospital facility and cleanliness
- `STAFF_FRIENDLINESS` - Hospital staff behavior
- `APPOINTMENT_SCHEDULING` - Ease of booking appointments
- `FOLLOW_UP_CARE` - Post-appointment care and follow-up

## Testing Scenarios

### 1. **Positive Feedback Test**
```json
{
    "rating": 5,
    "comment": "Amazing doctor! Very thorough examination and clear explanations.",
    "category": "DOCTOR_PROFESSIONALISM",
    "wouldRecommend": true
}
```

### 2. **Negative Feedback Test (Triggers Admin Alert)**
```json
{
    "rating": 2,
    "comment": "Doctor seemed rushed and didn't listen to my concerns properly.",
    "category": "COMMUNICATION", 
    "wouldRecommend": false
}
```

### 3. **Neutral Feedback Test**
```json
{
    "rating": 3,
    "comment": "Service was okay, but room for improvement in wait times.",
    "category": "WAIT_TIME",
    "wouldRecommend": null
}
```

### 4. **Minimal Feedback Test**
```json
{
    "rating": 4,
    "category": "OVERALL_EXPERIENCE"
}
```

## Email Notifications

The test feedback system sends the same email notifications as regular feedback:

### **Thank You Email (All Feedback)**
- Sent to patient's email address
- Confirms feedback submission
- Professional hospital-branded template

### **Low Rating Alert (Rating ≤ 2)**
- Sent to admin@hospital.com  
- Includes patient details and feedback content
- Flags feedback for administrative review

## Database Impact

### **Test Appointments Created**
- Status: `COMPLETED`
- Date: Yesterday (to simulate completed appointment)
- Consultation Notes: "Test appointment for feedback system testing - Virtual consultation"

### **Feedback Records**
- All feedback is stored normally in the database
- Appears in doctor statistics and analytics
- Available through all feedback management endpoints

### **Doctor Statistics**
- Test feedback affects doctor rating calculations
- Included in performance analytics
- Shows up in admin dashboards

## Using with Different Users

### **Patient Users**
Any user with PATIENT role can submit test feedback:
```bash
# Login as patient first
POST /api/auth/login
{
    "email": "patient@test.com",
    "password": "password"
}

# Use returned JWT token for feedback submission
POST /api/feedback/test
Authorization: Bearer <jwt-token>
```

### **Multiple Patients**
Different patients can submit test feedback, and it will be associated with their accounts appropriately.

## Postman Testing

### **Collection Setup**
1. Create environment variable: `baseUrl = http://localhost:8080`
2. Create environment variable: `patientToken = <jwt-token-after-login>`

### **Test Sequence**
1. **Login as Patient**
   ```
   POST {{baseUrl}}/api/auth/login
   ```

2. **Submit Test Feedback**
   ```
   POST {{baseUrl}}/api/feedback/test
   Authorization: Bearer {{patientToken}}
   ```

3. **Check Patient Feedback History**
   ```
   GET {{baseUrl}}/api/feedback/patient/history
   Authorization: Bearer {{patientToken}}
   ```

4. **Admin: View All Feedback** (if admin access available)
   ```
   GET {{baseUrl}}/api/feedback/admin/all
   Authorization: Bearer {{adminToken}}
   ```

## Development Benefits

### **Quick Testing**
- No need to create real appointments for testing
- Instant feedback submission for any patient
- Perfect for API testing and frontend development

### **Demo Purposes**
- Show feedback functionality without complex appointment setup
- Generate sample data for presentations
- Test different feedback scenarios quickly

### **Development Workflow**  
- Test feedback features during development
- Validate email notifications
- Check database integration
- Test error handling

## Cleanup

To clean up test data:

```sql
-- Remove test feedback (optional)
DELETE FROM feedback WHERE appointment_id IN (
    SELECT id FROM appointments WHERE consultation_notes LIKE '%Test appointment for feedback system testing%'
);

-- Remove test appointments (optional)  
DELETE FROM appointments WHERE consultation_notes LIKE '%Test appointment for feedback system testing%';
```

## Security Considerations

- ⚠️ **Test system bypasses business validations**
- ⚠️ **Should not be used for real patient feedback**
- ⚠️ **Consider disabling in production environments**
- ✅ **Still requires proper authentication (PATIENT role)**
- ✅ **Maintains data integrity for test scenarios**

## Support

For issues with the test feedback system:
- Check application logs for detailed error information
- Ensure user has PATIENT role
- Verify at least one DOCTOR exists in the system
- Confirm database connectivity

---

**Version**: 1.0  
**Last Updated**: August 5, 2025  
**Author**: Hospital Management Team
