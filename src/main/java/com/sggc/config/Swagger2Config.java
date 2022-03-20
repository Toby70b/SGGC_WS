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
 * Represents the configuration for swaggers
 */
@Configuration
@EnableSwagger2
public class Swagger2Config {
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors
                        .basePackage("com.sggc.controllers"))
                .paths(PathSelectors.regex("/api.*"))
                .build().apiInfo(apiEndPointsInfo());
    }

    /**
     * Collects non-technical information about the API such as title, description and author details
     *
     * @return an object containing non-technical information about the API
     */
    private ApiInfo apiEndPointsInfo() {
        return new ApiInfoBuilder().title("SGGC - REST API")
                .description("REST API for the Steam Group Game Checker")
                .contact(new Contact("Toby Peel", "", ""))
                .build();
    }
}

