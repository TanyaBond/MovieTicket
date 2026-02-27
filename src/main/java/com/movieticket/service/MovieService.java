package com.movieticket.service;

import com.movieticket.dto.MovieDto;
import com.movieticket.entity.Movie;
import com.movieticket.repository.MovieRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
