package com.movieticket.controller;

import com.movieticket.dto.ShowtimeDto;
import com.movieticket.dto.TheaterDto;
import com.movieticket.service.ShowtimeService;
import com.movieticket.service.TheaterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/theaters")
public class TheaterController {

    private final TheaterService theaterService;
    private final ShowtimeService showtimeService;

    public TheaterController(TheaterService theaterService, ShowtimeService showtimeService) {
        this.theaterService = theaterService;
        this.showtimeService = showtimeService;
    }

    @GetMapping
    public ResponseEntity<List<TheaterDto>> getAllTheaters() {
        return ResponseEntity.ok(theaterService.getAllTheaters());
    }

    @GetMapping("/{theaterId}/showtimes")
    public ResponseEntity<List<ShowtimeDto>> getShowtimes(@PathVariable UUID theaterId) {
        return ResponseEntity.ok(showtimeService.getShowtimesByTheater(theaterId));
    }
}
