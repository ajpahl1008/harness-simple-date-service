package com.bespin.dateservice.controller;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import com.bespin.dateservice.dto.DateResponse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/** Plain unit tests for the controller's date logic, with no Spring context. */
class DateControllerUnitTest {

    private static final DateTimeFormatter RESPONSE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    /**
     * Invokes the controller with a clock fixed at the supplied UTC instant.
     *
     * @param instant an ISO-8601 instant
     * @return the controller response at that instant
     */
    private DateResponse currentDateAt(String instant) {
        Clock clock = Clock.fixed(Instant.parse(instant), ZoneOffset.UTC);
        return new DateController(clock).currentDate();
    }

    /** Verifies that the controller reads the complete UTC date-time from its clock. */
    @Test
    @DisplayName("Returns the clock's UTC date and time")
    void returnsClockDateTime() {
        assertThat(currentDateAt("2026-09-06T12:34:56.789Z").date())
                .isEqualTo(LocalDateTime.of(2026, 9, 6, 12, 34, 56, 789_000_000));
    }

    /**
     * Verifies fixed-width millisecond formatting for representative calendar boundaries.
     *
     * @param instant an ISO-8601 instant to format
     */
    @ParameterizedTest(name = "{0} -> yyyy-MM-dd''T''HH:mm:ss.SSS")
    @DisplayName("Formats every instant as ISO-8601 with millisecond precision")
    @ValueSource(strings = {
            "2026-01-01T00:00:00.000Z",
            "2024-02-29T23:59:59.999Z",
            "2026-12-31T23:59:59.500Z",
            "2026-09-06T12:34:56.789Z"})
    void formatsWithMillisecondPrecision(String instant) {
        LocalDateTime dateTime = currentDateAt(instant).date();
        assertThat(dateTime.format(RESPONSE_FORMAT))
                .matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}");
    }

    /** Verifies that whole seconds retain an explicit three-digit fractional part. */
    @Test
    @DisplayName("Whole seconds still render three fractional digits")
    void padsMillisecondsOnWholeSeconds() {
        // LocalDateTime.toString() would emit "2026-09-06T12:34:56" here, dropping the
        // fractional part entirely; the response format must keep the .000.
        LocalDateTime dateTime = currentDateAt("2026-09-06T12:34:56Z").date();
        assertThat(dateTime.format(RESPONSE_FORMAT)).isEqualTo("2026-09-06T12:34:56.000");
    }

    /** Verifies that significant trailing zeros in milliseconds are preserved. */
    @Test
    @DisplayName("Trailing zeros in the milliseconds are preserved")
    void keepsTrailingZeroMilliseconds() {
        // toString() would shorten .100 to .1
        LocalDateTime dateTime = currentDateAt("2026-09-06T12:34:56.100Z").date();
        assertThat(dateTime.format(RESPONSE_FORMAT)).isEqualTo("2026-09-06T12:34:56.100");
    }

    /** Verifies that precision below milliseconds is truncated rather than rounded. */
    @Test
    @DisplayName("Sub-millisecond precision is truncated, not rounded")
    void truncatesBelowMilliseconds() {
        LocalDateTime dateTime = currentDateAt("2026-09-06T12:34:56.789999Z").date();
        assertThat(dateTime.format(RESPONSE_FORMAT)).isEqualTo("2026-09-06T12:34:56.789");
    }

    /** Verifies that a valid leap-day value survives controller conversion and formatting. */
    @Test
    @DisplayName("Leap day is preserved")
    void handlesLeapDay() {
        assertThat(currentDateAt("2024-02-29T12:00:00.000Z").date().format(RESPONSE_FORMAT))
                .isEqualTo("2024-02-29T12:00:00.000");
    }

    /** Verifies exact values on both sides of the UTC midnight boundary. */
    @Test
    @DisplayName("Midnight and the last millisecond of the day are rendered correctly")
    void respectsUtcDayBoundary() {
        assertThat(currentDateAt("2026-09-06T23:59:59.999Z").date().format(RESPONSE_FORMAT))
                .isEqualTo("2026-09-06T23:59:59.999");
        assertThat(currentDateAt("2026-09-07T00:00:00.000Z").date().format(RESPONSE_FORMAT))
                .isEqualTo("2026-09-07T00:00:00.000");
    }

    /** Verifies that a millisecond-aligned response value round-trips without data loss. */
    @Test
    @DisplayName("The formatted value round-trips back to the same date-time")
    void roundTripsThroughTheFormat() {
        LocalDateTime dateTime = currentDateAt("2026-09-06T12:34:56.789Z").date();
        assertThat(LocalDateTime.parse(dateTime.format(RESPONSE_FORMAT), RESPONSE_FORMAT))
                .isEqualTo(dateTime);
    }
}
