package com.movieticket.service;

import com.movieticket.dto.BookingRequest;
import com.movieticket.dto.ReservationDto;
import com.movieticket.entity.Reservation;
import com.movieticket.entity.ReservedSeat;
import com.movieticket.entity.Showtime;
import com.movieticket.exception.InvalidSeatException;
import com.movieticket.exception.ReservationNotFoundException;
import com.movieticket.exception.SeatUnavailableException;
import com.movieticket.exception.ShowtimeNotFoundException;
import com.movieticket.repository.ReservationRepository;
import com.movieticket.repository.ReservedSeatRepository;
import com.movieticket.repository.ShowtimeRepository;
import com.movieticket.util.SeatLayout;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final ShowtimeRepository showtimeRepository;
    private final ReservationRepository reservationRepository;
    private final ReservedSeatRepository reservedSeatRepository;

    public BookingService(ShowtimeRepository showtimeRepository,
                          ReservationRepository reservationRepository,
                          ReservedSeatRepository reservedSeatRepository) {
        this.showtimeRepository = showtimeRepository;
        this.reservationRepository = reservationRepository;
        this.reservedSeatRepository = reservedSeatRepository;
    }

    @Transactional
    public ReservationDto createBooking(BookingRequest request) {
        Showtime showtime = showtimeRepository.findById(request.showtimeId())
                .orElseThrow(() -> new ShowtimeNotFoundException(request.showtimeId()));

        List<String> invalidSeats = request.seatIds().stream()
                .filter(s -> !SeatLayout.isValidSeatId(s))
                .toList();
        if (!invalidSeats.isEmpty()) {
            throw new InvalidSeatException(invalidSeats);
        }

        Set<String> uniqueSeats = new HashSet<>(request.seatIds());
        if (uniqueSeats.size() != request.seatIds().size()) {
            throw new InvalidSeatException("Duplicate seat IDs in request");
        }

        Set<String> takenSeats = reservedSeatRepository.findByShowtimeId(showtime.getId()).stream()
                .map(ReservedSeat::getSeatId)
                .collect(Collectors.toSet());

        List<String> alreadyTaken = request.seatIds().stream()
                .filter(takenSeats::contains)
                .toList();
        if (!alreadyTaken.isEmpty()) {
            throw new SeatUnavailableException(alreadyTaken);
        }

        Reservation reservation = new Reservation(showtime);

        for (String seatId : request.seatIds()) {
            ReservedSeat rs = new ReservedSeat(reservation, showtime, seatId);
            reservation.getReservedSeats().add(rs);
        }

        reservation = reservationRepository.save(reservation);

        return toDto(reservation);
    }

    @Transactional
    public void cancelBooking(UUID confirmationId) {
        Reservation reservation = reservationRepository.findByConfirmationId(confirmationId)
                .orElseThrow(() -> new ReservationNotFoundException(confirmationId));

        reservationRepository.delete(reservation);
    }

    private ReservationDto toDto(Reservation r) {
        Showtime s = r.getShowtime();
        return new ReservationDto(
                r.getConfirmationId(),
                s.getId(),
                s.getMovie().getTitle(),
                s.getScreen().getTheater().getName(),
                s.getScreen().getLabel(),
                s.getDateTime(),
                r.getReservedSeats().stream()
                        .map(ReservedSeat::getSeatId)
                        .sorted()
                        .toList(),
                r.getCreatedAt()
        );
    }
}
