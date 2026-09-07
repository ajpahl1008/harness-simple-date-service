package com.bespin.dateservice.controller;

import java.time.Clock;
import java.time.LocalDate;

import com.bespin.dateservice.dto.DateResponse;
import com.bespin.dateservice.metrics.DateRequestCounter;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller that provides the current date endpoint.
 * Returns the current date in ISO-8601 format based on the injected clock.
 *
 * <p>Every successful call is recorded on {@link DateRequestCounter}, which surfaces the running
 * total through the actuator (see {@code /actuator/info} and
 * {@code /actuator/metrics/date.service.requests}).
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Date", description = "Current date")
public class DateController {

    private final Clock clock;
    private final DateRequestCounter requestCounter;

    /**
     * Constructs a DateController with the specified clock and request counter.
     *
     * @param clock the clock to use for determining the current date
     * @param requestCounter the counter incremented on each served request
     */
    public DateController(Clock clock, DateRequestCounter requestCounter) {
        this.clock = clock;
        this.requestCounter = requestCounter;
    }

    /**
     * Returns the current date in ISO-8601 format and records the call against the request counter.
     *
     * @return DateResponse containing the current date in UTC
     */
    @Operation(summary = "Get the current date",
            description = "Returns the current date (UTC) as an ISO-8601 calendar date.")
    @ApiResponse(responseCode = "200", description = "The current date",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = DateResponse.class)))
    @GetMapping(value = "/date", produces = MediaType.APPLICATION_JSON_VALUE)
    public DateResponse currentDate() {
        requestCounter.increment();
        return new DateResponse(LocalDate.now(clock));
    }
}
