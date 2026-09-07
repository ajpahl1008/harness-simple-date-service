package com.bespin.dateservice.actuator;

import java.util.Map;

import com.bespin.dateservice.metrics.DateRequestCounter;

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

/**
 * Publishes the date endpoint call count under the {@code dateRequests} key of
 * {@code /actuator/info}, so the counter is visible from the actuator without having to query the
 * metrics endpoint:
 *
 * <pre>
 * {"dateRequests":{"total":42,"meter":"date.service.requests"}}
 * </pre>
 *
 * @see DateRequestCounter
 */
@Component
public class DateRequestInfoContributor implements InfoContributor {

    private final DateRequestCounter requestCounter;

    /**
     * Creates a contributor that reports the supplied counter.
     *
     * @param requestCounter the counter whose total is exposed in the info endpoint
     */
    public DateRequestInfoContributor(DateRequestCounter requestCounter) {
        this.requestCounter = requestCounter;
    }

    /**
     * Adds the current call total to the actuator info payload.
     *
     * @param builder the builder the info endpoint assembles its response from
     */
    @Override
    public void contribute(Info.Builder builder) {
        builder.withDetail("dateRequests", Map.of(
                "total", requestCounter.count(),
                "meter", DateRequestCounter.METER_NAME));
    }
}
