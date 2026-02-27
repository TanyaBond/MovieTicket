package com.movieticket.controller;

import com.movieticket.dto.AvailableSeatsResponse;
import com.movieticket.service.ShowtimeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/showtimes")
public class ShowtimeController {

    private final ShowtimeService showtimeService;

    public ShowtimeController(ShowtimeService showtimeService) {
        this.showtimeService = showtimeService;
    }

    @GetMapping("/{showtimeId}/seats")
    public ResponseEntity<AvailableSeatsResponse> getAvailableSeats(
            @PathVariable UUID showtimeId) {
        return ResponseEntity.ok(showtimeService.getAvailableSeats(showtimeId));
    }
}
