package com.movieticket.controller;

import com.movieticket.dto.MovieDto;
import com.movieticket.dto.ShowtimeDto;
import com.movieticket.service.MovieService;
import com.movieticket.service.ShowtimeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieService movieService;
    private final ShowtimeService showtimeService;

    public MovieController(MovieService movieService, ShowtimeService showtimeService) {
        this.movieService = movieService;
        this.showtimeService = showtimeService;
    }

    @GetMapping
    public ResponseEntity<List<MovieDto>> searchMovies(
            @RequestParam(required = false) String title) {
        return ResponseEntity.ok(movieService.searchMovies(title));
    }

    @GetMapping("/{movieId}/showtimes")
    public ResponseEntity<List<ShowtimeDto>> getShowtimes(@PathVariable UUID movieId) {
        return ResponseEntity.ok(showtimeService.getShowtimesByMovie(movieId));
    }
}
