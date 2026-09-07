package com.bespin.dateservice.controller;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import com.bespin.dateservice.metrics.DateRequestCounter;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Slice tests for {@link DateController} against a fixed clock, so the expected date is exact
 * rather than "whatever today happens to be".
 */
@WebMvcTest(DateController.class)
class DateControllerTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2026-09-06T12:34:56Z");

    /**
     * Test configuration supplying the controller's collaborators: a fixed clock for deterministic
     * dates and a request counter backed by an in-memory meter registry, since the {@code @WebMvcTest}
     * slice does not scan the application's own components.
     */
    @TestConfiguration
    static class FixedClockConfig {
        /**
         * Provides a fixed clock bean set to a specific instant.
         *
         * @return a Clock fixed at the test instant in UTC
         */
        @Bean
        Clock clock() {
            return Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC);
        }

        /**
         * Provides an in-memory meter registry for the request counter.
         *
         * @return a SimpleMeterRegistry holding meters for the duration of the test
         */
        @Bean
        MeterRegistry meterRegistry() {
            return new SimpleMeterRegistry();
        }

        /**
         * Provides the request counter the controller increments.
         *
         * @param meterRegistry the registry the counter registers itself with
         * @return a DateRequestCounter starting at zero
         */
        @Bean
        DateRequestCounter dateRequestCounter(MeterRegistry meterRegistry) {
            return new DateRequestCounter(meterRegistry);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DateRequestCounter requestCounter;

    /**
     * Verifies that the date endpoint returns HTTP 200 with JSON content type.
     */
    @Test
    @DisplayName("GET /api/v1/date returns 200 with JSON content type")
    void returnsJson() throws Exception {
        mockMvc.perform(get("/api/v1/date"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    /**
     * Verifies that the date endpoint returns the date in ISO-8601 format.
     */
    @Test
    @DisplayName("GET /api/v1/date returns the clock's date in ISO-8601 format")
    void returnsCurrentDateInIsoFormat() throws Exception {
        mockMvc.perform(get("/api/v1/date"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2026-09-06"))
                .andExpect(jsonPath("$.date").value(org.hamcrest.Matchers.matchesPattern("\\d{4}-\\d{2}-\\d{2}")));
    }

    /**
     * Verifies that the response body contains exactly the expected date field and no other fields.
     */
    @Test
    @DisplayName("Response body contains only the date key")
    void responseContainsOnlyDateKey() throws Exception {
        mockMvc.perform(get("/api/v1/date"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"date\":\"2026-09-06\"}", JsonCompareMode.STRICT));
    }

    /**
     * Verifies that requests served through the web layer are recorded on the request counter.
     */
    @Test
    @DisplayName("Serving GET /api/v1/date increments the request counter")
    void incrementsCounterOnRequest() throws Exception {
        long before = requestCounter.count();

        mockMvc.perform(get("/api/v1/date")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/date")).andExpect(status().isOk());

        assertThat(requestCounter.count()).isEqualTo(before + 2);
    }
}
