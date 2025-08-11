package com.hospital.Hospital.Management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for Hospital Management System.
 * 
 * This class serves as the entry point for the Spring Boot application and
 * enables key features required for hospital operations including:
 * - Asynchronous processing for email services
 * - Scheduled tasks for appointment reminders
 * - Component scanning for all hospital management modules
 * 
 * Enabled Features:
 * - @EnableAsync: Supports asynchronous email sending and background processing
 * - @EnableScheduling: Enables automated appointment reminder scheduling
 * - @SpringBootApplication: Auto-configuration and component scanning
 * 
 * Key Components:
 * - Appointment management and scheduling
 * - Email service for notifications and reminders
 * - User authentication and authorization
 * - Doctor availability management
 * - Administrative reporting and monitoring
 * 
 * Scheduled Operations:
 * - Daily appointment reminders at 9:00 AM
 * - Weekly appointment summaries on Mondays
 * - System health monitoring and reporting
 * 
 * @author Hospital Management Team
 * @version 1.0
 * @since 2025-08-05
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class HospitalManagementApplication {

	/**
	 * Main method to start the Hospital Management System application.
	 * 
	 * Initializes the Spring Boot application context and starts all
	 * configured services including:
	 * - Web controllers for patient and doctor interfaces
	 * - Background services for email processing
	 * - Scheduled tasks for appointment reminders
	 * - Database connections and data management
	 * - Security configuration and user authentication
	 * 
	 * @param args Command line arguments (not used in typical deployment)
	 */
	public static void main(String[] args) {
		SpringApplication.run(HospitalManagementApplication.class, args);
	}
}
