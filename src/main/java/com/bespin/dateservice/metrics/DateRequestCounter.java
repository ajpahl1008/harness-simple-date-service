package com.bespin.dateservice.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

import org.springframework.stereotype.Component;

/**
 * Tracks how many times the date endpoint has been served since the application started.
 *
 * <p>The count is held in a Micrometer {@link Counter} registered as
 * {@value #METER_NAME}, so it is published through every configured metrics backend and is
 * readable at {@code /actuator/metrics/date.service.requests}. The same value is surfaced in a
 * friendlier shape at {@code /actuator/info} by
 * {@link com.bespin.dateservice.actuator.DateRequestInfoContributor}.
 *
 * <p>The counter is in-memory and per-instance: it resets on restart and is not shared across
 * replicas. Aggregate across instances in the metrics backend if a fleet-wide total is needed.
 */
@Component
public class DateRequestCounter {

    /** Name of the Micrometer meter backing this counter. */
    public static final String METER_NAME = "date.service.requests";

    private final Counter counter;

    /**
     * Registers the request counter with the supplied meter registry.
     *
     * @param meterRegistry the Micrometer registry to register the counter with
     */
    public DateRequestCounter(MeterRegistry meterRegistry) {
        this.counter = Counter.builder(METER_NAME)
                .description("Total number of requests served by the date endpoint")
                .baseUnit("requests")
                .register(meterRegistry);
    }

    /** Records one call to the date endpoint. */
    public void increment() {
        counter.increment();
    }

    /**
     * Returns the number of date endpoint calls recorded since this instance started.
     *
     * @return the total call count, never negative
     */
    public long count() {
        return (long) counter.count();
    }
}
