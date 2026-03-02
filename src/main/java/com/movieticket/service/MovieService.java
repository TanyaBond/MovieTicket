package com.movieticket.service;

import com.movieticket.dto.CreateMovieRequest;
import com.movieticket.dto.MovieDto;
import com.movieticket.dto.UpdateMovieRequest;
import com.movieticket.entity.Movie;
import com.movieticket.exception.MovieNotFoundException;
import com.movieticket.repository.MovieRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class MovieService {

    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public List<MovieDto> searchMovies(String title) {
        List<Movie> movies;
        if (title == null || title.isBlank()) {
            movies = movieRepository.findAll();
        } else {
            movies = movieRepository.findByTitleContainingIgnoreCase(title);
        }
        return movies.stream()
                .map(m -> new MovieDto(m.getId(), m.getTitle()))
                .toList();
    }

    public MovieDto createMovie(CreateMovieRequest request) {
        Movie movie = new Movie(request.title());
        Movie savedMovie = movieRepository.save(movie);
        return new MovieDto(savedMovie.getId(), savedMovie.getTitle());
    }

    public MovieDto updateMovie(UUID movieId, UpdateMovieRequest request) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new MovieNotFoundException(movieId));

        movie.setTitle(request.title());
        Movie updatedMovie = movieRepository.save(movie);
        return new MovieDto(updatedMovie.getId(), updatedMovie.getTitle());
    }

    public void deleteMovie(UUID movieId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new MovieNotFoundException(movieId));
        movieRepository.delete(movie);
    }
}
