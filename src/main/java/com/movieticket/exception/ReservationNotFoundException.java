package com.movieticket.exception;

import java.util.UUID;

public class ReservationNotFoundException extends RuntimeException {

    public ReservationNotFoundException(UUID confirmationId) {
        super("Reservation not found with confirmation ID: " + confirmationId);
    }
}
