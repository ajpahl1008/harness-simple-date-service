package com.bespin.dateservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for OpenAPI/Swagger documentation.
 * Defines metadata and settings for the API specification.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Configures the OpenAPI specification for the Simple Date Service API.
     *
     * @return OpenAPI instance with API metadata including title, description, version, and license
     */
    @Bean
    public OpenAPI simpleDateServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Simple Date Service API")
                .description("Returns the current date in ISO-8601 format.")
                .version("v1")
                .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
