package com.hospital.Hospital.Management.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import com.hospital.Hospital.Management.dto.AnnouncementRequestDto;
import com.hospital.Hospital.Management.dto.DashboardAnalyticsDto;
import com.hospital.Hospital.Management.model.Appointment;
import com.hospital.Hospital.Management.model.AppointmentStatus;
import com.hospital.Hospital.Management.model.User;
import com.hospital.Hospital.Management.repository.AppointmentRepository;
import com.hospital.Hospital.Management.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class AdminDashboardServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private LogService logService;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AdminDashboardService adminDashboardService;

    private User testUser;
    private Appointment testAppointment;
    private UserDetails adminDetails;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .fullName("Test User")
                .enabled(true)
                .accountNonLocked(true)
                .build();

        testAppointment = Appointment.builder()
                .id(1L)
                .status(AppointmentStatus.SCHEDULED)
                .build();

        adminDetails = mock(UserDetails.class);
        when(adminDetails.getUsername()).thenReturn("admin@example.com");
    }

    @Test
    void getDashboardAnalytics_ShouldReturnCorrectCounts() {
        // Arrange
        List<User> users = Arrays.asList(
            testUser
        );
        List<Appointment> appointments = Arrays.asList(
            testAppointment,
            Appointment.builder().status(AppointmentStatus.COMPLETED).build()
        );

        when(userRepository.findAll()).thenReturn(users);
        when(appointmentRepository.findAll()).thenReturn(appointments);

        // Act
        DashboardAnalyticsDto result = adminDashboardService.getDashboardAnalytics();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalUsers());
        assertEquals(1, result.getTotalDoctors());
        assertEquals(1, result.getTotalPatients());
        assertEquals(2, result.getTotalAppointments());
        assertEquals(1, result.getScheduledAppointments());
        assertEquals(1, result.getCompletedAppointments());
        assertEquals(0, result.getCanceledAppointments());
    }

    @Test
    void sendSystemAnnouncement_ShouldSendToAllActiveUsers() {
        // Arrange
        List<User> activeUsers = Arrays.asList(testUser);
        AnnouncementRequestDto announcement = new AnnouncementRequestDto();
        announcement.setSubject("Test Announcement");
        announcement.setMessage("Test Message");

        when(userRepository.findAll()).thenReturn(activeUsers);
        when(userRepository.findByEmail(adminDetails.getUsername())).thenReturn(Optional.of(testUser));

        // Act
        adminDashboardService.sendSystemAnnouncement(announcement, adminDetails);

        // Assert
      
        verify(logService).logActivity(
            any(User.class),
            eq("SYSTEM_ANNOUNCEMENT_SENT"),
            contains("Test Announcement")
        );
    }
}
