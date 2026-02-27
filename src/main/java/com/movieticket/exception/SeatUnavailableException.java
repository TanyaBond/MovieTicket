package com.movieticket.exception;

import java.util.List;

public class SeatUnavailableException extends RuntimeException {

    public SeatUnavailableException(List<String> seatIds) {
        super("Seats already booked: " + seatIds);
    }
}
