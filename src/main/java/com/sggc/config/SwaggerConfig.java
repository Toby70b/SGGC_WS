package com.sggc.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Represents the non-technical configuration for Swagger
 */
@Configuration
public class SwaggerConfig {

    public static final String SGGC_API_TITLE = "SGGC - REST API";
    public static final String SGGC_API_DESCRIPTION = "REST API for the Steam Group Game Checker";

    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI().info(new Info()
                .title(SGGC_API_TITLE)
                .description(SGGC_API_DESCRIPTION));
    }
}
