package com.sggc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Represents the configuration for Swagger
 */
//Todo configure endpoint
@Configuration
@EnableSwagger2
public class Swagger2Config {

    public static final String BASE_PACKAGE = "com.sggc.controllers";
    public static final String PATH_REGEX = "/api.*";
    public static final String SWAGGER_API_TITLE = "SGGC - REST API";
    public static final String SWAGGER_API_DESCRIPTION = "REST API for the Steam Group Game Checker";

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage(BASE_PACKAGE))
                .paths(PathSelectors.regex(PATH_REGEX))
                .build().apiInfo(apiEndPointsInfo());
    }

    /**
     * Collects non-technical information about the API such as title, description and author details
     *
     * @return an object containing non-technical information about the API
     */
    private ApiInfo apiEndPointsInfo() {
        return new ApiInfoBuilder().title(SWAGGER_API_TITLE)
                .description(SWAGGER_API_DESCRIPTION)
                .build();
    }
}

