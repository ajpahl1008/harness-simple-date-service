package com.bespin.dateservice.controller;

import java.time.Clock;
import java.time.LocalDate;

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

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Date", description = "Current date")
public class DateController {

    private final Clock clock;

    public DateController(Clock clock) {
        this.clock = clock;
    }

    @Operation(summary = "Get the current date",
            description = "Returns the current date (UTC) as an ISO-8601 calendar date.")
    @ApiResponse(responseCode = "200", description = "The current date",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = DateResponse.class)))
    @GetMapping(value = "/date", produces = MediaType.APPLICATION_JSON_VALUE)
    public DateResponse currentDate() {
        return new DateResponse(LocalDate.now(clock));
    }
}
