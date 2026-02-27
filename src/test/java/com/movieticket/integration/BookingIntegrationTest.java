package com.movieticket.integration;

import com.movieticket.entity.Movie;
import com.movieticket.entity.Screen;
import com.movieticket.entity.Showtime;
import com.movieticket.entity.Theater;
import com.movieticket.repository.*;
import com.movieticket.util.SeatLayout;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BookingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private TheaterRepository theaterRepository;

    @Autowired
    private ScreenRepository screenRepository;

    @Autowired
    private ShowtimeRepository showtimeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    private Movie matrix;
    private Movie inception;
    private Theater amcTheater;
    private Theater regalTheater;
    private Screen amcScreen1;
    private Screen regalScreen1;
    private Showtime matrixShowtime;
    private Showtime inceptionShowtime;
    private Showtime matrixAtRegal;

    @BeforeEach
    void setUp() {
        // Create movies
        matrix = movieRepository.save(new Movie("The Matrix"));
        inception = movieRepository.save(new Movie("Inception"));

        // Create theaters
        amcTheater = theaterRepository.save(new Theater("AMC Empire 25"));
        regalTheater = theaterRepository.save(new Theater("Regal Union Square"));

        // Create screens
        amcScreen1 = screenRepository.save(new Screen(amcTheater, "Screen 1"));
        regalScreen1 = screenRepository.save(new Screen(regalTheater, "Screen 1"));

        // Create showtimes
        matrixShowtime = showtimeRepository.save(
                new Showtime(matrix, amcScreen1, LocalDateTime.of(2026, 3, 15, 19, 0)));
        inceptionShowtime = showtimeRepository.save(
                new Showtime(inception, amcScreen1, LocalDateTime.of(2026, 3, 15, 21, 0)));
        matrixAtRegal = showtimeRepository.save(
                new Showtime(matrix, regalScreen1, LocalDateTime.of(2026, 3, 15, 20, 0)));
    }

    // ==========================================
    // Section 1: Positive Flow Tests
    // ==========================================

    @Nested
    class PositiveFlows {

        // Scenario 1: Search movies by title (case-insensitive substring)
        @Test
        void searchMovies_caseInsensitiveSubstring_findsMatch() throws Exception {
            mockMvc.perform(get("/api/movies").param("title", "matrix"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].title").value("The Matrix"))
                    .andExpect(jsonPath("$[0].id").value(matrix.getId().toString()));
        }

        @Test
        void searchMovies_uppercaseQuery_findsMatch() throws Exception {
            mockMvc.perform(get("/api/movies").param("title", "MATRIX"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].title").value("The Matrix"));
        }

        @Test
        void searchMovies_noParam_returnsAllMovies() throws Exception {
            mockMvc.perform(get("/api/movies"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)));
        }

        @Test
        void searchMovies_noMatch_returnsEmptyList() throws Exception {
            mockMvc.perform(get("/api/movies").param("title", "Nonexistent"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        // Scenario 2: List all theaters
        @Test
        void getAllTheaters_returnsTheatersWithScreens() throws Exception {
            mockMvc.perform(get("/api/theaters"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[*].name",
                            containsInAnyOrder("AMC Empire 25", "Regal Union Square")));
        }

        // Scenario 3: Get showtimes by movie
        @Test
        void getShowtimesByMovie_returnsAllShowtimesAcrossTheaters() throws Exception {
            mockMvc.perform(get("/api/movies/{movieId}/showtimes", matrix.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[*].theaterName",
                            containsInAnyOrder("AMC Empire 25", "Regal Union Square")));
        }

        // Scenario 4: Get showtimes by theater
        @Test
        void getShowtimesByTheater_returnsShowtimesAtThatTheater() throws Exception {
            mockMvc.perform(get("/api/theaters/{theaterId}/showtimes", amcTheater.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[*].movieTitle",
                            containsInAnyOrder("The Matrix", "Inception")));
        }

        // Scenario 5: View available seats for a fresh showtime
        @Test
        void getAvailableSeats_freshShowtime_returnsAll546Seats() throws Exception {
            mockMvc.perform(get("/api/showtimes/{showtimeId}/seats", matrixShowtime.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.showtimeId").value(matrixShowtime.getId().toString()))
                    .andExpect(jsonPath("$.totalSeats").value(SeatLayout.TOTAL_SEATS))
                    .andExpect(jsonPath("$.availableCount").value(546))
                    .andExpect(jsonPath("$.availableSeats", hasSize(546)));
        }

        // Scenario 6: Book a single seat
        @Test
        void bookSingleSeat_returns201WithConfirmationId() throws Exception {
            String bookingJson = """
                    {"showtimeId": "%s", "seatIds": ["A5"]}
                    """.formatted(matrixShowtime.getId());

            MvcResult result = mockMvc.perform(post("/api/bookings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(bookingJson))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.confirmationId").exists())
                    .andExpect(jsonPath("$.seatIds", hasSize(1)))
                    .andExpect(jsonPath("$.seatIds[0]").value("A5"))
                    .andExpect(jsonPath("$.movieTitle").value("The Matrix"))
                    .andExpect(jsonPath("$.theaterName").value("AMC Empire 25"))
                    .andReturn();

            // Verify the seat is now unavailable
            mockMvc.perform(get("/api/showtimes/{showtimeId}/seats", matrixShowtime.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.availableCount").value(545))
                    .andExpect(jsonPath("$.availableSeats", not(hasItem("A5"))));
        }

        // Scenario 7: Book multiple seats in one reservation
        @Test
        void bookMultipleSeats_allSeatInResponse_availableDrops() throws Exception {
            String bookingJson = """
                    {"showtimeId": "%s", "seatIds": ["A5", "A6", "A7"]}
                    """.formatted(matrixShowtime.getId());

            mockMvc.perform(post("/api/bookings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(bookingJson))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.seatIds", hasSize(3)))
                    .andExpect(jsonPath("$.seatIds", containsInAnyOrder("A5", "A6", "A7")));

            // Verify available seats dropped by 3
            mockMvc.perform(get("/api/showtimes/{showtimeId}/seats", matrixShowtime.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.availableCount").value(543));
        }

        // Scenario 8: Book seats at two different showtimes independently
        @Test
        void bookSeatsAtDifferentShowtimes_bothSucceed_seatsIndependent() throws Exception {
            // Book A5 at Matrix showtime
            String booking1 = """
                    {"showtimeId": "%s", "seatIds": ["A5"]}
                    """.formatted(matrixShowtime.getId());

            mockMvc.perform(post("/api/bookings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(booking1))
                    .andExpect(status().isCreated());

            // Book A5 at Inception showtime (same seat, different showtime — should succeed)
            String booking2 = """
                    {"showtimeId": "%s", "seatIds": ["A5"]}
                    """.formatted(inceptionShowtime.getId());

            mockMvc.perform(post("/api/bookings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(booking2))
                    .andExpect(status().isCreated());

            // Verify each showtime lost exactly 1 seat independently
            mockMvc.perform(get("/api/showtimes/{showtimeId}/seats", matrixShowtime.getId()))
                    .andExpect(jsonPath("$.availableCount").value(545));

            mockMvc.perform(get("/api/showtimes/{showtimeId}/seats", inceptionShowtime.getId()))
                    .andExpect(jsonPath("$.availableCount").value(545));
        }

        // Scenario 9: Cancel reservation releases seats
        @Test
        void cancelReservation_releasesSeats_returns204() throws Exception {
            // Book a seat
            String bookingJson = """
                    {"showtimeId": "%s", "seatIds": ["B10"]}
                    """.formatted(matrixShowtime.getId());

            MvcResult bookResult = mockMvc.perform(post("/api/bookings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(bookingJson))
                    .andExpect(status().isCreated())
                    .andReturn();

            String confirmationId = com.jayway.jsonpath.JsonPath
                    .read(bookResult.getResponse().getContentAsString(), "$.confirmationId");

            // Verify seat is unavailable
            mockMvc.perform(get("/api/showtimes/{showtimeId}/seats", matrixShowtime.getId()))
                    .andExpect(jsonPath("$.availableCount").value(545));

            // Cancel the reservation
            mockMvc.perform(delete("/api/bookings/{confirmationId}", confirmationId))
                    .andExpect(status().isNoContent());

            // Verify seat is available again
            mockMvc.perform(get("/api/showtimes/{showtimeId}/seats", matrixShowtime.getId()))
                    .andExpect(jsonPath("$.availableCount").value(546))
                    .andExpect(jsonPath("$.availableSeats", hasItem("B10")));
        }
    }

    // ==========================================
    // Section 2: Negative / Edge Case Tests
    // ==========================================

    @Nested
    class NegativeEdgeCases {

        // Scenario 10: Book already reserved seat — 409
        @Test
        void bookAlreadyReservedSeat_returns409WithSeatId() throws Exception {
            // First booking succeeds
            String booking1 = """
                    {"showtimeId": "%s", "seatIds": ["A5"]}
                    """.formatted(matrixShowtime.getId());

            mockMvc.perform(post("/api/bookings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(booking1))
                    .andExpect(status().isCreated());

            // Second booking for same seat fails with 409
            mockMvc.perform(post("/api/bookings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(booking1))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message", containsString("A5")));
        }

        // Scenario 11: Partial overlap — all-or-nothing atomicity
        @Test
        void bookWithPartialOverlap_entireRequestFails_noPartialBooking() throws Exception {
            // Book A5 and A6
            String booking1 = """
                    {"showtimeId": "%s", "seatIds": ["A5", "A6"]}
                    """.formatted(matrixShowtime.getId());

            mockMvc.perform(post("/api/bookings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(booking1))
                    .andExpect(status().isCreated());

            // Try to book A6 (taken) and A7 (free) — entire request should fail
            String booking2 = """
                    {"showtimeId": "%s", "seatIds": ["A6", "A7"]}
                    """.formatted(matrixShowtime.getId());

            mockMvc.perform(post("/api/bookings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(booking2))
                    .andExpect(status().isConflict());

            // Verify A7 is still available (not partially booked)
            mockMvc.perform(get("/api/showtimes/{showtimeId}/seats", matrixShowtime.getId()))
                    .andExpect(jsonPath("$.availableCount").value(544))  // only A5, A6 taken
                    .andExpect(jsonPath("$.availableSeats", hasItem("A7")));
        }

        // Scenario 12: Invalid seat IDs — 400
        @ParameterizedTest
        @ValueSource(strings = {"A99", "ZZ1", "a5", "A-1", "99"})
        void bookWithInvalidSeatId_returns400(String invalidSeat) throws Exception {
            String bookingJson = """
                    {"showtimeId": "%s", "seatIds": ["%s"]}
                    """.formatted(matrixShowtime.getId(), invalidSeat);

            mockMvc.perform(post("/api/bookings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(bookingJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsString(invalidSeat)));
        }

        // Scenario 13: Book on non-existent showtime — 404
        @Test
        void bookWithNonExistentShowtime_returns404() throws Exception {
            UUID fakeShowtimeId = UUID.randomUUID();
            String bookingJson = """
                    {"showtimeId": "%s", "seatIds": ["A5"]}
                    """.formatted(fakeShowtimeId);

            mockMvc.perform(post("/api/bookings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(bookingJson))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", containsString(fakeShowtimeId.toString())));
        }

        // Scenario 14: Cancel non-existent confirmation ID — 404
        @Test
        void cancelNonExistentReservation_returns404() throws Exception {
            UUID fakeConfirmationId = UUID.randomUUID();

            mockMvc.perform(delete("/api/bookings/{confirmationId}", fakeConfirmationId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message",
                            containsString(fakeConfirmationId.toString())));
        }

        // Scenario 15: Get seats for non-existent showtime — 404
        @Test
        void getSeatsForNonExistentShowtime_returns404() throws Exception {
            UUID fakeShowtimeId = UUID.randomUUID();

            mockMvc.perform(get("/api/showtimes/{showtimeId}/seats", fakeShowtimeId))
                    .andExpect(status().isNotFound());
        }

        // Scenario 16: Double cancel same reservation — second returns 404
        @Test
        void doubleCancelSameReservation_secondReturns404() throws Exception {
            // Book a seat
            String bookingJson = """
                    {"showtimeId": "%s", "seatIds": ["C3"]}
                    """.formatted(matrixShowtime.getId());

            MvcResult bookResult = mockMvc.perform(post("/api/bookings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(bookingJson))
                    .andExpect(status().isCreated())
                    .andReturn();

            String confirmationId = com.jayway.jsonpath.JsonPath
                    .read(bookResult.getResponse().getContentAsString(), "$.confirmationId");

            // First cancel succeeds
            mockMvc.perform(delete("/api/bookings/{confirmationId}", confirmationId))
                    .andExpect(status().isNoContent());

            // Second cancel returns 404
            mockMvc.perform(delete("/api/bookings/{confirmationId}", confirmationId))
                    .andExpect(status().isNotFound());
        }
    }

    // ==========================================
    // Section 3: Multi-Show and Cancellation
    // ==========================================

    @Nested
    class MultiShowAndCancellation {

        // Scenario 17: Book at 2 showtimes, cancel one — other booking untouched
        @Test
        void bookTwoShowtimes_cancelOne_otherUnaffected() throws Exception {
            // Book at Matrix showtime
            String booking1 = """
                    {"showtimeId": "%s", "seatIds": ["D1", "D2"]}
                    """.formatted(matrixShowtime.getId());

            MvcResult result1 = mockMvc.perform(post("/api/bookings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(booking1))
                    .andExpect(status().isCreated())
                    .andReturn();

            String confirmationId1 = com.jayway.jsonpath.JsonPath
                    .read(result1.getResponse().getContentAsString(), "$.confirmationId");

            // Book at Inception showtime
            String booking2 = """
                    {"showtimeId": "%s", "seatIds": ["E5", "E6"]}
                    """.formatted(inceptionShowtime.getId());

            mockMvc.perform(post("/api/bookings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(booking2))
                    .andExpect(status().isCreated());

            // Cancel the Matrix booking
            mockMvc.perform(delete("/api/bookings/{confirmationId}", confirmationId1))
                    .andExpect(status().isNoContent());

            // Matrix showtime seats are released
            mockMvc.perform(get("/api/showtimes/{showtimeId}/seats", matrixShowtime.getId()))
                    .andExpect(jsonPath("$.availableCount").value(546))
                    .andExpect(jsonPath("$.availableSeats", hasItem("D1")))
                    .andExpect(jsonPath("$.availableSeats", hasItem("D2")));

            // Inception showtime booking is untouched
            mockMvc.perform(get("/api/showtimes/{showtimeId}/seats", inceptionShowtime.getId()))
                    .andExpect(jsonPath("$.availableCount").value(544))
                    .andExpect(jsonPath("$.availableSeats", not(hasItem("E5"))))
                    .andExpect(jsonPath("$.availableSeats", not(hasItem("E6"))));
        }

        // Scenario 18: Rebook after cancellation
        @Test
        void rebookAfterCancellation_succeeds_newConfirmationId() throws Exception {
            // Book A5
            String bookingJson = """
                    {"showtimeId": "%s", "seatIds": ["A5"]}
                    """.formatted(matrixShowtime.getId());

            MvcResult firstBook = mockMvc.perform(post("/api/bookings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(bookingJson))
                    .andExpect(status().isCreated())
                    .andReturn();

            String firstConfirmationId = com.jayway.jsonpath.JsonPath
                    .read(firstBook.getResponse().getContentAsString(), "$.confirmationId");

            // Cancel
            mockMvc.perform(delete("/api/bookings/{confirmationId}", firstConfirmationId))
                    .andExpect(status().isNoContent());

            // Rebook A5
            MvcResult secondBook = mockMvc.perform(post("/api/bookings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(bookingJson))
                    .andExpect(status().isCreated())
                    .andReturn();

            String secondConfirmationId = com.jayway.jsonpath.JsonPath
                    .read(secondBook.getResponse().getContentAsString(), "$.confirmationId");

            // Confirmation IDs are different
            assertThat(secondConfirmationId).isNotEqualTo(firstConfirmationId);

            // Seat is booked again
            mockMvc.perform(get("/api/showtimes/{showtimeId}/seats", matrixShowtime.getId()))
                    .andExpect(jsonPath("$.availableCount").value(545))
                    .andExpect(jsonPath("$.availableSeats", not(hasItem("A5"))));
        }

        // Scenario 19: Duplicate seat IDs in request — 400
        @Test
        void bookWithDuplicateSeatIds_returns400() throws Exception {
            String bookingJson = """
                    {"showtimeId": "%s", "seatIds": ["A5", "A5"]}
                    """.formatted(matrixShowtime.getId());

            mockMvc.perform(post("/api/bookings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(bookingJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsStringIgnoringCase("duplicate")));
        }

        // Scenario 20: Empty seat list — 400
        @Test
        void bookWithEmptySeatList_returns400() throws Exception {
            String bookingJson = """
                    {"showtimeId": "%s", "seatIds": []}
                    """.formatted(matrixShowtime.getId());

            mockMvc.perform(post("/api/bookings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(bookingJson))
                    .andExpect(status().isBadRequest());
        }

        // Scenario 21: Missing showtime ID — 400
        @Test
        void bookWithMissingShowtimeId_returns400() throws Exception {
            String bookingJson = """
                    {"seatIds": ["A5"]}
                    """;

            mockMvc.perform(post("/api/bookings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(bookingJson))
                    .andExpect(status().isBadRequest());
        }
    }
}
