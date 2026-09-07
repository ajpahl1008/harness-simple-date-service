package com.bespin.dateservice.controller;

import java.time.Clock;
import java.time.LocalDateTime;

import com.bespin.dateservice.dto.DateResponse;

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
 * Returns the current date and time in ISO-8601 format based on the injected clock.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Date", description = "Current date and time")
public class DateController {

    private final Clock clock;

    /**
     * Constructs a DateController with the specified clock.
     *
     * @param clock the clock to use for determining the current date
     */
    public DateController(Clock clock) {
        this.clock = clock;
    }

    /**
     * Returns the current date and time in ISO-8601 format.
     *
     * @return DateResponse containing the current date and time in UTC
     */
    @Operation(summary = "Get the current date and time",
            description = "Returns the current date and time (UTC) as an ISO-8601 date-time with millisecond precision.")
    @ApiResponse(responseCode = "200", description = "The current date and time",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = DateResponse.class)))
    @GetMapping(value = "/date", produces = MediaType.APPLICATION_JSON_VALUE)
    public DateResponse currentDate() {
        return new DateResponse(LocalDateTime.now(clock));
    }
}
