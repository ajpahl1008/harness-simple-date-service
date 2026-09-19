package com.bespin.dateservice.ui;

import java.time.ZoneId;
import java.util.List;

import com.bespin.dateservice.timezone.TimeZoneSettings;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.ExtendedClientDetails;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Management view for the in-memory time zone the date endpoint renders responses in.
 *
 * <p>On load, the view reads the browser's time zone (via
 * {@link com.vaadin.flow.component.page.Page#getExtendedClientDetails()}) and offers it as a
 * one-click default; the user can otherwise pick any IANA zone from the list. Applying a zone
 * updates {@link TimeZoneSettings} immediately, which every subsequent call to
 * {@code /api/v1/date} picks up. Nothing here is persisted: a restart resets the service to UTC.
 */
@Route("admin/timezone")
@PageTitle("Time Zone Settings")
public class TimeZoneView extends VerticalLayout {

    static final List<String> AVAILABLE_ZONE_IDS = ZoneId.getAvailableZoneIds().stream().sorted().toList();

    private final TimeZoneSettings timeZoneSettings;

    final Span detectedZoneLabel;
    final Span activeZoneLabel;
    final ComboBox<String> zoneSelector;
    final Button useDetectedButton;
    final Button applyButton;

    private String detectedZoneId;

    /**
     * Builds the view, wiring the components to the shared time zone settings.
     *
     * @param timeZoneSettings the in-memory settings the date endpoint reads its zone from
     */
    public TimeZoneView(TimeZoneSettings timeZoneSettings) {
        this.timeZoneSettings = timeZoneSettings;

        setMaxWidth("32rem");

        add(new H2("Date Service Time Zone"));
        add(new Paragraph("The date service (/api/v1/date) renders its response in the time zone "
                + "selected below. This setting lives in memory only: it resets to UTC on every "
                + "restart and is not shared across instances."));

        detectedZoneLabel = new Span();
        detectedZoneLabel.setId("detected-zone-label");
        updateDetectedZoneLabel(null);

        zoneSelector = new ComboBox<>("Time zone");
        zoneSelector.setId("zone-selector");
        zoneSelector.setItems(AVAILABLE_ZONE_IDS);
        zoneSelector.setValue(timeZoneSettings.getZone().getId());
        zoneSelector.setWidthFull();

        useDetectedButton = new Button("Use browser time zone", event -> useDetectedZone());
        useDetectedButton.setId("use-detected-button");
        useDetectedButton.setEnabled(false);

        applyButton = new Button("Apply", event -> applySelectedZone());
        applyButton.setId("apply-button");

        activeZoneLabel = new Span();
        activeZoneLabel.setId("active-zone-label");
        refreshActiveZoneLabel();

        add(detectedZoneLabel, zoneSelector, useDetectedButton, applyButton, activeZoneLabel);
    }

    /**
     * Reads the browser's time zone as soon as the view is attached, so the detected zone is
     * ready by the time the user looks at the page.
     *
     * <p>Browser details are captured during UI bootstrap, so the cached value is normally
     * already available here; {@link ExtendedClientDetails#refresh} is used as a fallback to
     * cover the rare case where it isn't yet.
     *
     * @param attachEvent the attach event supplying the current UI
     */
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        ExtendedClientDetails details = attachEvent.getUI().getPage().getExtendedClientDetails();
        if (details.getTimeZoneId() != null) {
            applyDetectedZoneId(details.getTimeZoneId());
        } else {
            details.refresh(refreshed -> applyDetectedZoneId(refreshed.getTimeZoneId()));
        }
    }

    /**
     * Records the browser-supplied time zone and enables the "use browser time zone" shortcut
     * when it names a zone the JVM recognizes.
     *
     * <p>Package-private so tests can simulate the browser's response directly, since browserless
     * tests have no real browser to answer {@code retrieveExtendedClientDetails}.
     *
     * @param zoneId the IANA zone id reported by the browser, or {@code null} if unavailable
     */
    void applyDetectedZoneId(String zoneId) {
        this.detectedZoneId = zoneId;
        updateDetectedZoneLabel(zoneId);
        useDetectedButton.setEnabled(zoneId != null && AVAILABLE_ZONE_IDS.contains(zoneId));
    }

    private void updateDetectedZoneLabel(String zoneId) {
        detectedZoneLabel.setText(zoneId == null
                ? "Detecting your browser's time zone..."
                : "Your browser's time zone: " + zoneId);
    }

    private void useDetectedZone() {
        if (detectedZoneId != null) {
            zoneSelector.setValue(detectedZoneId);
        }
    }

    private void applySelectedZone() {
        String selected = zoneSelector.getValue();
        if (selected == null) {
            Notification.show("Choose a time zone first.");
            return;
        }
        timeZoneSettings.setZone(ZoneId.of(selected));
        refreshActiveZoneLabel();
        Notification.show("The date service now renders responses in " + selected + ".");
    }

    private void refreshActiveZoneLabel() {
        activeZoneLabel.setText("Currently active: " + timeZoneSettings.getZone().getId());
    }
}
