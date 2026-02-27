package com.movieticket.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ReservationDto(
        UUID confirmationId,
        UUID showtimeId,
        String movieTitle,
        String theaterName,
        String screenLabel,
        LocalDateTime showDateTime,
        List<String> seatIds,
        LocalDateTime createdAt
) {
}
