package com.movieticket.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ShowtimeDto(
        UUID id,
        UUID movieId,
        String movieTitle,
        UUID screenId,
        String screenLabel,
        UUID theaterId,
        String theaterName,
        LocalDateTime dateTime
) {
}
