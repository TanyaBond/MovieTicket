package com.movieticket.exception;

import java.util.List;

public class InvalidSeatException extends RuntimeException {

    public InvalidSeatException(String message) {
        super(message);
    }

    public InvalidSeatException(List<String> seatIds) {
        super("Invalid seat IDs: " + seatIds);
    }
}
