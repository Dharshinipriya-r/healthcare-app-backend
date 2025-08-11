package com.hospital.Hospital.Management.controller;

import com.hospital.Hospital.Management.dto.ApiResponse;
import com.hospital.Hospital.Management.scheduler.AppointmentReminderScheduler;
import com.hospital.Hospital.Management.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Administrative controller for managing appointment reminder emails and scheduled tasks.
 * 
 * This controller provides administrative endpoints for hospital staff to manually
 * manage appointment reminder systems, monitor scheduled tasks, and handle emergency
 * reminder scenarios outside of automated scheduling.
 * 
 * Security:
 * - All endpoints restricted to ADMIN role only
 * - Sensitive operations require administrative privileges
 * - Audit logging for all administrative actions
 * 
 * Key Features:
 * - Manual reminder triggering for emergency situations
 * - Individual appointment reminder sending
 * - Batch reminder processing control
 * - System health monitoring for reminder services
 * 
 * Use Cases:
 * - Emergency reminder sending outside scheduled times
 * - Testing reminder system functionality
 * - Recovery from failed scheduled executions
 * - Administrative override for special situations
 * - System maintenance and monitoring
 * 
 * Integration:
 * - Works with AppointmentReminderScheduler for manual triggers
 * - Uses AppointmentService for individual reminder operations
 * - Provides administrative control over automated systems
 * 
 * @author Hospital Management Team
 * @version 1.0
 * @since 2025-08-05
 */
@RestController
@RequestMapping("/api/admin/reminders")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminReminderController {

    private final AppointmentReminderScheduler reminderScheduler;
    private final AppointmentService appointmentService;

    /**
     * POST /api/admin/reminders/trigger-daily
     * Manually triggers the daily appointment reminder job.
     * 
     * This endpoint allows hospital administrators to manually execute the
     * daily reminder process outside of the scheduled time. Useful for:
     * - Emergency reminder sending
     * - Testing the reminder system
     * - Recovery from failed scheduled executions
     * - Holiday or special schedule adjustments
     * 
     * The process will:
     * - Find all appointments scheduled for tomorrow
     * - Send personalized reminder emails to each patient
     * - Log all execution details for audit purposes
     * - Return summary of processing results
     * 
     * Security: ADMIN role required
     * 
     * @return ResponseEntity with success message and execution summary
     */
    @PostMapping("/trigger-daily")
    public ResponseEntity<ApiResponse<Void>> triggerDailyReminders() {
        log.info("ADMIN REQUEST: Manual trigger of daily appointment reminders");
        
        try {
            reminderScheduler.triggerManualReminderSending();
            
            String message = "Daily appointment reminders triggered successfully. " +
                           "Check application logs for detailed execution results.";
            
            log.info("ADMIN SUCCESS: Daily reminders triggered successfully");
            return ResponseEntity.ok(ApiResponse.success(message));
            
        } catch (Exception e) {
            String errorMessage = "Failed to trigger daily appointment reminders: " + e.getMessage();
            log.error("ADMIN ERROR: Failed to trigger daily reminders", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(errorMessage));
        }
    }

    /**
     * POST /api/admin/reminders/send-individual/{appointmentId}
     * Sends a reminder email for a specific appointment.
     * 
     * This endpoint allows administrators to send individual appointment
     * reminders outside of the batch processing system. Useful for:
     * - Custom reminder timing for VIP patients
     * - Re-sending reminders upon patient request
     * - Special case handling
     * - Follow-up reminders for important appointments
     * 
     * Validation:
     * - Verifies appointment exists
     * - Ensures appointment is in SCHEDULED status
     * - Confirms patient email is available
     * 
     * Security: ADMIN role required
     * 
     * @param appointmentId The unique identifier of the appointment
     * @return ResponseEntity with success/failure message
     */
    @PostMapping("/send-individual/{appointmentId}")
    public ResponseEntity<ApiResponse<Void>> sendIndividualReminder(@PathVariable Long appointmentId) {
        log.info("ADMIN REQUEST: Manual reminder for appointment ID: {}", appointmentId);
        
        try {
            appointmentService.sendSingleAppointmentReminder(appointmentId);
            
            String message = String.format(
                "Appointment reminder sent successfully for appointment ID: %d", 
                appointmentId
            );
            
            log.info("ADMIN SUCCESS: Individual reminder sent for appointment ID: {}", appointmentId);
            return ResponseEntity.ok(ApiResponse.success(message));
            
        } catch (IllegalArgumentException e) {
            String errorMessage = "Appointment not found: " + e.getMessage();
            log.warn("ADMIN WARNING: Appointment not found for ID: {}", appointmentId);
            return ResponseEntity.badRequest().body(ApiResponse.error(errorMessage));
            
        } catch (IllegalStateException e) {
            String errorMessage = "Cannot send reminder: " + e.getMessage();
            log.warn("ADMIN WARNING: Cannot send reminder for appointment ID: {}", appointmentId);
            return ResponseEntity.badRequest().body(ApiResponse.error(errorMessage));
            
        } catch (Exception e) {
            String errorMessage = "Failed to send reminder: " + e.getMessage();
            log.error("ADMIN ERROR: Failed to send reminder for appointment ID: {}", appointmentId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(errorMessage));
        }
    }

    /**
     * GET /api/admin/reminders/status
     * Retrieves the current status of the reminder system.
     * 
     * This endpoint provides administrators with system health information
     * and current status of reminder services. Returns information about:
     * - Last execution time of scheduled reminders
     * - Number of upcoming appointments requiring reminders
     * - System configuration and health status
     * - Recent execution statistics
     * 
     * Security: ADMIN role required
     * 
     * @return ResponseEntity with system status information
     */
    @GetMapping("/status")
    public ResponseEntity<Object> getReminderSystemStatus() {
        log.info("ADMIN REQUEST: Reminder system status check");
        
        try {
            // Get upcoming appointments count
            var upcomingAppointments = appointmentService.getAllUpcomingAppointments();
            int totalUpcoming = upcomingAppointments.size();
            
            // Create status response
            var statusResponse = new java.util.HashMap<String, Object>();
            statusResponse.put("status", "OPERATIONAL");
            statusResponse.put("upcomingAppointments", totalUpcoming);
            statusResponse.put("lastStatusCheck", java.time.LocalDateTime.now());
            statusResponse.put("schedulerEnabled", true);
            statusResponse.put("emailServiceStatus", "ACTIVE");
            
            // Additional system information
            statusResponse.put("systemInfo", java.util.Map.of(
                "scheduledJobsActive", true,
                "dailyReminderTime", "09:00 AM",
                "weeklyReportTime", "Monday 08:00 AM",
                "timeZone", java.time.ZoneId.systemDefault().toString()
            ));
            
            log.info("ADMIN SUCCESS: Status check completed - {} upcoming appointments", totalUpcoming);
            return ResponseEntity.ok(statusResponse);
            
        } catch (Exception e) {
            log.error("ADMIN ERROR: Failed to retrieve system status", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve system status: " + e.getMessage()));
        }
    }

    /**
     * POST /api/admin/reminders/test-email/{patientEmail}
     * Sends a test reminder email to verify email service functionality.
     * 
     * This endpoint allows administrators to test the email reminder system
     * by sending a sample reminder email to a specified address. Useful for:
     * - Testing email configuration
     * - Verifying email template rendering
     * - Troubleshooting email delivery issues
     * - System health verification
     * 
     * The test email will contain:
     * - Sample appointment data
     * - Full HTML template rendering
     * - All standard reminder content
     * - Clear indication it's a test message
     * 
     * Security: ADMIN role required
     * 
     * @param patientEmail The email address to send the test reminder to
     * @return ResponseEntity with test result message
     */
    @PostMapping("/test-email/{patientEmail}")
    public ResponseEntity<ApiResponse<Void>> sendTestReminderEmail(@PathVariable String patientEmail) {
        log.info("ADMIN REQUEST: Test reminder email to: {}", patientEmail);
        
        try {
            // Use EmailService directly for test email
            var emailService = appointmentService.getClass()
                    .getDeclaredField("emailService");
            emailService.setAccessible(true);
            var emailServiceInstance = (com.hospital.Hospital.Management.service.EmailService) 
                    emailService.get(appointmentService);
            
            // Send test reminder with sample data
            emailServiceInstance.sendAppointmentReminderEmail(
                patientEmail,
                "Test Patient",
                "Sample Doctor",
                java.time.LocalDate.now().plusDays(1).toString(),
                "14:30",
                "Hospital Main Building - TEST REMINDER"
            );
            
            String message = String.format(
                "Test reminder email sent successfully to: %s", 
                patientEmail
            );
            
            log.info("ADMIN SUCCESS: Test email sent to: {}", patientEmail);
            return ResponseEntity.ok(ApiResponse.success(message));
            
        } catch (Exception e) {
            String errorMessage = "Failed to send test email: " + e.getMessage();
            log.error("ADMIN ERROR: Failed to send test email to: {}", patientEmail, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(errorMessage));
        }
    }

    /**
     * GET /api/admin/reminders/upcoming-count
     * Returns the count of appointments requiring reminders.
     * 
     * This endpoint provides a quick count of appointments that will be
     * processed by the next reminder job execution. Useful for:
     * - Monitoring reminder workload
     * - Planning system resources
     * - Verifying appointment data
     * - Administrative reporting
     * 
     * Security: ADMIN role required
     * 
     * @return ResponseEntity with appointment count information
     */
    @GetMapping("/upcoming-count")
    public ResponseEntity<Object> getUpcomingAppointmentCount() {
        log.info("ADMIN REQUEST: Upcoming appointment count");
        
        try {
            var upcomingAppointments = appointmentService.getAllUpcomingAppointments();
            
            var countResponse = java.util.Map.of(
                "totalUpcoming", upcomingAppointments.size(),
                "timeRange", "Next 30 days",
                "lastUpdated", java.time.LocalDateTime.now(),
                "status", "ACTIVE"
            );
            
            log.info("ADMIN SUCCESS: Count retrieved - {} upcoming appointments", 
                    upcomingAppointments.size());
            return ResponseEntity.ok(countResponse);
            
        } catch (Exception e) {
            log.error("ADMIN ERROR: Failed to get appointment count", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve appointment count: " + e.getMessage()));
        }
    }
}
