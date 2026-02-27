package com.movieticket.service;

import com.movieticket.dto.AvailableSeatsResponse;
import com.movieticket.dto.ShowtimeDto;
import com.movieticket.entity.ReservedSeat;
import com.movieticket.entity.Showtime;
import com.movieticket.exception.ShowtimeNotFoundException;
import com.movieticket.repository.ReservedSeatRepository;
import com.movieticket.repository.ShowtimeRepository;
import com.movieticket.util.SeatLayout;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final ReservedSeatRepository reservedSeatRepository;

    public ShowtimeService(ShowtimeRepository showtimeRepository,
                           ReservedSeatRepository reservedSeatRepository) {
        this.showtimeRepository = showtimeRepository;
        this.reservedSeatRepository = reservedSeatRepository;
    }

    public List<ShowtimeDto> getShowtimesByMovie(UUID movieId) {
        return showtimeRepository.findByMovieId(movieId).stream()
                .map(this::toDto)
                .toList();
    }

    public List<ShowtimeDto> getShowtimesByTheater(UUID theaterId) {
        return showtimeRepository.findByScreenTheaterId(theaterId).stream()
                .map(this::toDto)
                .toList();
    }

    public AvailableSeatsResponse getAvailableSeats(UUID showtimeId) {

        Set<String> takenSeats = reservedSeatRepository.findByShowtimeId(showtimeId).stream()
                .map(ReservedSeat::getSeatId)
                .collect(Collectors.toSet());

        List<String> availableSeats = SeatLayout.allSeatIds().stream()
                .filter(seat -> !takenSeats.contains(seat))
                .toList();

        return new AvailableSeatsResponse(
                showtimeId,
                SeatLayout.TOTAL_SEATS,
                availableSeats.size(),
                availableSeats
        );
    }

    private ShowtimeDto toDto(Showtime s) {
        return new ShowtimeDto(
                s.getId(),
                s.getMovie().getId(),
                s.getMovie().getTitle(),
                s.getScreen().getId(),
                s.getScreen().getLabel(),
                s.getScreen().getTheater().getId(),
                s.getScreen().getTheater().getName(),
                s.getDateTime()
        );
    }
}
