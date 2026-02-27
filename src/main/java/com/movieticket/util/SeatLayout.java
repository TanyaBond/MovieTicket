package com.movieticket.util;

import java.util.ArrayList;
import java.util.List;

public final class SeatLayout {

    public static final char FIRST_ROW = 'A';
    public static final char LAST_ROW = 'Z';
    public static final int FIRST_SEAT = 0;
    public static final int LAST_SEAT = 20;
    public static final int SEATS_PER_ROW = LAST_SEAT - FIRST_SEAT + 1;
    public static final int TOTAL_ROWS = LAST_ROW - FIRST_ROW + 1;
    public static final int TOTAL_SEATS = TOTAL_ROWS * SEATS_PER_ROW;

    private SeatLayout() {
    }

    public static List<String> allSeatIds() {
        List<String> seats = new ArrayList<>(TOTAL_SEATS);
        for (char row = FIRST_ROW; row <= LAST_ROW; row++) {
            for (int num = FIRST_SEAT; num <= LAST_SEAT; num++) {
                seats.add(String.valueOf(row) + num);
            }
        }
        return seats;
    }

    public static boolean isValidSeatId(String seatId) {
        if (seatId == null || seatId.length() < 2 || seatId.length() > 3) {
            return false;
        }
        char row = seatId.charAt(0);
        if (row < FIRST_ROW || row > LAST_ROW) {
            return false;
        }
        try {
            int num = Integer.parseInt(seatId.substring(1));
            return num >= FIRST_SEAT && num <= LAST_SEAT;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
