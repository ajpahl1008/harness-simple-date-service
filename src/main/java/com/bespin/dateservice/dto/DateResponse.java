package com.bespin.dateservice.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response body of the date endpoint. Jackson serialises {@code date} as an ISO-8601 date-time
 * with millisecond precision (yyyy-MM-dd'T'HH:mm:ss.SSS).
 *
 * <p>The explicit {@link JsonFormat} pattern matters: {@code LocalDateTime.toString()} drops
 * trailing zeros (and the whole fractional part when it is zero), which would emit
 * {@code 2026-09-06T12:34:56} on the stroke of a second. The pattern below always renders
 * exactly three fractional digits.
 */
@Schema(name = "DateResponse", description = "The current date and time in ISO-8601 format")
public record DateResponse(
        @Schema(description = "Current date and time, ISO-8601 (yyyy-MM-dd'T'HH:mm:ss.SSS)",
                example = "2026-09-06T12:34:56.789", type = "string", format = "date-time")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
        LocalDateTime date) {
}
