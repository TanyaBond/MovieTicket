package com.movieticket.integration;

import com.movieticket.entity.Movie;
import com.movieticket.entity.Screen;
import com.movieticket.entity.Showtime;
import com.movieticket.entity.Theater;
import com.movieticket.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BookingConcurrencyTest {

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
    private ReservedSeatRepository reservedSeatRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    private Showtime showtime;
    private Movie movie;
    private Theater theater;
    private Screen screen;

    @BeforeEach
    void setUp() {
        movie = movieRepository.save(new Movie("The Matrix"));
        theater = theaterRepository.save(new Theater("AMC Empire 25"));
        screen = screenRepository.save(new Screen(theater, "Screen 1"));
        showtime = showtimeRepository.save(
                new Showtime(movie, screen, LocalDateTime.of(2026, 3, 15, 19, 0)));
    }

    // Scenario 22: 10 threads race for the same seat — exactly 1 succeeds
    @Test
    void concurrentBooking_sameSeat_exactlyOneSucceeds() throws Exception {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);

        String bookingJson = """
                {"showtimeId": "%s", "seatIds": ["A0"]}
                """.formatted(showtime.getId());

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    MvcResult result = mockMvc.perform(post("/api/bookings")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(bookingJson))
                            .andReturn();

                    int status = result.getResponse().getStatus();
                    if (status == 201) {
                        successCount.incrementAndGet();
                    } else if (status == 409) {
                        conflictCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    // Unexpected exception — test will fail on assertions below
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // Release all threads at once
        startLatch.countDown();
        boolean completed = doneLatch.await(10, TimeUnit.SECONDS);

        executor.shutdown();

        assertThat(completed).isTrue();
        assertThat(successCount.get())
                .as("Exactly 1 thread should succeed")
                .isEqualTo(1);
        assertThat(conflictCount.get())
                .as("All other threads should get 409 Conflict")
                .isEqualTo(threadCount - 1);

        // Verify only 1 seat is booked
        mockMvc.perform(get("/api/showtimes/{showtimeId}/seats", showtime.getId()))
                .andExpect(jsonPath("$.availableCount").value(545));
    }

    // Scenario 23: 10 threads book different seats on same showtime — all succeed
    @Test
    void concurrentBooking_differentSeats_allSucceed() throws Exception {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final String seatId = "A" + i;  // A0, A1, A2, ..., A9
            executor.submit(() -> {
                try {
                    startLatch.await();
                    String bookingJson = """
                            {"showtimeId": "%s", "seatIds": ["%s"]}
                            """.formatted(showtime.getId(), seatId);

                    MvcResult result = mockMvc.perform(post("/api/bookings")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(bookingJson))
                            .andReturn();

                    if (result.getResponse().getStatus() == 201) {
                        successCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    // Unexpected
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        boolean completed = doneLatch.await(10, TimeUnit.SECONDS);

        executor.shutdown();

        assertThat(completed).isTrue();
        assertThat(successCount.get())
                .as("All 10 threads should succeed since they book different seats")
                .isEqualTo(threadCount);

        // Verify 10 seats are now booked
        mockMvc.perform(get("/api/showtimes/{showtimeId}/seats", showtime.getId()))
                .andExpect(jsonPath("$.availableCount").value(536));
    }

    // Scenario 24: Concurrent book and cancel on same showtime
    @Test
    void concurrentBookAndCancel_bothSucceed_finalStateConsistent() throws Exception {
        // Pre-book a seat to set up a reservation we can cancel
        String preBookJson = """
                {"showtimeId": "%s", "seatIds": ["Z20"]}
                """.formatted(showtime.getId());

        MvcResult preBookResult = mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(preBookJson))
                .andExpect(status().isCreated())
                .andReturn();

        String confirmationId = com.jayway.jsonpath.JsonPath
                .read(preBookResult.getResponse().getContentAsString(), "$.confirmationId");

        // Verify 1 seat is booked
        mockMvc.perform(get("/api/showtimes/{showtimeId}/seats", showtime.getId()))
                .andExpect(jsonPath("$.availableCount").value(545));

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);

        AtomicInteger bookSuccess = new AtomicInteger(0);
        AtomicInteger cancelSuccess = new AtomicInteger(0);

        // Thread 1: Book a new seat
        executor.submit(() -> {
            try {
                startLatch.await();
                String bookJson = """
                        {"showtimeId": "%s", "seatIds": ["A0"]}
                        """.formatted(showtime.getId());

                MvcResult result = mockMvc.perform(post("/api/bookings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(bookJson))
                        .andReturn();

                if (result.getResponse().getStatus() == 201) {
                    bookSuccess.incrementAndGet();
                }
            } catch (Exception e) {
                // Unexpected
            } finally {
                doneLatch.countDown();
            }
        });

        // Thread 2: Cancel the pre-booked reservation
        executor.submit(() -> {
            try {
                startLatch.await();
                MvcResult result = mockMvc.perform(
                                delete("/api/bookings/{confirmationId}", confirmationId))
                        .andReturn();

                if (result.getResponse().getStatus() == 204) {
                    cancelSuccess.incrementAndGet();
                }
            } catch (Exception e) {
                // Unexpected
            } finally {
                doneLatch.countDown();
            }
        });

        startLatch.countDown();
        boolean completed = doneLatch.await(10, TimeUnit.SECONDS);

        executor.shutdown();

        assertThat(completed).isTrue();
        assertThat(bookSuccess.get()).as("New booking should succeed").isEqualTo(1);
        assertThat(cancelSuccess.get()).as("Cancel should succeed").isEqualTo(1);

        // Final state: Z20 is released (cancelled), A0 is booked
        // Available = 546 - 1 (A0) = 545
        mockMvc.perform(get("/api/showtimes/{showtimeId}/seats", showtime.getId()))
                .andExpect(jsonPath("$.availableCount").value(545))
                .andExpect(jsonPath("$.availableSeats", org.hamcrest.Matchers.hasItem("Z20")))
                .andExpect(jsonPath("$.availableSeats",
                        org.hamcrest.Matchers.not(org.hamcrest.Matchers.hasItem("A0"))));
    }

    // Scenario 25: 10 threads book same seat on different showtimes — all succeed
    @Test
    void concurrentBooking_sameSeatDifferentShowtimes_allSucceed() throws Exception {
        // Create 10 showtimes (including the one from setUp)
        List<Showtime> showtimes = new ArrayList<>();
        showtimes.add(showtime);

        for (int i = 1; i < 10; i++) {
            Showtime st = showtimeRepository.save(
                    new Showtime(movie, screen,
                            LocalDateTime.of(2026, 3, 15, 10 + i, 0)));
            showtimes.add(st);
        }

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final UUID showtimeId = showtimes.get(i).getId();
            executor.submit(() -> {
                try {
                    startLatch.await();
                    String bookingJson = """
                            {"showtimeId": "%s", "seatIds": ["A5"]}
                            """.formatted(showtimeId);

                    MvcResult result = mockMvc.perform(post("/api/bookings")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(bookingJson))
                            .andReturn();

                    if (result.getResponse().getStatus() == 201) {
                        successCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    // Unexpected
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        boolean completed = doneLatch.await(10, TimeUnit.SECONDS);

        executor.shutdown();

        assertThat(completed).isTrue();
        assertThat(successCount.get())
                .as("All 10 should succeed — same seat but different showtimes have no conflict")
                .isEqualTo(threadCount);

        // Each showtime should have exactly 1 seat booked
        for (Showtime st : showtimes) {
            mockMvc.perform(get("/api/showtimes/{showtimeId}/seats", st.getId()))
                    .andExpect(jsonPath("$.availableCount").value(545));
        }
    }
}
