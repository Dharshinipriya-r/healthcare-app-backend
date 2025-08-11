package com.hospital.Hospital.Management.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception thrown when a patient tries to book an appointment slot
 * that is either already taken or falls outside the doctor's set availability.
 * Responds with HTTP 409 Conflict.
 */
@ResponseStatus(HttpStatus.CONFLICT) // This tells Spring to default to a 409 status
public class SlotUnavailableException extends RuntimeException {
    public SlotUnavailableException(String message) {
        super(message);
    }
}