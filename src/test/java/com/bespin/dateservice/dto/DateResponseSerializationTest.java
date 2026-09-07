package com.bespin.dateservice.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies how Jackson actually renders {@link DateResponse}. The controller tests pin a single
 * instant with non-zero milliseconds; these cover the edges where {@code LocalDateTime.toString()}
 * and the declared {@code @JsonFormat} pattern disagree.
 */
class DateResponseSerializationTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    private String serialise(LocalDateTime dateTime) throws Exception {
        return objectMapper.writeValueAsString(new DateResponse(dateTime));
    }

    @Test
    @DisplayName("Milliseconds are rendered as three digits")
    void rendersMilliseconds() throws Exception {
        assertThat(serialise(LocalDateTime.of(2026, 9, 6, 12, 34, 56, 789_000_000)))
                .isEqualTo("{\"date\":\"2026-09-06T12:34:56.789\"}");
    }

    @Test
    @DisplayName("A whole second is padded to .000 rather than dropping the fraction")
    void padsWholeSeconds() throws Exception {
        assertThat(serialise(LocalDateTime.of(2026, 9, 6, 12, 34, 56, 0)))
                .isEqualTo("{\"date\":\"2026-09-06T12:34:56.000\"}");
    }

    @Test
    @DisplayName("Trailing zeros are kept (.100, not .1)")
    void keepsTrailingZeros() throws Exception {
        assertThat(serialise(LocalDateTime.of(2026, 9, 6, 12, 34, 56, 100_000_000)))
                .isEqualTo("{\"date\":\"2026-09-06T12:34:56.100\"}");
    }

    @Test
    @DisplayName("Single-digit months, days, hours and minutes are zero padded")
    void zeroPadsAllFields() throws Exception {
        assertThat(serialise(LocalDateTime.of(2026, 1, 2, 3, 4, 5, 6_000_000)))
                .isEqualTo("{\"date\":\"2026-01-02T03:04:05.006\"}");
    }
}
