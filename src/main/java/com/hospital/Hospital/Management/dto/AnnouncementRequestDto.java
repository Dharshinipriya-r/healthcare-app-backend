package com.hospital.Hospital.Management.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AnnouncementRequestDto {

    @NotBlank(message = "Subject cannot be blank")
    private String subject;

    @NotBlank(message = "Message body cannot be blank")
    private String message;
}