package com.bespin.dateservice.controller;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;

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

    @TestConfiguration
    static class FixedClockConfig {
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
    @DisplayName("GET /api/v1/date returns the clock's date in ISO-8601 format")
    void returnsCurrentDateInIsoFormat() throws Exception {
        mockMvc.perform(get("/api/v1/date"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2026-09-06"))
                .andExpect(jsonPath("$.date").value(org.hamcrest.Matchers.matchesPattern("\\d{4}-\\d{2}-\\d{2}")));
    }

    @Test
    @DisplayName("Response body contains only the date key")
    void responseContainsOnlyDateKey() throws Exception {
        mockMvc.perform(get("/api/v1/date"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"date\":\"2026-09-06\"}", JsonCompareMode.STRICT));
    }

}
