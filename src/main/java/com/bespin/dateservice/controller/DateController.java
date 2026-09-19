package com.bespin.dateservice.controller;

import java.time.Clock;
import java.time.LocalDateTime;

import com.bespin.dateservice.dto.DateResponse;
import com.bespin.dateservice.metrics.DateRequestCounter;
import com.bespin.dateservice.timezone.TimeZoneSettings;

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
 * Returns the current date and time in ISO-8601 format based on the injected clock, rendered in
 * whichever time zone is currently active on {@link TimeZoneSettings}.
 *
 * <p>Every successful call is recorded on {@link DateRequestCounter}, which surfaces the running
 * total through the actuator (see {@code /actuator/info} and
 * {@code /actuator/metrics/date.service.requests}).
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Date", description = "Current date and time")
public class DateController {

    private final Clock clock;
    private final DateRequestCounter requestCounter;
    private final TimeZoneSettings timeZoneSettings;

    /**
     * Constructs a DateController with the specified clock, request counter, and time zone
     * settings.
     *
     * @param clock the clock to use for determining the current instant
     * @param requestCounter the counter incremented on each served request
     * @param timeZoneSettings supplies the time zone the response is rendered in
     */
    public DateController(Clock clock, DateRequestCounter requestCounter,
            TimeZoneSettings timeZoneSettings) {
        this.clock = clock;
        this.requestCounter = requestCounter;
        this.timeZoneSettings = timeZoneSettings;
    }

    /**
     * Returns the current date and time in ISO-8601 format, rendered in the active time zone, and
     * records the call against the request counter.
     *
     * @return DateResponse containing the current date and time in the active time zone
     */
    @Operation(summary = "Get the current date and time",
            description = "Returns the current date and time as an ISO-8601 date-time with "
                    + "millisecond precision, in the time zone configured via /admin/timezone "
                    + "(UTC by default).")
    @ApiResponse(responseCode = "200", description = "The current date and time",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = DateResponse.class)))
    @GetMapping(value = "/date", produces = MediaType.APPLICATION_JSON_VALUE)
    public DateResponse currentDate() {
        requestCounter.increment();
        Clock zonedClock = clock.withZone(timeZoneSettings.getZone());
        return new DateResponse(LocalDateTime.now(zonedClock));
    }
}
