package com.movieticket.repository;

import com.movieticket.entity.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ShowtimeRepository extends JpaRepository<Showtime, UUID> {

    List<Showtime> findByMovieId(UUID movieId);

    List<Showtime> findByScreenTheaterId(UUID theaterId);
}
