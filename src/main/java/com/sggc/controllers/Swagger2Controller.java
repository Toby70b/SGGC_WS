package com.sggc.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Represents the controller for the Swagger endpoint the URL is the root of the service
 */
@Controller
public class Swagger2Controller {
    /**
     * GET mapping which redirects clients to the Swagger html page containing documentation of the REST API
     *
     * @return a string representing a redirect command to the swagger html page
     */
    @RequestMapping("")
    public String home() {
        return"redirect:/swagger-ui.html";
    }
}
