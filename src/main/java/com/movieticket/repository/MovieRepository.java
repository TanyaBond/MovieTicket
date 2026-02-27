package com.movieticket.repository;

import com.movieticket.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MovieRepository extends JpaRepository<Movie, UUID> {

    List<Movie> findByTitleContainingIgnoreCase(String title);
}
