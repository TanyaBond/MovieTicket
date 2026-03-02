package com.movieticket.controller;

import com.movieticket.dto.CreateMovieRequest;
import com.movieticket.dto.MovieDto;
import com.movieticket.dto.ShowtimeDto;
import com.movieticket.dto.UpdateMovieRequest;
import com.movieticket.service.MovieService;
import com.movieticket.service.ShowtimeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
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

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovieDto> createMovie(@Valid @RequestBody CreateMovieRequest request) {
        MovieDto movieDto = movieService.createMovie(request);
        return ResponseEntity.created(URI.create("/api/movies/" + movieDto.id()))
                .body(movieDto);
    }

    @PutMapping("/{movieId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovieDto> updateMovie(
            @PathVariable UUID movieId,
            @Valid @RequestBody UpdateMovieRequest request) {
        MovieDto updatedMovie = movieService.updateMovie(movieId, request);
        return ResponseEntity.ok(updatedMovie);
    }

    @DeleteMapping("/{movieId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMovie(@PathVariable UUID movieId) {
        movieService.deleteMovie(movieId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{movieId}/showtimes")
    public ResponseEntity<List<ShowtimeDto>> getShowtimes(@PathVariable UUID movieId) {
        return ResponseEntity.ok(showtimeService.getShowtimesByMovie(movieId));
    }
}
