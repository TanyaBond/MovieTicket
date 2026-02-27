package com.movieticket.exception;

import java.util.UUID;

public class ShowtimeNotFoundException extends RuntimeException {

    public ShowtimeNotFoundException(UUID showtimeId) {
        super("Showtime not found: " + showtimeId);
    }
}
