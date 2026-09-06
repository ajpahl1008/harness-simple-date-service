package com.bespin.dateservice.controller;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
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

    private DateResponse currentDateAt(String instant) {
        Clock clock = Clock.fixed(Instant.parse(instant), ZoneOffset.UTC);
        return new DateController(clock).currentDate();
    }

    @Test
    @DisplayName("Returns the clock's UTC date")
    void returnsClockDate() {
        assertThat(currentDateAt("2026-09-06T00:00:00Z").date()).isEqualTo(LocalDate.of(2026, 9, 6));
    }

    @ParameterizedTest(name = "{0} -> ISO date")
    @ValueSource(strings = {"2026-01-01T00:00:00Z", "2024-02-29T23:59:59Z", "2026-12-31T23:59:59Z"})
    @DisplayName("Formats every date as ISO-8601 yyyy-MM-dd")
    void formatsAsIso8601(String instant) {
        LocalDate date = currentDateAt(instant).date();
        assertThat(date.format(DateTimeFormatter.ISO_LOCAL_DATE)).matches("\\d{4}-\\d{2}-\\d{2}");
        assertThat(LocalDate.parse(date.toString(), DateTimeFormatter.ISO_LOCAL_DATE)).isEqualTo(date);
    }

    @Test
    @DisplayName("Leap day is preserved")
    void handlesLeapDay() {
        assertThat(currentDateAt("2024-02-29T12:00:00Z").date()).hasToString("2024-02-29");
    }

    @Test
    @DisplayName("Just before UTC midnight the date has not yet rolled over")
    void respectsUtcDayBoundary() {
        assertThat(currentDateAt("2026-09-06T23:59:59Z").date()).hasToString("2026-09-06");
        assertThat(currentDateAt("2026-09-07T00:00:00Z").date()).hasToString("2026-09-07");
    }
}
