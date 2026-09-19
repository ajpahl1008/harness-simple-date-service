package com.bespin.dateservice.timezone;

import java.time.ZoneId;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Component;

/**
 * Holds the time zone the date endpoint renders responses in.
 *
 * <p>The value lives only in memory: it starts at UTC on every boot and is lost on restart. It is
 * not persisted, and (with multiple instances) is not shared across replicas. The management UI
 * ({@code /admin/timezone}) is the only way to change it at runtime.
 *
 * @see com.bespin.dateservice.controller.DateController
 */
@Component
public class TimeZoneSettings {

    private final AtomicReference<ZoneId> zone = new AtomicReference<>(ZoneId.of("UTC"));

    /**
     * Returns the time zone currently applied to the date endpoint.
     *
     * @return the active zone, {@code UTC} until overridden
     */
    public ZoneId getZone() {
        return zone.get();
    }

    /**
     * Overrides the time zone applied to the date endpoint.
     *
     * @param zone the zone to switch to; must not be {@code null}
     */
    public void setZone(ZoneId zone) {
        this.zone.set(Objects.requireNonNull(zone, "zone must not be null"));
    }
}
