package com.movieticket.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "theaters")
public class Theater {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "theater", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Screen> screens = new ArrayList<>();

    protected Theater() {
    }

    public Theater(String name) {
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Screen> getScreens() {
        return screens;
    }

    public void setScreens(List<Screen> screens) {
        this.screens = screens;
    }
}
