package com.movieticket.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "reserved_seats", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"showtime_id", "seat_id"})
})
public class ReservedSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "showtime_id", nullable = false)
    private Showtime showtime;

    @Column(name = "seat_id", nullable = false)
    private String seatId;

    protected ReservedSeat() {
    }

    public ReservedSeat(Reservation reservation, Showtime showtime, String seatId) {
        this.reservation = reservation;
        this.showtime = showtime;
        this.seatId = seatId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public Showtime getShowtime() {
        return showtime;
    }

    public void setShowtime(Showtime showtime) {
        this.showtime = showtime;
    }

    public String getSeatId() {
        return seatId;
    }

    public void setSeatId(String seatId) {
        this.seatId = seatId;
    }
}
