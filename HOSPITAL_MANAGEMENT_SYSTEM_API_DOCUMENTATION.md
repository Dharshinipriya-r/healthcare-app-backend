# Hospital Management System - Complete API Documentation

## 📋 Table of Contents
1. [System Overview](#system-overview)
2. [Technology Stack](#technology-stack)
3. [Architecture Overview](#architecture-overview)
4. [Authentication & Security](#authentication--security)
5. [API Endpoints](#api-endpoints)
6. [Data Models](#data-models)
7. [Sample cURL Requests](#sample-curl-requests)
8. [Error Handling](#error-handling)
9. [Setup & Configuration](#setup--configuration)
10. [Testing Guide](#testing-guide)

---

## System Overview

The Hospital Management System is a comprehensive web application built with Spring Boot that facilitates efficient management of hospital operations including patient appointments, doctor schedules, feedback collection, and automated reminder systems.

### Key Features
- **Patient Management**: Registration, appointment booking, medical history
- **Doctor Management**: Profile management, availability setting, appointment handling
- **Appointment System**: Scheduling, reminders, status tracking
- **Feedback System**: Patient feedback collection and doctor rating
- **Admin Dashboard**: User management, system monitoring, analytics
- **Email System**: Automated appointment reminders and notifications
- **Security**: JWT-based authentication with role-based access control

---

## Technology Stack

### Backend
- **Framework**: Spring Boot 3.5.3
- **Security**: Spring Security with JWT Authentication
- **Database**: MySQL 8.0+ with JPA/Hibernate
- **Email**: Spring Mail with Gmail SMTP
- **Build Tool**: Maven
- **Java Version**: 19

### Dependencies
```xml
<!-- Core Spring Boot Starters -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>

<!-- JWT Authentication -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>

<!-- Database -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
</dependency>

<!-- Utilities -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
</dependency>
```

---

## Architecture Overview

### System Architecture
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   Spring Boot   │    │   MySQL         │
│   (React/JS)    │◄──►│   Backend       │◄──►│   Database      │
│                 │    │                 │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                              │
                              ▼
                       ┌─────────────────┐
                       │   Email Service │
                       │   (Gmail SMTP)  │
                       └─────────────────┘
```

### Module Structure
```
src/main/java/com/hospital/Hospital/Management/
├── config/           # Configuration classes
├── controller/       # REST Controllers
├── dto/             # Data Transfer Objects
├── exception/       # Exception handling
├── model/           # JPA Entities
├── repository/      # JPA Repositories
├── scheduler/       # Scheduled tasks
├── security/        # Security configuration
└── service/         # Business logic
```

### Key Modules
1. **Authentication Module**: User registration, login, JWT management
2. **Appointment Module**: Booking, scheduling, status management
3. **Doctor Management Module**: Profile, availability, appointment handling
4. **Feedback Module**: Patient feedback collection and analytics
5. **Admin Module**: User management, system monitoring
6. **Email Module**: Automated reminders and notifications
7. **Scheduler Module**: Automated background tasks

---

## Authentication & Security

### JWT Authentication Flow
```
1. User Login → 2. JWT Token Generated → 3. Token in Header → 4. Access Granted
   POST /api/auth/authenticate → Bearer eyJhbGc... → Authorization: Bearer <token>
```

### Role-Based Access Control
- **PATIENT**: Book appointments, view own data, submit feedback
- **DOCTOR**: Manage schedule, handle appointments, view feedback
- **ADMIN**: Full system access, user management, system monitoring

### Security Configuration
```java
@EnableMethodSecurity(prePostEnabled = true)
@PreAuthorize("hasRole('PATIENT')")
@PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
@PreAuthorize("hasRole('ADMIN')")
```

---

## API Endpoints

### 🔐 Authentication Endpoints

#### Register User
```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "patient@example.com",
  "password": "SecurePass123",
  "fullName": "John Doe",
  "roles": ["ROLE_PATIENT"]
}
```

#### Login
```http
POST /api/auth/authenticate
Content-Type: application/json

{
  "email": "patient@example.com",
  "password": "SecurePass123"
}
```

#### Verify Email
```http
GET /api/auth/verify-email?token={verification_token}
```

#### Forgot Password
```http
POST /api/auth/forgot-password?email=patient@example.com
```

#### Reset Password
```http
POST /api/auth/reset-password
Content-Type: application/json

{
  "token": "reset_token_here",
  "newPassword": "NewSecurePass123"
}
```

---

### 👥 Patient Endpoints

#### Get Available Doctors
```http
GET /api/appointments/doctors
Authorization: Bearer {patient_token}
```

#### Book Appointment
```http
POST /api/appointments/book
Authorization: Bearer {patient_token}
Content-Type: application/json

{
  "doctorId": 2,
  "appointmentDateTime": "2025-08-10T14:30:00"
}
```

#### Get My Appointments
```http
GET /api/appointments/my-appointments
Authorization: Bearer {patient_token}
```

#### Get Upcoming Appointments
```http
GET /api/appointments/upcoming
Authorization: Bearer {patient_token}
```

#### Cancel Appointment
```http
PUT /api/appointments/{appointmentId}/cancel
Authorization: Bearer {patient_token}
```

#### Submit Feedback
```http
POST /api/feedback/submit
Authorization: Bearer {patient_token}
Content-Type: application/json

{
  "appointmentId": 123,
  "rating": 5,
  "comment": "Excellent care and very professional",
  "category": "OVERALL_EXPERIENCE",
  "wouldRecommend": true
}
```

#### Get My Feedback History
```http
GET /api/feedback/my-feedback
Authorization: Bearer {patient_token}
```

#### Patient Profile
```http
GET /api/patient/profile
Authorization: Bearer {patient_token}
```

#### Patient Dashboard
```http
GET /api/patient/dashboard
Authorization: Bearer {patient_token}
```

---

### 👨‍⚕️ Doctor Endpoints

#### Update Doctor Profile
```http
PUT /api/doctors/{doctorId}/profile
Authorization: Bearer {doctor_token}
Content-Type: application/json

{
  "specialization": "Cardiology",
  "location": "Building A, Floor 2",
  "contactInfo": "Dr. Smith - Cardiology Specialist"
}
```

#### Set Weekly Availability
```http
PUT /api/doctors/{doctorId}/availability
Authorization: Bearer {doctor_token}
Content-Type: application/json

{
  "availabilities": [
    {
      "dayOfWeek": "MONDAY",
      "startTime": "09:00",
      "endTime": "17:00"
    },
    {
      "dayOfWeek": "TUESDAY",
      "startTime": "09:00",
      "endTime": "17:00"
    }
  ]
}
```

#### Get Upcoming Appointments
```http
GET /api/doctors/{doctorId}/appointments/upcoming
Authorization: Bearer {doctor_token}
```

#### Confirm Appointment
```http
PUT /api/doctors/{doctorId}/appointments/{appointmentId}/confirm
Authorization: Bearer {doctor_token}
```

#### Decline Appointment
```http
PUT /api/doctors/{doctorId}/appointments/{appointmentId}/decline
Authorization: Bearer {doctor_token}
```

#### Complete Appointment
```http
PUT /api/doctors/{doctorId}/appointments/{appointmentId}/complete
Authorization: Bearer {doctor_token}
```

#### Get Appointment History
```http
GET /api/doctors/{doctorId}/appointments/history?patientId={patientId}
Authorization: Bearer {doctor_token}
```

#### Add Consultation Notes
```http
POST /api/doctors/{doctorId}/appointments/{appointmentId}/notes
Authorization: Bearer {doctor_token}
Content-Type: application/json

{
  "diagnosis": "Mild hypertension",
  "prescription": "Lisinopril 10mg daily",
  "treatmentDetails": "Follow up in 3 months",
  "remarks": "Patient responded well to treatment"
}
```

#### Get My Rating Statistics
```http
GET /api/feedback/my-stats
Authorization: Bearer {doctor_token}
```

#### Get My Received Feedback
```http
GET /api/feedback/my-doctor-feedback
Authorization: Bearer {doctor_token}
```

#### Search Doctors
```http
GET /api/doctors/search?specialization=Cardiology&location=Building A
Authorization: Bearer {any_token}
```

#### Doctor Profile
```http
GET /api/doctor/profile
Authorization: Bearer {doctor_token}
```

#### Doctor Dashboard
```http
GET /api/doctor/dashboard
Authorization: Bearer {doctor_token}
```

#### Send Individual Appointment Reminder
```http
POST /api/appointments/{appointmentId}/send-reminder
Authorization: Bearer {doctor_token}
```

---

### 👨‍💼 Admin Endpoints

#### Get All Users
```http
GET /api/admin/users
Authorization: Bearer {admin_token}
```

#### Add User
```http
POST /api/admin/users
Authorization: Bearer {admin_token}
Content-Type: application/json

{
  "email": "newdoctor@example.com",
  "password": "SecurePass123",
  "fullName": "Dr. Jane Smith",
  "roles": ["ROLE_DOCTOR"]
}
```

#### Block User
```http
POST /api/admin/users/{userId}/block
Authorization: Bearer {admin_token}
```

#### Unblock User
```http
POST /api/admin/users/{userId}/unblock
Authorization: Bearer {admin_token}
```

#### Get All Doctors
```http
GET /api/admin/doctors
Authorization: Bearer {admin_token}
```

#### Get Doctor Schedule
```http
GET /api/admin/doctors/{doctorId}/schedule
Authorization: Bearer {admin_token}
```

#### Get System Logs
```http
GET /api/admin/logs
Authorization: Bearer {admin_token}
```

#### Get Dashboard Analytics
```http
GET /api/admin/analytics
Authorization: Bearer {admin_token}
```

#### Send System Announcement
```http
POST /api/admin/announcements
Authorization: Bearer {admin_token}
Content-Type: application/json

{
  "title": "System Maintenance",
  "message": "The system will be down for maintenance on Sunday.",
  "priority": "HIGH",
  "targetRoles": ["ROLE_DOCTOR", "ROLE_PATIENT"]
}
```

#### Get All Upcoming Appointments
```http
GET /api/appointments/upcoming/all
Authorization: Bearer {admin_token}
```

#### Trigger Batch Appointment Reminders
```http
POST /api/appointments/send-reminders
Authorization: Bearer {admin_token}
```

#### Manual Daily Reminder Trigger
```http
POST /api/admin/reminders/trigger-daily
Authorization: Bearer {admin_token}
```

#### Send Individual Reminder
```http
POST /api/admin/reminders/send-individual/{appointmentId}
Authorization: Bearer {admin_token}
```

#### Get Reminder System Status
```http
GET /api/admin/reminders/status
Authorization: Bearer {admin_token}
```

#### Send Test Email
```http
POST /api/admin/reminders/test-email/{email}
Authorization: Bearer {admin_token}
```

#### Get Upcoming Appointment Count
```http
GET /api/admin/reminders/upcoming-count
Authorization: Bearer {admin_token}
```

#### Get Feedback Requiring Review
```http
GET /api/feedback/admin/pending-review
Authorization: Bearer {admin_token}
```

#### Review Feedback
```http
PUT /api/feedback/admin/{feedbackId}/review
Authorization: Bearer {admin_token}
Content-Type: application/json

{
  "adminNotes": "Followed up with patient, issue resolved"
}
```

#### Get Low Rating Feedback
```http
GET /api/feedback/admin/low-ratings?maxRating=2
Authorization: Bearer {admin_token}
```

#### Get Doctor Feedback (Admin View)
```http
GET /api/feedback/admin/doctor/{doctorId}/feedback
Authorization: Bearer {admin_token}
```

#### Hospital Analytics
```http
GET /api/feedback/admin/analytics?startDate=2025-01-01T00:00:00&endDate=2025-12-31T23:59:59
Authorization: Bearer {admin_token}
```

#### Admin Profile
```http
GET /api/admin/profile
Authorization: Bearer {admin_token}
```

#### Admin Dashboard
```http
GET /api/admin/dashboard
Authorization: Bearer {admin_token}
```

---

## Data Models

### User Entity
```java
@Entity
@Table(name = "users")
public class User {
    private Long id;
    private String email;
    private String password;
    private String fullName;
    private Set<Role> roles;
    private boolean enabled;
    private boolean accountNonLocked;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String specialization;  // For doctors
    private String location;        // For doctors
    private Double rating;          // For doctors
    private List<DoctorAvailability> availabilities;
}
```

### Appointment Entity
```java
@Entity
@Table(name = "appointments")
public class Appointment {
    private Long id;
    private User patient;
    private User doctor;
    private LocalDateTime appointmentDateTime;
    private AppointmentStatus status;
    private String consultationNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### AppointmentStatus Enum
```java
public enum AppointmentStatus {
    SCHEDULED,              // Initial state
    CONFIRMED_BY_DOCTOR,    // Doctor confirmed
    COMPLETED,              // Consultation done
    CANCELLED_BY_PATIENT,   // Patient cancelled
    CANCELLED_BY_DOCTOR,    // Doctor cancelled
    NO_SHOW                 // Patient didn't show up
}
```

### DoctorAvailability Entity
```java
@Entity
@Table(name = "doctor_availability")
public class DoctorAvailability {
    private Long id;
    private User doctor;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
}
```

### Feedback Entity
```java
@Entity
@Table(name = "feedback")
public class Feedback {
    private Long id;
    private User patient;
    private User doctor;
    private Appointment appointment;
    private Integer rating;
    private String comment;
    private FeedbackCategory category;
    private Boolean wouldRecommend;
    private LocalDateTime submittedAt;
    private String adminNotes;
    private LocalDateTime reviewedAt;
}
```

---

## Sample cURL Requests

### Complete User Journey Example

#### 1. Register as Patient
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "SecurePass123",
    "fullName": "John Doe",
    "roles": ["ROLE_PATIENT"]
  }'
```

#### 2. Login and Get Token
```bash
curl -X POST http://localhost:8080/api/auth/authenticate \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "SecurePass123"
  }'
```
**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "john.doe@example.com",
  "roles": ["ROLE_PATIENT"]
}
```

#### 3. Get Available Doctors
```bash
curl -X GET http://localhost:8080/api/appointments/doctors \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

#### 4. Book Appointment
```bash
curl -X POST http://localhost:8080/api/appointments/book \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "doctorId": 2,
    "appointmentDateTime": "2025-08-10T14:30:00"
  }'
```

#### 5. Get My Appointments
```bash
curl -X GET http://localhost:8080/api/appointments/my-appointments \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

#### 6. Submit Feedback After Appointment
```bash
curl -X POST http://localhost:8080/api/feedback/submit \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "appointmentId": 123,
    "rating": 5,
    "comment": "Excellent care and very professional",
    "category": "OVERALL_EXPERIENCE",
    "wouldRecommend": true
  }'
```

### Doctor Workflow Example

#### 1. Doctor Login
```bash
curl -X POST http://localhost:8080/api/auth/authenticate \
  -H "Content-Type: application/json" \
  -d '{
    "email": "dr.smith@hospital.com",
    "password": "DoctorPass123"
  }'
```

#### 2. Set Weekly Availability
```bash
curl -X PUT http://localhost:8080/api/doctors/2/availability \
  -H "Authorization: Bearer doctor_token_here" \
  -H "Content-Type: application/json" \
  -d '{
    "availabilities": [
      {
        "dayOfWeek": "MONDAY",
        "startTime": "09:00",
        "endTime": "17:00"
      },
      {
        "dayOfWeek": "WEDNESDAY",
        "startTime": "09:00",
        "endTime": "17:00"
      }
    ]
  }'
```

#### 3. Get Upcoming Appointments
```bash
curl -X GET http://localhost:8080/api/doctors/2/appointments/upcoming \
  -H "Authorization: Bearer doctor_token_here"
```

#### 4. Confirm Appointment
```bash
curl -X PUT http://localhost:8080/api/doctors/2/appointments/123/confirm \
  -H "Authorization: Bearer doctor_token_here"
```

#### 5. Add Consultation Notes
```bash
curl -X POST http://localhost:8080/api/doctors/2/appointments/123/notes \
  -H "Authorization: Bearer doctor_token_here" \
  -H "Content-Type: application/json" \
  -d '{
    "diagnosis": "Mild hypertension",
    "prescription": "Lisinopril 10mg daily",
    "treatmentDetails": "Follow up in 3 months",
    "remarks": "Patient responded well to treatment"
  }'
```

### Admin Operations Example

#### 1. Admin Login
```bash
curl -X POST http://localhost:8080/api/auth/authenticate \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@hospital.com",
    "password": "AdminPass123"
  }'
```

#### 2. Get All Users
```bash
curl -X GET http://localhost:8080/api/admin/users \
  -H "Authorization: Bearer admin_token_here"
```

#### 3. Create New Doctor
```bash
curl -X POST http://localhost:8080/api/admin/users \
  -H "Authorization: Bearer admin_token_here" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "dr.johnson@hospital.com",
    "password": "NewDoctorPass123",
    "fullName": "Dr. Emily Johnson",
    "roles": ["ROLE_DOCTOR"]
  }'
```

#### 4. Trigger Manual Reminders
```bash
curl -X POST http://localhost:8080/api/admin/reminders/trigger-daily \
  -H "Authorization: Bearer admin_token_here"
```

#### 5. Get System Analytics
```bash
curl -X GET http://localhost:8080/api/admin/analytics \
  -H "Authorization: Bearer admin_token_here"
```

### Feedback System Example

#### 1. Get Doctor's Rating Statistics
```bash
curl -X GET http://localhost:8080/api/feedback/my-stats \
  -H "Authorization: Bearer doctor_token_here"
```

#### 2. Admin: Get Low Rating Feedback
```bash
curl -X GET "http://localhost:8080/api/feedback/admin/low-ratings?maxRating=2" \
  -H "Authorization: Bearer admin_token_here"
```

#### 3. Admin: Review Feedback
```bash
curl -X PUT http://localhost:8080/api/feedback/admin/456/review \
  -H "Authorization: Bearer admin_token_here" \
  -H "Content-Type: application/json" \
  -d '{
    "adminNotes": "Followed up with patient and doctor. Issue resolved."
  }'
```

---

## Error Handling

### Standard Error Response Format
```json
{
  "success": false,
  "message": "Error description",
  "timestamp": "2025-08-05T10:30:00",
  "status": 400
}
```

### Common HTTP Status Codes

| Status Code | Description | Example |
|------------|-------------|---------|
| 200 | Success | Successful GET request |
| 201 | Created | User registered successfully |
| 400 | Bad Request | Invalid input data |
| 401 | Unauthorized | Invalid JWT token |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Resource not found |
| 409 | Conflict | Duplicate appointment time |
| 500 | Server Error | Database connection error |

### Example Error Responses

#### Authentication Error
```json
{
  "success": false,
  "message": "Invalid credentials",
  "timestamp": "2025-08-05T10:30:00",
  "status": 401
}
```

#### Validation Error
```json
{
  "success": false,
  "message": "Appointment time slot already taken",
  "timestamp": "2025-08-05T10:30:00",
  "status": 409
}
```

#### Authorization Error
```json
{
  "success": false,
  "message": "Access denied. Admin role required.",
  "timestamp": "2025-08-05T10:30:00",
  "status": 403
}
```

---

## Setup & Configuration

### Prerequisites
- Java 19 or higher
- Maven 3.6+
- MySQL 8.0+
- SMTP Email Account (Gmail recommended)

### Database Configuration
```properties
# application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/hospital_db?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
```

### Email Configuration
```properties
# Email Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your_email@gmail.com
spring.mail.password=your_app_password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

### JWT Configuration
```properties
# JWT Configuration
app.jwt.secret=your_very_long_secret_key_here
app.jwt.expiration-ms=3600000
app.jwt.refresh-expiration-ms=604800000
```

### Running the Application

#### 1. Clone Repository
```bash
git clone <repository-url>
cd hospital-management-system
```

#### 2. Configure Database
```sql
CREATE DATABASE hospital_db;
```

#### 3. Update Configuration
Edit `src/main/resources/application.properties` with your database and email settings.

#### 4. Build and Run
```bash
mvn clean install
mvn spring-boot:run
```

#### 5. Access Application
- API Base URL: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html` (if configured)

---

## Testing Guide

### Testing Tools
- **Postman**: For API testing
- **cURL**: Command line testing
- **JUnit**: Unit tests
- **MockMvc**: Integration tests

### Sample Test Scenarios

#### 1. Authentication Flow Test
```bash
# Register → Login → Access Protected Endpoint
./test-auth-flow.sh
```

#### 2. Appointment Booking Flow Test
```bash
# Login as Patient → Get Doctors → Book Appointment → Confirm
./test-appointment-flow.sh
```

#### 3. Doctor Workflow Test
```bash
# Login as Doctor → Set Availability → Manage Appointments
./test-doctor-workflow.sh
```

#### 4. Admin Operations Test
```bash
# Login as Admin → Manage Users → System Operations
./test-admin-operations.sh
```

### Postman Collection
Import the provided Postman collection for comprehensive API testing:
- File: `Hospital_Management_API.postman_collection.json`
- Environment: `Hospital_Management_Environment.postman_environment.json`

### Unit Testing
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=AppointmentServiceTest

# Run with coverage
mvn test jacoco:report
```

---

## Scheduled Tasks

### Appointment Reminder Scheduler
- **Daily Reminders**: Every day at 9:00 AM
- **Weekly Summary**: Every Monday at 8:00 AM
- **Manual Triggers**: Available through admin endpoints

### Scheduler Configuration
```properties
# Scheduler Configuration
hospital.scheduler.enabled=true
hospital.scheduler.pool-size=3
hospital.reminders.daily-enabled=true
hospital.reminders.batch-size=50
```

---

## Best Practices

### API Usage
1. **Always include Authorization header** for protected endpoints
2. **Use appropriate HTTP methods** (GET, POST, PUT, DELETE)
3. **Handle error responses** properly in client applications
4. **Implement retry logic** for transient failures
5. **Use pagination** for large data sets

### Security
1. **Store JWT tokens securely** (not in localStorage for production)
2. **Implement token refresh** logic
3. **Use HTTPS** in production
4. **Validate all inputs** on client side
5. **Handle authentication failures** gracefully

### Performance
1. **Cache frequently accessed data**
2. **Use database indexes** appropriately
3. **Implement pagination** for large results
4. **Optimize database queries**
5. **Use async operations** for email sending

---

## Support & Troubleshooting

### Common Issues

#### 1. Authentication Problems
- Verify JWT token format
- Check token expiration
- Ensure correct role assignments

#### 2. Database Connection Issues
- Verify database credentials
- Check MySQL service status
- Ensure database exists

#### 3. Email Service Problems
- Verify SMTP configuration
- Check app password for Gmail
- Ensure firewall allows SMTP

#### 4. Appointment Booking Issues
- Verify doctor availability
- Check appointment time conflicts
- Ensure valid date format

### Logs and Monitoring
```bash
# Application logs
tail -f logs/hospital-management.log

# Database logs
tail -f /var/log/mysql/error.log

# System resources
htop
df -h
```

### Contact Information
- **Development Team**: ADM Team 6
- **Email**: support@hospital-management.com
- **Documentation**: Available in project repository

---

## Appendix

### Database Schema
See `schema.sql` for complete database structure.

### API Response Examples
See `api-examples/` directory for detailed response samples.

### Deployment Guide
See `DEPLOYMENT.md` for production deployment instructions.

### Changelog
See `CHANGELOG.md` for version history and updates.

---

*Last Updated: August 5, 2025*
*Version: 1.0.0*
*Hospital Management System - ADM Team 6*
