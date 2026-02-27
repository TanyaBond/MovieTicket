package com.movieticket.dto;

import java.util.List;
import java.util.UUID;

public record AvailableSeatsResponse(
        UUID showtimeId,
        int totalSeats,
        int availableCount,
        List<String> availableSeats
) {
}
