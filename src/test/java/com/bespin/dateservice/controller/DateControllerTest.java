package com.bespin.dateservice.controller;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Slice tests for {@link DateController} against a fixed clock, so the expected date-time is
 * exact rather than "whatever now happens to be".
 */
@WebMvcTest(DateController.class)
class DateControllerTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2026-09-06T12:34:56.789Z");

    @TestConfiguration
    static class FixedClockConfig {
        /**
         * Replaces the application clock with a fixed one so responses are deterministic.
         *
         * @return a Clock fixed at {@code 2026-09-06T12:34:56.789Z}
         */
        @Bean
        Clock clock() {
            return Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/v1/date returns 200 with JSON content type")
    void returnsJson() throws Exception {
        mockMvc.perform(get("/api/v1/date"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("GET /api/v1/date serialises the date-time as yyyy-MM-dd'T'HH:mm:ss.SSS")
    void returnsDateTimeInIsoFormat() throws Exception {
        mockMvc.perform(get("/api/v1/date"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2026-09-06T12:34:56.789"))
                .andExpect(jsonPath("$.date").value(org.hamcrest.Matchers.matchesPattern(
                        "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}")));
    }

    @Test
    @DisplayName("Response body contains only the date key")
    void responseContainsOnlyDateKey() throws Exception {
        mockMvc.perform(get("/api/v1/date"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"date\":\"2026-09-06T12:34:56.789\"}", JsonCompareMode.STRICT));
    }

    @Test
    @DisplayName("The date-time is emitted as a JSON string, not an array or object")
    void serialisesAsString() throws Exception {
        mockMvc.perform(get("/api/v1/date"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").isString());
    }
}
