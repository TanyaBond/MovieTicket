package com.movieticket.controller;

import com.movieticket.dto.BookingRequest;
import com.movieticket.dto.ReservationDto;
import com.movieticket.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<ReservationDto> createBooking(
            @Valid @RequestBody BookingRequest request) {
        ReservationDto reservation = bookingService.createBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservation);
    }

    @DeleteMapping("/{confirmationId}")
    public ResponseEntity<Void> cancelBooking(@PathVariable UUID confirmationId) {
        bookingService.cancelBooking(confirmationId);
        return ResponseEntity.noContent().build();
    }
}
