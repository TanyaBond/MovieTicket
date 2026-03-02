package com.movieticket.exception;

import java.util.UUID;

public class MovieNotFoundException extends RuntimeException {

    public MovieNotFoundException(UUID movieId) {
        super("Movie not found: " + movieId);
    }

    public MovieNotFoundException(String message) {
        super(message);
    }
}
