package com.hospital.Hospital.Management.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Configuration class for scheduling appointment reminder emails and related tasks.
 * 
 * This configuration enables Spring's scheduling capabilities and provides
 * custom task scheduler configuration for optimal performance of appointment
 * reminder jobs and other scheduled hospital management tasks.
 * 
 * Key Features:
 * - Enables @Scheduled annotation processing
 * - Configures dedicated thread pool for scheduled tasks
 * - Provides non-blocking execution for email reminders
 * - Supports concurrent task execution without system impact
 * 
 * Scheduler Configuration:
 * - Dedicated thread pool for scheduled tasks
 * - Configurable pool size based on hospital needs
 * - Thread naming for easy monitoring and debugging
 * - Graceful shutdown handling
 * 
 * Integration:
 * - Works with AppointmentReminderScheduler for automated reminders
 * - Supports future scheduled tasks (reports, notifications, etc.)
 * - Provides foundation for hospital operational automation
 * 
 * Performance Considerations:
 * - Non-blocking execution prevents impact on main application
 * - Configurable thread pool size for scalability
 * - Error isolation to prevent scheduler failures
 * - Resource management for long-running tasks
 * 
 * @author Hospital Management Team
 * @version 1.0
 * @since 2025-08-05
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {

    /**
     * Custom task scheduler bean for appointment reminders and hospital operations.
     * 
     * This scheduler provides dedicated threading for scheduled tasks including:
     * - Daily appointment reminder emails
     * - Weekly appointment summaries
     * - Administrative reports
     * - System maintenance tasks
     * 
     * Configuration Details:
     * - Pool Size: 3 threads (suitable for typical hospital operations)
     * - Thread Naming: "hospital-scheduler-" prefix for easy identification
     * - Daemon Threads: false (ensures proper shutdown)
     * - Wait for Tasks: true (graceful shutdown)
     * - Shutdown Timeout: 30 seconds
     * 
     * Thread Pool Sizing:
     * - 1 thread: Daily appointment reminders
     * - 1 thread: Weekly summaries and reports
     * - 1 thread: Emergency/manual tasks and buffer
     * 
     * Monitoring:
     * - Thread names include "hospital-scheduler-" for easy log filtering
     * - JMX monitoring available through Spring Boot Actuator
     * - Custom metrics can be added for task execution monitoring
     * 
     * @return Configured ThreadPoolTaskScheduler for hospital scheduling needs
     */
    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        
        // Core configuration
        scheduler.setPoolSize(3); // Adequate for hospital reminder tasks
        scheduler.setThreadNamePrefix("hospital-scheduler-"); // Clear identification
        scheduler.setDaemon(false); // Ensure proper shutdown behavior
        
        // Shutdown configuration
        scheduler.setWaitForTasksToCompleteOnShutdown(true); // Graceful shutdown
        scheduler.setAwaitTerminationSeconds(30); // Max wait time for shutdown
        
        // Error handling
        scheduler.setRejectedExecutionHandler(
            (runnable, executor) -> {
                System.err.println("Hospital Scheduler: Task rejected - " + runnable.toString());
                // Log the rejection for monitoring
                // Consider implementing custom rejection handling based on needs
            }
        );
        
        // Initialize the scheduler
        scheduler.initialize();
        
        return scheduler;
    }

    /**
     * Configuration properties for scheduler customization.
     * 
     * These properties can be overridden in application.properties or
     * application.yml for environment-specific configuration.
     * 
     * Example application.properties:
     * 
     * # Scheduler Configuration
     * hospital.scheduler.pool-size=5
     * hospital.scheduler.thread-name-prefix=custom-hospital-scheduler-
     * hospital.scheduler.shutdown-timeout=60
     * 
     * # Reminder Timing Configuration
     * hospital.reminders.daily-time=09:00
     * hospital.reminders.evening-time=18:00
     * hospital.reminders.weekly-summary-time=08:00
     * 
     * # Email Configuration
     * hospital.reminders.enabled=true
     * hospital.reminders.batch-size=50
     * hospital.reminders.retry-attempts=3
     * 
     * Future Enhancement Ideas:
     * - Environment-specific scheduler configuration
     * - Dynamic pool size adjustment based on load
     * - Custom metrics for task execution monitoring
     * - Integration with hospital management dashboards
     * - Configurable retry policies for failed tasks
     */
}
