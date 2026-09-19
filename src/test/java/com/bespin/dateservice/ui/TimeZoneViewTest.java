package com.bespin.dateservice.ui;

import java.time.ZoneId;

import com.bespin.dateservice.timezone.TimeZoneSettings;

import com.vaadin.browserless.SpringBrowserlessTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Browserless tests for {@link TimeZoneView}.
 *
 * <p>Browserless testing has no real browser to answer {@code Page.getExtendedClientDetails()},
 * so it always returns a placeholder with a {@code null} time zone id here. Detection is
 * exercised by calling the view's package-private {@link TimeZoneView#applyDetectedZoneId} seam
 * directly, simulating the browser's response.
 */
@SpringBootTest
class TimeZoneViewTest extends SpringBrowserlessTest {

    @Autowired
    private TimeZoneSettings timeZoneSettings;

    /**
     * Resets the shared, singleton {@link TimeZoneSettings} bean before each test, since the
     * Spring context (and therefore the bean) is reused across test methods.
     */
    @BeforeEach
    void resetTimeZoneSettings() {
        timeZoneSettings.setZone(ZoneId.of("UTC"));
    }

    @Test
    @DisplayName("Shows UTC as the active zone before anything is changed")
    void showsDefaultActiveZone() {
        TimeZoneView view = navigate(TimeZoneView.class);

        assertThat(test(view.activeZoneLabel).getText()).isEqualTo("Currently active: UTC");
        assertThat(test(view.zoneSelector).getSelected()).isEqualTo("UTC");
    }

    @Test
    @DisplayName("Before detection completes, the 'use browser time zone' shortcut is disabled")
    void detectionPendingByDefault() {
        TimeZoneView view = navigate(TimeZoneView.class);

        assertThat(view.useDetectedButton.isEnabled()).isFalse();
        assertThat(test(view.detectedZoneLabel).getText())
                .isEqualTo("Detecting your browser's time zone...");
    }

    @Test
    @DisplayName("Simulating a detected browser zone enables the shortcut and updates the label")
    void appliesDetectedZoneId() {
        TimeZoneView view = navigate(TimeZoneView.class);

        view.applyDetectedZoneId("Europe/Helsinki");

        assertThat(test(view.detectedZoneLabel).getText())
                .isEqualTo("Your browser's time zone: Europe/Helsinki");
        assertThat(view.useDetectedButton.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("An unrecognized detected zone id does not enable the shortcut")
    void ignoresUnrecognizedDetectedZoneId() {
        TimeZoneView view = navigate(TimeZoneView.class);

        view.applyDetectedZoneId("Not/AZone");

        assertThat(view.useDetectedButton.isEnabled()).isFalse();
    }

    @Test
    @DisplayName("Clicking 'use browser time zone' copies the detected zone into the selector")
    void useDetectedButtonCopiesZoneIntoSelector() {
        TimeZoneView view = navigate(TimeZoneView.class);
        view.applyDetectedZoneId("Asia/Tokyo");

        test(view.useDetectedButton).click();

        assertThat(test(view.zoneSelector).getSelected()).isEqualTo("Asia/Tokyo");
    }

    @Test
    @DisplayName("Applying a selected zone updates the shared settings and the active-zone label")
    void applyingSelectedZoneUpdatesSettings() {
        TimeZoneView view = navigate(TimeZoneView.class);

        test(view.zoneSelector).selectItem("Asia/Tokyo");
        test(view.applyButton).click();

        assertThat(timeZoneSettings.getZone()).isEqualTo(ZoneId.of("Asia/Tokyo"));
        assertThat(test(view.activeZoneLabel).getText()).isEqualTo("Currently active: Asia/Tokyo");
    }

    @Test
    @DisplayName("Applying with nothing selected leaves the active zone untouched")
    void applyingWithNoSelectionLeavesSettingsUntouched() {
        TimeZoneView view = navigate(TimeZoneView.class);
        view.zoneSelector.clear();

        test(view.applyButton).click();

        assertThat(timeZoneSettings.getZone()).isEqualTo(ZoneId.of("UTC"));
        assertThat(test(view.activeZoneLabel).getText()).isEqualTo("Currently active: UTC");
    }
}
