package com.sggc.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Represents the controller for the Swagger endpoint.
 */
@Controller
public class SwaggerController {

    public static final String SWAGGER_API_URI = "";
    public static final String SWAGGER_UI_REDIRECT = "redirect:/swagger-ui.html";

    /**
     * GET mapping which redirects clients to the Swagger html page containing documentation of the REST API
     *
     * @return a string representing a redirect command to the swagger html page
     */
    @RequestMapping(SWAGGER_API_URI)
    public String home() {
        return SWAGGER_UI_REDIRECT;
    }
}
