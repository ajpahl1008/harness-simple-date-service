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

    @Autowired
    void setUp(WebApplicationContext context) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @DisplayName("OpenAPI spec is served at /api/v1/openapi and documents the date endpoint")
    void servesOpenApiSpec() throws Exception {
        mockMvc.perform(get("/api/v1/openapi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists())
                .andExpect(jsonPath("$.info.title").value("Simple Date Service API"))
                .andExpect(jsonPath("$.paths['/api/v1/date'].get").exists())
                .andExpect(jsonPath("$.components.schemas.DateResponse.properties.date.format").value("date"));
    }

    @Test
    @DisplayName("Swagger UI is reachable at /api/v1/swagger-ui")
    void servesSwaggerUi() throws Exception {
        mockMvc.perform(get("/api/v1/swagger-ui"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/api/v1/swagger-ui/index.html"));
    }
}
