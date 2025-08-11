# Hospital Management System - Appointment Reminder Email System

## Overview

The Hospital Management System now includes a comprehensive automated appointment reminder email system that sends personalized reminders to patients about their upcoming appointments. This system improves patient attendance rates, reduces no-shows, and enhances overall patient satisfaction.

## 🚀 Features

### ✅ **Automated Email Reminders**
- **Daily Scheduled Reminders**: Automatic emails sent every day at 9:00 AM for next-day appointments
- **Personalized Content**: Each email includes patient name, doctor name, appointment details, and helpful reminders
- **Professional Templates**: HTML-formatted emails with hospital branding and clear appointment information
- **Comprehensive Instructions**: Pre-appointment checklist including arrival time, required documents, and contact information

### ✅ **Manual Administrative Control**
- **Individual Reminders**: Send reminders for specific appointments outside scheduled times
- **Batch Processing**: Manually trigger daily reminder jobs for emergency situations
- **System Monitoring**: Real-time status monitoring and appointment count tracking
- **Test Functionality**: Send test emails to verify system functionality

### ✅ **Robust System Architecture**
- **Asynchronous Processing**: Non-blocking email delivery for optimal performance
- **Error Handling**: Individual email failures don't stop batch processing
- **Comprehensive Logging**: Detailed audit trails for monitoring and troubleshooting
- **Scalable Threading**: Dedicated thread pool for scheduled tasks

## 📋 System Components

### 1. **EmailService** (`EmailService.java`)
```java
@Async
public void sendAppointmentReminderEmail(String to, String patientName, String doctorName, 
                                       String appointmentDate, String appointmentTime, 
                                       String location)
```
- Sends personalized HTML appointment reminder emails
- Professional orange-themed template design
- Includes appointment details and helpful pre-visit instructions
- Handles email delivery errors gracefully

### 2. **AppointmentService** (Enhanced)
```java
public void sendAppointmentReminders()
public void sendSingleAppointmentReminder(Long appointmentId)
public List<AppointmentResponseDto> getUpcomingAppointmentsForPatient(String email)
public List<AppointmentResponseDto> getAllUpcomingAppointments()
```
- Batch processing for daily reminder jobs
- Individual appointment reminder functionality
- Upcoming appointment retrieval and filtering
- Integration with email service for reminder delivery

### 3. **AppointmentReminderScheduler** (`scheduler/AppointmentReminderScheduler.java`)
```java
@Scheduled(cron = "0 0 9 * * ?")  // Daily at 9:00 AM
public void sendDailyAppointmentReminders()

@Scheduled(cron = "0 0 8 * * MON")  // Weekly on Monday at 8:00 AM
public void generateWeeklyAppointmentSummary()
```
- Automated daily reminder execution
- Weekly appointment summary generation
- Manual trigger capabilities for emergency use
- Comprehensive error handling and logging

### 4. **AdminReminderController** (`controller/AdminReminderController.java`)
- **POST** `/api/admin/reminders/trigger-daily` - Manual daily reminder trigger
- **POST** `/api/admin/reminders/send-individual/{appointmentId}` - Individual reminder sending
- **GET** `/api/admin/reminders/status` - System status monitoring
- **POST** `/api/admin/reminders/test-email/{email}` - Test email functionality
- **GET** `/api/admin/reminders/upcoming-count` - Appointment count tracking

### 5. **AppointmentController** (Enhanced)
- **GET** `/api/appointments/upcoming` - Patient's upcoming appointments
- **GET** `/api/appointments/upcoming/all` - All upcoming appointments (admin/doctor)
- **POST** `/api/appointments/send-reminders` - Batch reminder trigger (admin)
- **POST** `/api/appointments/{id}/send-reminder` - Individual reminder (admin/doctor)

## 🔧 Configuration

### **Application Properties** (`application.properties`)
```properties
# Scheduler Configuration
hospital.scheduler.enabled=true
hospital.scheduler.pool-size=3
hospital.reminders.daily-time=09:00

# Email Configuration
hospital.reminders.enabled=true
hospital.reminders.batch-size=50
hospital.reminders.template.subject=Appointment Reminder - Hospital Management System

# Error Handling
hospital.reminders.error.max-retries=3
hospital.reminders.error.continue-on-failure=true
```

### **Spring Boot Configuration**
```java
@SpringBootApplication
@EnableAsync
@EnableScheduling  // ← Added for reminder system
public class HospitalManagementApplication
```

## 📅 Scheduling Details

### **Daily Reminder Schedule**
- **Time**: Every day at 9:00 AM
- **Cron Expression**: `"0 0 9 * * ?"`
- **Purpose**: Send reminders for appointments happening tomorrow
- **Processing**: Finds all SCHEDULED appointments for next day and sends personalized emails

### **Weekly Summary Schedule**
- **Time**: Every Monday at 8:00 AM
- **Cron Expression**: `"0 0 8 * * MON"`
- **Purpose**: Generate weekly appointment statistics and system health reports
- **Processing**: Counts upcoming appointments and logs system status

## 🔒 Security

### **Role-Based Access Control**
- **Patient Role**: Can view their own upcoming appointments only
- **Doctor Role**: Can view all upcoming appointments and send individual reminders
- **Admin Role**: Full access to all reminder functions, manual triggers, and system monitoring

### **API Security**
```java
@PreAuthorize("hasRole('PATIENT')")    // Patient endpoints
@PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")  // Doctor endpoints  
@PreAuthorize("hasRole('ADMIN')")      // Admin-only endpoints
```

## 📧 Email Template Features

### **Professional Design**
- Orange theme (#FF9800) for appointment reminders
- Responsive HTML layout for mobile and desktop
- Clear visual hierarchy with appointment details highlighted
- Hospital branding and professional styling

### **Content Includes**
- Personalized patient greeting
- Complete appointment information (doctor, date, time, location)
- Pre-appointment checklist:
  - Arrive 15 minutes early
  - Bring valid ID and insurance card
  - Bring medical records/test results
  - 24-hour cancellation policy
- Contact information for changes/questions
- Professional closing with hospital team signature

## 🚀 Getting Started

### **1. Enable the System**
The reminder system is automatically enabled when the application starts with `@EnableScheduling` annotation.

### **2. Configure Email Settings**
Ensure email configuration is properly set in `application.properties`:
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
```

### **3. Test the System**
```bash
# Test individual reminder
POST /api/admin/reminders/send-individual/1

# Check system status
GET /api/admin/reminders/status

# Send test email
POST /api/admin/reminders/test-email/test@example.com
```

### **4. Monitor Execution**
```bash
# Check application logs for scheduled execution
INFO - === SCHEDULED JOB STARTED: Daily Appointment Reminders ===
INFO - Found 5 appointments for tomorrow requiring reminders
INFO - Reminder sent for appointment ID: 123
INFO - === SCHEDULED JOB COMPLETED: Daily Appointment Reminders ===
```

## 📊 API Endpoints Summary

### **Patient Endpoints**
| Method | Endpoint | Description | Role |
|--------|----------|-------------|------|
| GET | `/api/appointments/upcoming` | Get patient's upcoming appointments | PATIENT |

### **Doctor/Admin Endpoints**  
| Method | Endpoint | Description | Role |
|--------|----------|-------------|------|
| GET | `/api/appointments/upcoming/all` | Get all upcoming appointments | DOCTOR, ADMIN |
| POST | `/api/appointments/send-reminders` | Trigger batch reminders | ADMIN |
| POST | `/api/appointments/{id}/send-reminder` | Send individual reminder | DOCTOR, ADMIN |

### **Admin-Only Endpoints**
| Method | Endpoint | Description | Role |
|--------|----------|-------------|------|
| POST | `/api/admin/reminders/trigger-daily` | Manual daily reminder trigger | ADMIN |
| POST | `/api/admin/reminders/send-individual/{id}` | Send individual reminder | ADMIN |
| GET | `/api/admin/reminders/status` | System status monitoring | ADMIN |
| POST | `/api/admin/reminders/test-email/{email}` | Send test email | ADMIN |
| GET | `/api/admin/reminders/upcoming-count` | Get appointment count | ADMIN |

## 🔍 Monitoring and Troubleshooting

### **Log Patterns to Monitor**
```bash
# Successful execution
INFO - SCHEDULED JOB COMPLETED: Daily Appointment Reminders
INFO - Reminder sent for appointment ID: 123

# System errors
ERROR - SCHEDULED JOB FAILED: Daily Appointment Reminders
ERROR - Failed to send reminder for appointment ID: 123

# Administrative actions
INFO - ADMIN REQUEST: Manual trigger of daily appointment reminders
INFO - ADMIN SUCCESS: Daily reminders triggered successfully
```

### **Common Issues and Solutions**

**Issue**: Emails not being sent
**Solution**: Check email configuration in `application.properties` and verify SMTP settings

**Issue**: Scheduled jobs not running
**Solution**: Ensure `@EnableScheduling` is present in main application class

**Issue**: Wrong timezone for scheduling
**Solution**: Configure `hospital.scheduler.timezone` in application properties

**Issue**: Performance issues with large appointment volumes
**Solution**: Adjust `hospital.scheduler.pool-size` and `hospital.reminders.batch-size`

## 🎯 Future Enhancements

### **Planned Features**
- **SMS Reminders**: Integration with SMS service providers
- **Multiple Reminder Types**: Confirmation reminders, follow-up reminders
- **Custom Templates**: Department-specific email templates
- **Patient Preferences**: Allow patients to configure reminder timing
- **Analytics Dashboard**: Reminder delivery statistics and effectiveness metrics
- **Multi-language Support**: Localized reminder emails
- **Calendar Integration**: .ics file attachments for appointment calendar entries

### **Performance Optimizations**
- **Database Indexing**: Optimize queries for appointment retrieval
- **Caching**: Cache frequently accessed appointment data
- **Batch Processing**: Enhanced batch processing for large volumes
- **Message Queues**: Integration with message brokers for reliability

## 📞 Support

For technical support or questions about the appointment reminder system:

- **Development Team**: Hospital Management Development Team
- **Documentation**: This README and inline code comments
- **Logs**: Check application logs for detailed execution information
- **Testing**: Use admin endpoints to test system functionality

---

**Version**: 1.0  
**Last Updated**: August 5, 2025  
**Author**: Hospital Management Team
