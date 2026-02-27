package com.movieticket.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record BookingRequest(
        @NotNull(message = "Showtime ID is required")
        UUID showtimeId,

        @NotEmpty(message = "At least one seat must be selected")
        List<String> seatIds
) {
}
