package com.movieticket.repository;

import com.movieticket.entity.ReservedSeat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReservedSeatRepository extends JpaRepository<ReservedSeat, UUID> {

    List<ReservedSeat> findByShowtimeId(UUID showtimeId);
}
