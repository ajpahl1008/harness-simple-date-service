package com.bespin.dateservice;

import com.bespin.dateservice.metrics.DateRequestCounter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies the Spring Boot Actuator endpoints exposed by the service: the health check, and the
 * date endpoint call counter as it appears on both {@code /actuator/info} and
 * {@code /actuator/metrics}.
 */
@SpringBootTest
@WebAppConfiguration
class ActuatorEndpointsTest {

    private MockMvc mockMvc;

    @Autowired
    private DateRequestCounter requestCounter;

    /**
     * Sets up MockMvc with the web application context.
     *
     * @param context the web application context to configure MockMvc with
     */
    @Autowired
    void setUp(WebApplicationContext context) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    /**
     * Verifies the health endpoint reports the service as UP.
     */
    @Test
    @DisplayName("GET /actuator/health reports UP")
    void healthIsUp() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    /**
     * Verifies the info endpoint carries the request counter details.
     */
    @Test
    @DisplayName("GET /actuator/info exposes the date request counter")
    void infoExposesRequestCounter() throws Exception {
        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dateRequests.total").isNumber())
                .andExpect(jsonPath("$.dateRequests.meter").value(DateRequestCounter.METER_NAME));
    }

    /**
     * Verifies that calls to the date endpoint move the total reported by the info endpoint.
     */
    @Test
    @DisplayName("Calls to /api/v1/date are reflected in the info endpoint total")
    void infoTotalTracksDateEndpointCalls() throws Exception {
        long before = requestCounter.count();

        mockMvc.perform(get("/api/v1/date")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/date")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/date")).andExpect(status().isOk());

        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dateRequests.total").value((int) (before + 3)));
    }

    /**
     * Verifies the Micrometer counter is published on the metrics endpoint under its meter name.
     */
    @Test
    @DisplayName("GET /actuator/metrics/date.service.requests exposes the Micrometer counter")
    void metricsExposeRequestCounter() throws Exception {
        mockMvc.perform(get("/api/v1/date")).andExpect(status().isOk());

        mockMvc.perform(get("/actuator/metrics/" + DateRequestCounter.METER_NAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(DateRequestCounter.METER_NAME))
                .andExpect(jsonPath("$.baseUnit").value("requests"))
                .andExpect(jsonPath("$.measurements[?(@.statistic == 'COUNT')].value").exists());
    }
}
