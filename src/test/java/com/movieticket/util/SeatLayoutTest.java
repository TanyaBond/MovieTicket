package com.movieticket.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SeatLayoutTest {

    @Test
    void constants_areCorrect() {
        assertThat(SeatLayout.TOTAL_ROWS).isEqualTo(26);
        assertThat(SeatLayout.SEATS_PER_ROW).isEqualTo(21);
        assertThat(SeatLayout.TOTAL_SEATS).isEqualTo(546);
    }

    @Test
    void allSeatIds_returnsCorrectCount() {
        List<String> seats = SeatLayout.allSeatIds();
        assertThat(seats).hasSize(546);
    }

    @Test
    void allSeatIds_firstSeatIsA0() {
        List<String> seats = SeatLayout.allSeatIds();
        assertThat(seats.getFirst()).isEqualTo("A0");
    }

    @Test
    void allSeatIds_lastSeatIsZ20() {
        List<String> seats = SeatLayout.allSeatIds();
        assertThat(seats.getLast()).isEqualTo("Z20");
    }

    @Test
    void allSeatIds_containsNoDuplicates() {
        List<String> seats = SeatLayout.allSeatIds();
        assertThat(seats).doesNotHaveDuplicates();
    }

    @ParameterizedTest
    @ValueSource(strings = {"A0", "A20", "Z0", "Z20", "M10", "A1", "B15"})
    void isValidSeatId_validSeats_returnsTrue(String seatId) {
        assertThat(SeatLayout.isValidSeatId(seatId)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"A21", "A99", "ZZ1", "a5", "5A", "AA0", "[0", "A-1", " ", "AB"})
    void isValidSeatId_invalidSeats_returnsFalse(String seatId) {
        assertThat(SeatLayout.isValidSeatId(seatId)).isFalse();
    }

    @Test
    void isValidSeatId_null_returnsFalse() {
        assertThat(SeatLayout.isValidSeatId(null)).isFalse();
    }

    @Test
    void isValidSeatId_emptyString_returnsFalse() {
        assertThat(SeatLayout.isValidSeatId("")).isFalse();
    }
}
