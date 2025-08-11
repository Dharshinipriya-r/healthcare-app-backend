package com.hospital.Hospital.Management.scheduler;

import com.hospital.Hospital.Management.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled task component for automatically sending appointment reminder emails.
 * 
 * This component handles the automated execution of appointment reminder emails
 * using Spring's @Scheduled annotation. It integrates with the AppointmentService
 * to find and process upcoming appointments requiring reminder notifications.
 * 
 * Scheduling Features:
 * - Daily execution at configurable times
 * - Cron-based scheduling for precise timing control
 * - Automatic error handling and logging
 * - Non-blocking execution to prevent system impact
 * 
 * Business Logic:
 * - Runs daily to send reminders for next-day appointments
 * - Processes all SCHEDULED appointments for tomorrow
 * - Sends personalized HTML email reminders to each patient
 * - Logs execution results for monitoring and audit purposes
 * 
 * Integration:
 * - Uses AppointmentService.sendAppointmentReminders() for batch processing
 * - Leverages EmailService for actual email delivery
 * - Supports hospital operational schedules and patient communication needs
 * 
 * Configuration:
 * - Requires @EnableScheduling in main application class
 * - Uses hospital timezone for accurate scheduling
 * - Configurable execution times through cron expressions
 * 
 * @author Hospital Management Team
 * @version 1.0
 * @since 2025-08-05
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentReminderScheduler {

    private final AppointmentService appointmentService;

    /**
     * Daily appointment reminder job - Primary execution schedule.
     * 
     * Executes every day at 9:00 AM to send reminder emails for appointments
     * scheduled for the following day. This timing ensures patients receive
     * reminders with adequate notice while maintaining business hours delivery.
     * 
     * Schedule: Daily at 9:00 AM (Monday through Sunday)
     * Cron Expression: "0 0 9 * * ?" 
     * - Second: 0 (at the start of the minute)
     * - Minute: 0 (at the start of the hour)
     * - Hour: 9 (9 AM)
     * - Day of Month: * (every day)
     * - Month: * (every month)
     * - Day of Week: ? (any day of the week)
     * 
     * Business Benefits:
     * - Improves patient attendance rates through proactive communication
     * - Reduces no-shows and last-minute cancellations
     * - Enhances patient satisfaction with professional service
     * - Supports hospital operational efficiency
     * 
     * Error Handling:
     * - Individual email failures don't stop the batch process
     * - Comprehensive logging for monitoring and troubleshooting
     * - Graceful degradation for system resilience
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void sendDailyAppointmentReminders() {
        log.info("=== SCHEDULED JOB STARTED: Daily Appointment Reminders ===");
        log.info("Execution time: {}", java.time.LocalDateTime.now());
        
        try {
            // Execute the batch reminder process
            appointmentService.sendAppointmentReminders();
            
            log.info("=== SCHEDULED JOB COMPLETED: Daily Appointment Reminders ===");
            
        } catch (Exception e) {
            log.error("=== SCHEDULED JOB FAILED: Daily Appointment Reminders ===", e);
            log.error("Error details: {}", e.getMessage());
            
            // Note: We don't rethrow the exception to prevent scheduler from stopping
            // Instead, we log the error and let the system continue for next execution
        }
    }

    /**
     * Evening appointment reminder job - Secondary execution schedule.
     * 
     * Executes every day at 6:00 PM as a secondary reminder for appointments
     * scheduled for the next day. This provides an additional touchpoint for
     * patients who may have missed the morning reminder.
     * 
     * Schedule: Daily at 6:00 PM (Monday through Sunday)
     * Cron Expression: "0 0 18 * * ?"
     * - Second: 0 (at the start of the minute)
     * - Minute: 0 (at the start of the hour) 
     * - Hour: 18 (6 PM)
     * - Day of Month: * (every day)
     * - Month: * (every month)
     * - Day of Week: ? (any day of the week)
     * 
     * Use Cases:
     * - Secondary reminder for critical appointments
     * - Backup delivery for morning reminder failures
     * - Enhanced patient communication for special cases
     * - Weekend and holiday coverage
     * 
     * Note: This method is commented out by default to prevent duplicate reminders.
     * Uncomment and configure as needed based on hospital policy.
     */
    /*
    @Scheduled(cron = "0 0 18 * * ?")
    public void sendEveningAppointmentReminders() {
        log.info("=== SCHEDULED JOB STARTED: Evening Appointment Reminders ===");
        log.info("Execution time: {}", java.time.LocalDateTime.now());
        
        try {
            // Execute the batch reminder process
            appointmentService.sendAppointmentReminders();
            
            log.info("=== SCHEDULED JOB COMPLETED: Evening Appointment Reminders ===");
            
        } catch (Exception e) {
            log.error("=== SCHEDULED JOB FAILED: Evening Appointment Reminders ===", e);
            log.error("Error details: {}", e.getMessage());
        }
    }
    */

    /**
     * Weekly appointment summary job - Administrative overview.
     * 
     * Executes every Monday at 8:00 AM to provide hospital administrators
     * with a weekly summary of upcoming appointments and reminder statistics.
     * 
     * Schedule: Every Monday at 8:00 AM
     * Cron Expression: "0 0 8 * * MON"
     * 
     * Features:
     * - Weekly operational reporting
     * - Appointment volume analysis
     * - Reminder delivery statistics
     * - System health monitoring
     * 
     * Note: Implementation can be added based on administrative requirements.
     */
    @Scheduled(cron = "0 0 8 * * MON")
    public void generateWeeklyAppointmentSummary() {
        log.info("=== SCHEDULED JOB STARTED: Weekly Appointment Summary ===");
        log.info("Execution time: {}", java.time.LocalDateTime.now());
        
        try {
            // Get upcoming appointments for the week
            var upcomingAppointments = appointmentService.getAllUpcomingAppointments();
            
            log.info("Weekly Summary: {} upcoming appointments scheduled for the next 30 days", 
                    upcomingAppointments.size());
            
            // Additional summary logic can be implemented here:
            // - Email summary to administrators
            // - Generate reports
            // - Update dashboards
            // - System health checks
            
            log.info("=== SCHEDULED JOB COMPLETED: Weekly Appointment Summary ===");
            
        } catch (Exception e) {
            log.error("=== SCHEDULED JOB FAILED: Weekly Appointment Summary ===", e);
            log.error("Error details: {}", e.getMessage());
        }
    }

    /**
     * Manual trigger method for testing and emergency use.
     * 
     * This method provides a way to manually trigger reminder sending
     * outside of the scheduled times. Useful for:
     * - Testing the reminder system
     * - Emergency reminder sending
     * - Administrative override situations
     * - System recovery scenarios
     * 
     * Security: This method should only be called by administrative functions
     * or during testing scenarios. Consider adding security annotations if
     * exposed through REST endpoints.
     */
    public void triggerManualReminderSending() {
        log.info("=== MANUAL TRIGGER: Appointment Reminders ===");
        log.info("Triggered by: Manual execution");
        log.info("Execution time: {}", java.time.LocalDateTime.now());
        
        try {
            appointmentService.sendAppointmentReminders();
            log.info("=== MANUAL TRIGGER COMPLETED: Appointment Reminders ===");
            
        } catch (Exception e) {
            log.error("=== MANUAL TRIGGER FAILED: Appointment Reminders ===", e);
            throw new RuntimeException("Manual reminder trigger failed", e);
        }
    }
}
