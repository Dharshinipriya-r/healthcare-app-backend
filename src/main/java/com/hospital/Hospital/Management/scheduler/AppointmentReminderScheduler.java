package com.hospital.Hospital.Management.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.hospital.Hospital.Management.service.AppointmentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentReminderScheduler {

    private final AppointmentService appointmentService;

    
    @Scheduled(cron = "0 0 9 * * ?")
    public void sendDailyAppointmentReminders() {
        log.info("=== SCHEDULED JOB STARTED: Daily Appointment Reminders ===");
        log.info("Execution time: {}", java.time.LocalDateTime.now());
        
        try {
          
            appointmentService.sendAppointmentReminders();
            
            log.info("=== SCHEDULED JOB COMPLETED: Daily Appointment Reminders ===");
            
        } catch (Exception e) {
            log.error("=== SCHEDULED JOB FAILED: Daily Appointment Reminders ===", e);
            log.error("Error details: {}", e.getMessage());
            
          
        }
    }

   
    @Scheduled(cron = "0 0 8 * * MON")
    public void generateWeeklyAppointmentSummary() {
        log.info("=== SCHEDULED JOB STARTED: Weekly Appointment Summary ===");
        log.info("Execution time: {}", java.time.LocalDateTime.now());
        
        try {
           
            var upcomingAppointments = appointmentService.getAllUpcomingAppointments();
            
            log.info("Weekly Summary: {} upcoming appointments scheduled for the next 30 days", 
                    upcomingAppointments.size());
            
           
            
            log.info("=== SCHEDULED JOB COMPLETED: Weekly Appointment Summary ===");
            
        } catch (Exception e) {
            log.error("=== SCHEDULED JOB FAILED: Weekly Appointment Summary ===", e);
            log.error("Error details: {}", e.getMessage());
        }
    }

    
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
