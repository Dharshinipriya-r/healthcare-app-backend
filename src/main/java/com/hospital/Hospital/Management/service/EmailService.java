package com.hospital.Hospital.Management.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.hospital.Hospital.Management.model.Appointment;
import com.hospital.Hospital.Management.model.User;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base-url}")
    private String baseUrl;

    

    @Async
    public void sendAppointmentConfirmationEmail(Appointment appointment) throws MessagingException {
        String subject = "Your Appointment is Confirmed!";
        String body = String.format(
                "Dear %s,<br/><br/>" +
                        "Great news! Your appointment with <strong>Dr. %s</strong> has been confirmed." +
                        "<br/><br/><strong>Appointment Details:</strong>" +
                        "<ul>" +
                        "<li><strong>Date:</strong> %s</li>" +
                        "<li><strong>Time:</strong> %s</li>" +
                        "<li><strong>Location:</strong> %s</li>" +
                        "</ul>" +
                        "Please arrive 15 minutes early for your appointment. If you need to cancel or reschedule, please contact our office at least 24 hours in advance." +
                        "<br/><br/>We look forward to seeing you."+
                        "<br/><br/>Best regards,<br/>The Hospital Management Team",
                appointment.getPatient().getFullName(),
                appointment.getDoctor().getFullName(),
                appointment.getAppointmentDateTime().toLocalDate().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")),
                appointment.getAppointmentDateTime().toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm a")),
                appointment.getDoctor().getLocation() != null ? appointment.getDoctor().getLocation() : "Hospital Main Campus"
        );
        sendHtmlEmail(appointment.getPatient().getEmail(), subject, body, "#4CAF50");
    }

    
    @Async
    public void sendAppointmentRescheduleByPatientEmail(Appointment appointment, LocalDateTime oldDateTime) throws MessagingException {
        String subject = "Patient Reschedule Notice - Appointment Update";
        String body = String.format(
                "Dear Dr. %s,<br/><br/>" +
                        "Please note that a patient has rescheduled their appointment." +
                        "<br/><br/><strong>Patient Name:</strong> %s" +
                        "<br/><strong>Original Time:</strong> %s at %s" +
                        "<br/><strong>New Time:</strong> <strong>%s at %s</strong>" +
                        "<br/><br/>Please review your updated schedule." +
                        "<br/><br/>Best regards,<br/>Hospital Management System",
                appointment.getDoctor().getFullName(),
                appointment.getPatient().getFullName(),
                oldDateTime.toLocalDate().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")),
                oldDateTime.toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm a")),
                appointment.getAppointmentDateTime().toLocalDate().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")),
                appointment.getAppointmentDateTime().toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm a"))
        );
        sendHtmlEmail(appointment.getDoctor().getEmail(), subject, body, "#FF9800");
    }

    
    @Async
    public void sendWaitlistBookingConfirmationEmail(Appointment appointment) throws MessagingException {
        String subject = "Your Waitlisted Appointment is Now Booked!";
        String body = String.format(
                "Dear %s,<br/><br/>" +
                        "Excellent news! A slot has opened up, and you have been automatically booked for an appointment with <strong>Dr. %s</strong>." +
                        "<br/><br/><strong>Your Confirmed Appointment Details:</strong>" +
                        "<ul>" +
                        "<li><strong>Date:</strong> %s</li>" +
                        "<li><strong>Time:</strong> %s</li>" +
                        "<li><strong>Location:</strong> %s</li>" +
                        "</ul>" +
                        "This appointment is now in your upcoming schedule. The doctor will need to confirm it. If you cannot make this time, please cancel it via the patient portal." +
                        "<br/><br/>Best regards,<br/>The Hospital Management Team",
                appointment.getPatient().getFullName(),
                appointment.getDoctor().getFullName(),
                appointment.getAppointmentDateTime().toLocalDate().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")),
                appointment.getAppointmentDateTime().toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm a")),
                appointment.getDoctor().getLocation() != null ? appointment.getDoctor().getLocation() : "Hospital Main Campus"
        );
        sendHtmlEmail(appointment.getPatient().getEmail(), subject, body, "#4CAF50");
    }

   
    @Async
    public void sendVerificationEmail(String to, String token) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject("Verify Your Hospital Management System Account");

        String verificationUrl = baseUrl + "/api/auth/verify-email?token=" + token;

        String htmlContent = String.format("""
            <html><head><style>body{font-family:Arial,sans-serif;line-height:1.6}.container{width:600px;margin:0 auto;padding:20px}.header{background-color:#4CAF50;color:white;padding:10px;text-align:center}.content{padding:20px}.button{background-color:#4CAF50;color:white;padding:10px 20px;text-decoration:none;border-radius:5px;display:inline-block}</style></head><body><div class="container"><div class="header"><h1>Email Verification</h1></div><div class="content"><h2>Hello,</h2><p>Thank you for registering with our Hospital Management System. Please verify your email address by clicking the button below:</p><p><a class="button" href="%s">Verify Email</a></p><p>Or copy and paste the following link in your browser:</p><p>%s</p><p>This link will expire in 24 hours.</p><p>If you did not register on our platform, please ignore this email.</p><p>Best regards,<br/>Hospital Management Team</p></div></div></body></html>
            """, verificationUrl, verificationUrl);

        helper.setText(htmlContent, true);
        mailSender.send(message);
    }

    @Async
    public void sendPasswordResetEmail(String to, String token) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject("Reset Your Hospital Management System Password");

        String resetUrl = baseUrl + "/api/auth/reset-password?token=" + token;

        String htmlContent = String.format("""
            <html><head><style>body{font-family:Arial,sans-serif;line-height:1.6}.container{width:600px;margin:0 auto;padding:20px}.header{background-color:#2196F3;color:white;padding:10px;text-align:center}.content{padding:20px}.button{background-color:#2196F3;color:white;padding:10px 20px;text-decoration:none;border-radius:5px;display:inline-block}</style></head><body><div class="container"><div class="header"><h1>Password Reset</h1></div><div class="content"><h2>Hello,</h2><p>You have requested to reset your password for the Hospital Management System. Please click the button below to set a new password:</p><p><a class="button" href="%s">Reset Password</a></p><p>Or copy and paste the following link in your browser:</p><p>%s</p><p>This link will expire in 24 hours.</p><p>If you did not request a password reset, please ignore this email or contact support.</p><p>Best regards,<br/>Hospital Management Team</p></div></div></body></html>
            """, resetUrl, resetUrl);

        helper.setText(htmlContent, true);
        mailSender.send(message);
    }

    @Async
    public void sendGenericEmail(String to, String subject, String text) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);

        String htmlContent = String.format("""
            <html><body>
                <div style="font-family: Arial, sans-serif; padding: 20px;">
                    <h2>%s</h2>
                    <p>Hello,</p>
                    <p>%s</p>
                    <p>Best regards,<br/>Hospital Management Team</p>
                </div>
            </body></html>
            """, subject, text.replace("\n", "<br/>"));

        helper.setText(htmlContent, true);
        mailSender.send(message);
    }

    @Async
    public void sendAppointmentReminderEmail(String to, String patientName, String doctorName,
                                             String appointmentDate, String appointmentTime,
                                             String location) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject("Appointment Reminder - Hospital Management System");

        String htmlContent = String.format("""
            <html><head><style>body{font-family:Arial,sans-serif;line-height:1.6;color:#333}.container{width:600px;margin:0 auto;padding:20px;border:1px solid #ddd;border-radius:5px}.header{background-color:#FF9800;color:white;padding:15px;text-align:center;border-radius:5px 5px 0 0}.content{padding:20px}.appointment-details{background-color:#f9f9f9;padding:15px;border-left:4px solid #FF9800;margin:15px 0}.detail-row{margin:8px 0}.label{font-weight:bold;color:#555}.value{color:#333}.reminder-note{background-color:#fff3cd;border:1px solid #ffeaa7;padding:10px;border-radius:5px;margin-top:15px}</style></head><body><div class="container"><div class="header"><h1>🏥 Appointment Reminder</h1></div><div class="content"><h2>Hello %s,</h2><p>This is a friendly reminder about your upcoming appointment.</p><div class="appointment-details"><h3>📅 Appointment Details</h3><div class="detail-row"><span class="label">Doctor:</span> <span class="value">Dr. %s</span></div><div class="detail-row"><span class="label">Date:</span> <span class="value">%s</span></div><div class="detail-row"><span class="label">Time:</span> <span class="value">%s</span></div><div class="detail-row"><span class="label">Location:</span> <span class="value">%s</span></div></div><div class="reminder-note"><p><strong>📝 Important Reminders:</strong></p><ul><li>Please arrive 15 minutes early.</li><li>Bring a valid ID and your insurance card.</li><li>If you need to reschedule or cancel, please contact us at least 24 hours in advance.</li></ul></div><p>Best regards,<br/>Hospital Management Team</p></div></div></body></html>
            """, patientName, doctorName, appointmentDate, appointmentTime, location);

        helper.setText(htmlContent, true);
        mailSender.send(message);
    }

    @Async
    public void sendAppointmentCancellationEmail(Appointment appointment) throws MessagingException {
        String subject = "Important: Your Appointment has been Cancelled";
        String body = String.format(
                "Dear %s,<br/><br/>" +
                        "Please be advised that your appointment with Dr. %s on %s at %s has been cancelled by the doctor's office." +
                        "<br/><br/>Please contact our office to reschedule. We apologize for any inconvenience." +
                        "<br/><br/>Best regards,<br/>The Hospital Management Team",
                appointment.getPatient().getFullName(),
                appointment.getDoctor().getFullName(),
                appointment.getAppointmentDateTime().toLocalDate().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")),
                appointment.getAppointmentDateTime().toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm a"))
        );
        sendHtmlEmail(appointment.getPatient().getEmail(), subject, body, "#D32F2F");
    }

    @Async
    public void sendAppointmentRescheduleEmail(Appointment appointment, LocalDateTime oldDateTime) throws MessagingException {
        String subject = "Important: Your Appointment has been Rescheduled";
        String body = String.format(
                "Dear %s,<br/><br/>" +
                        "Please note that your appointment with Dr. %s, originally scheduled for %s at %s, has been rescheduled." +
                        "<br/><br/>Your new appointment time is: <strong>%s at %s</strong>." +
                        "<br/><br/>If this new time does not work for you, please contact our office immediately." +
                        "<br/><br/>Best regards,<br/>The Hospital Management Team",
                appointment.getPatient().getFullName(),
                appointment.getDoctor().getFullName(),
                oldDateTime.toLocalDate().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")),
                oldDateTime.toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm a")),
                appointment.getAppointmentDateTime().toLocalDate().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")),
                appointment.getAppointmentDateTime().toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm a"))
        );
        sendHtmlEmail(appointment.getPatient().getEmail(), subject, body, "#1976D2");
    }

    @Async
    public void sendWaitlistNotificationEmail(User patient, User doctor, LocalDate preferredDate) throws MessagingException {
        String subject = "An Appointment Slot is Now Available!";
        String body = String.format(
                "Dear %s,<br/><br/>" +
                        "Good news! A previously booked appointment slot has opened up with <strong>Dr. %s</strong> for <strong>%s</strong>." +
                        "<br/><br/>Please visit our patient portal as soon as possible to book an available time. Slots are filled on a first-come, first-served basis." +
                        "<br/><br/>Best regards,<br/>The Hospital Management Team",
                patient.getFullName(),
                doctor.getFullName(),
                preferredDate.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))
        );
        sendHtmlEmail(patient.getEmail(), subject, body, "#4CAF50");
    }

    @Async
    public void sendCancellationByPatientToDoctorEmail(Appointment appointment) throws MessagingException {
        String subject = "Patient Cancellation Notice - Appointment Update";
        String body = String.format(
                "Dear Dr. %s,<br/><br/>" +
                        "This is an automated notification to inform you that an appointment has been cancelled by a patient." +
                        "<br/><br/><strong>Cancelled Appointment Details:</strong>" +
                        "<ul>" +
                        "<li><strong>Patient Name:</strong> %s</li>" +
                        "<li><strong>Original Date:</strong> %s</li>" +
                        "<li><strong>Original Time:</strong> %s</li>" +
                        "</ul>" +
                        "This time slot is now available for other bookings." +
                        "<br/><br/>Best regards,<br/>Hospital Management System",
                appointment.getDoctor().getFullName(),
                appointment.getPatient().getFullName(),
                appointment.getAppointmentDateTime().toLocalDate().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")),
                appointment.getAppointmentDateTime().toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm a"))
        );
        sendHtmlEmail(appointment.getDoctor().getEmail(), subject, body, "#FF9800");
    }

    private void sendHtmlEmail(String to, String subject, String body, String headerColor) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        String htmlContent = String.format("""
            <html><head><style>body{font-family:Arial,sans-serif;line-height:1.6;color:#333}.container{width:600px;margin:20px auto;padding:0;border:1px solid #ddd;border-radius:5px}.header{background-color:%s;color:white;padding:15px;text-align:center;border-radius:5px 5px 0 0;font-size:24px}.content{padding:20px}</style></head><body><div class="container"><div class="header">%s</div><div class="content">%s</div></div></body></html>
            """, headerColor, subject, body);
        helper.setText(htmlContent, true);
        mailSender.send(message);
    }
}