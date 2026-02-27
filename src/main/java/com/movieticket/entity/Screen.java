package com.movieticket.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "screens")
public class Screen {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theater_id", nullable = false)
    private Theater theater;

    @Column(nullable = false)
    private String label;

    @OneToMany(mappedBy = "screen")
    private List<Showtime> showtimes = new ArrayList<>();

    protected Screen() {
    }

    public Screen(Theater theater, String label) {
        this.theater = theater;
        this.label = label;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Theater getTheater() {
        return theater;
    }

    public void setTheater(Theater theater) {
        this.theater = theater;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<Showtime> getShowtimes() {
        return showtimes;
    }

    public void setShowtimes(List<Showtime> showtimes) {
        this.showtimes = showtimes;
    }
}
