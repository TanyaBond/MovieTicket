package com.movieticket.exception;

import com.movieticket.dto.ErrorResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ShowtimeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleShowtimeNotFound(ShowtimeNotFoundException ex) {
        return ResponseEntity.status(404)
                .body(new ErrorResponse(404, "Not Found", ex.getMessage()));
    }

    @ExceptionHandler(ReservationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleReservationNotFound(ReservationNotFoundException ex) {
        return ResponseEntity.status(404)
                .body(new ErrorResponse(404, "Not Found", ex.getMessage()));
    }

    @ExceptionHandler(InvalidSeatException.class)
    public ResponseEntity<ErrorResponse> handleInvalidSeat(InvalidSeatException ex) {
        return ResponseEntity.status(400)
                .body(new ErrorResponse(400, "Bad Request", ex.getMessage()));
    }

    @ExceptionHandler(SeatUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleSeatUnavailable(SeatUnavailableException ex) {
        return ResponseEntity.status(409)
                .body(new ErrorResponse(409, "Conflict", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(400)
                .body(new ErrorResponse(400, "Validation Error", message));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        return ResponseEntity.status(409)
                .body(new ErrorResponse(409, "Conflict",
                        "One or more seats were booked by another user. Please try again."));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.status(400)
                .body(new ErrorResponse(400, "Bad Request", "Invalid parameter: " + ex.getName()));
    }
}
