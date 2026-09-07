package com.bespin.dateservice;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Verifies the Swagger/OpenAPI specification is served on its configured endpoint. */
@SpringBootTest
@WebAppConfiguration
class OpenApiDocumentationTest {

    private MockMvc mockMvc;

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
     * Verifies that the OpenAPI specification is served at /api/v1/openapi and documents the date endpoint.
     */
    @Test
    @DisplayName("OpenAPI spec is served at /api/v1/openapi and documents the date endpoint")
    void servesOpenApiSpec() throws Exception {
        mockMvc.perform(get("/api/v1/openapi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists())
                .andExpect(jsonPath("$.info.title").value("Simple Date Service API"))
                .andExpect(jsonPath("$.paths['/api/v1/date'].get").exists())
                .andExpect(jsonPath("$.components.schemas.DateResponse.properties.date.format").value("date-time"))
                .andExpect(jsonPath("$.components.schemas.DateResponse.properties.date.example")
                        .value("2026-09-06T12:34:56.789"));
    }

    /**
     * Verifies that the Swagger UI is accessible and redirects correctly.
     */
    @Test
    @DisplayName("Swagger UI is reachable at /api/v1/swagger-ui")
    void servesSwaggerUi() throws Exception {
        mockMvc.perform(get("/api/v1/swagger-ui"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/api/v1/swagger-ui/index.html"));
    }
}
