package com.movieticket.repository;

import com.movieticket.entity.Theater;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TheaterRepository extends JpaRepository<Theater, UUID> {
}
