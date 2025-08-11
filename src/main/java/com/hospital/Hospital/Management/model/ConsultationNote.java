// src/main/java/com/hospital/Hospital/Management/doctor_management_module_3/model/ConsultationNote.java
package com.hospital.Hospital.Management.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "consultation_notes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false, unique = true)
    private Appointment appointment;

    @Lob // Use @Lob for potentially long text
    @Column(nullable = false)
    private String diagnosis;

    @Lob
    @Column(nullable = false)
    private String prescription;

    @Lob
    private String treatmentDetails;

    @Lob
    private String remarks;

    @CreationTimestamp
    private LocalDateTime createdAt;
}