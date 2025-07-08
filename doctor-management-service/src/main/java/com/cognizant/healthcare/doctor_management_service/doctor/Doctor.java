package com.cognizant.healthcare.doctor_management_service.doctor;

import jakarta.persistence.*;
import lombok.Data; // A Lombok annotation to create all the getters, setters, etc.

@Data // Generates getters, setters, toString(), equals(), and hashCode() methods
@Entity // Tells JPA that this class is an entity that maps to a database table
@Table(name = "doctors") // Specifies the name of the table in the database
public class Doctor {

    @Id // Marks this field as the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increments the ID
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String specialization;

    // This will store the doctor's weekly schedule.
    // We can store it as a simple text/JSON string for now.
    @Column(columnDefinition = "TEXT")
    private String weeklySchedule; // e.g., "Mon: 9am-5pm, Tue: 9am-1pm"

    // We also need a link to the User Management module's user ID.
    // For now, we'll assume it's just a number.
    private Long userId;
}