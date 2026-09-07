package com.bespin.dateservice.controller;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import com.bespin.dateservice.dto.DateResponse;
import com.bespin.dateservice.metrics.DateRequestCounter;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/** Plain unit tests for the controller's date logic, with no Spring context. */
class DateControllerUnitTest {

    /**
     * Helper method to get the current date response for a specific instant.
     *
     * @param instant the ISO-8601 instant string to use for the fixed clock
     * @return DateResponse from the controller at the given instant
     */
    private DateResponse currentDateAt(String instant) {
        Clock clock = Clock.fixed(Instant.parse(instant), ZoneOffset.UTC);
        return new DateController(clock, newCounter()).currentDate();
    }

    /**
     * Creates a request counter backed by a throwaway in-memory meter registry.
     *
     * @return a fresh DateRequestCounter starting at zero
     */
    private DateRequestCounter newCounter() {
        return new DateRequestCounter(new SimpleMeterRegistry());
    }

    /**
     * Verifies that the controller returns the date from the injected clock.
     */
    @Test
    @DisplayName("Returns the clock's UTC date")
    void returnsClockDate() {
        assertThat(currentDateAt("2026-09-06T00:00:00Z").date()).isEqualTo(LocalDate.of(2026, 9, 6));
    }

    /**
     * Verifies that dates are formatted as ISO-8601 yyyy-MM-dd strings.
     */
    @ParameterizedTest(name = "{0} -> ISO date")
    @ValueSource(strings = {"2026-01-01T00:00:00Z", "2024-02-29T23:59:59Z", "2026-12-31T23:59:59Z"})
    @DisplayName("Formats every date as ISO-8601 yyyy-MM-dd")
    void formatsAsIso8601(String instant) {
        LocalDate date = currentDateAt(instant).date();
        assertThat(date.format(DateTimeFormatter.ISO_LOCAL_DATE)).matches("\\d{4}-\\d{2}-\\d{2}");
        assertThat(LocalDate.parse(date.toString(), DateTimeFormatter.ISO_LOCAL_DATE)).isEqualTo(date);
    }

    /**
     * Verifies that leap day dates are handled correctly.
     */
    @Test
    @DisplayName("Leap day is preserved")
    void handlesLeapDay() {
        assertThat(currentDateAt("2024-02-29T12:00:00Z").date()).hasToString("2024-02-29");
    }

    /**
     * Verifies that the UTC day boundary is respected when determining the current date.
     */
    @Test
    @DisplayName("Just before UTC midnight the date has not yet rolled over")
    void respectsUtcDayBoundary() {
        assertThat(currentDateAt("2026-09-06T23:59:59Z").date()).hasToString("2026-09-06");
        assertThat(currentDateAt("2026-09-07T00:00:00Z").date()).hasToString("2026-09-07");
    }

    /**
     * Verifies that each served request increments the request counter by exactly one.
     */
    @Test
    @DisplayName("Each call increments the request counter")
    void incrementsRequestCounterPerCall() {
        Clock clock = Clock.fixed(Instant.parse("2026-09-06T12:00:00Z"), ZoneOffset.UTC);
        DateRequestCounter counter = newCounter();
        DateController controller = new DateController(clock, counter);

        assertThat(counter.count()).isZero();
        controller.currentDate();
        assertThat(counter.count()).isEqualTo(1L);
        controller.currentDate();
        controller.currentDate();
        assertThat(counter.count()).isEqualTo(3L);
    }
}
