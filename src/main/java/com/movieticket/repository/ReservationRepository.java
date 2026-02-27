package com.movieticket.repository;

import com.movieticket.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    Optional<Reservation> findByConfirmationId(UUID confirmationId);
}
