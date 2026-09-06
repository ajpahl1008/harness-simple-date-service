package com.bespin.dateservice.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response body of the date endpoint. Jackson serialises {@code date} as an ISO-8601 calendar
 * date (yyyy-MM-dd).
 */
@Schema(name = "DateResponse", description = "The current date in ISO-8601 format")
public record DateResponse(
        @Schema(description = "Current date, ISO-8601 (yyyy-MM-dd)", example = "2026-09-06", type = "string", format = "date")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate date) {
}
