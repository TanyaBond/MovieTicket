package com.movieticket.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "confirmation_id", nullable = false, unique = true, updatable = false)
    private UUID confirmationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "showtime_id", nullable = false)
    private Showtime showtime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReservedSeat> reservedSeats = new ArrayList<>();

    protected Reservation() {
    }

    public Reservation(Showtime showtime) {
        this.showtime = showtime;
    }

    @PrePersist
    protected void onCreate() {
        this.confirmationId = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getConfirmationId() {
        return confirmationId;
    }

    public Showtime getShowtime() {
        return showtime;
    }

    public void setShowtime(Showtime showtime) {
        this.showtime = showtime;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<ReservedSeat> getReservedSeats() {
        return reservedSeats;
    }

    public void setReservedSeats(List<ReservedSeat> reservedSeats) {
        this.reservedSeats = reservedSeats;
    }
}
