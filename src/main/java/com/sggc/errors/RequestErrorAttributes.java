package com.sggc.errors;

import lombok.AllArgsConstructor;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

/**
 * Represents a class used to support the controller advice to return standardized errors back to the controller
 */
@AllArgsConstructor
public class RequestErrorAttributes extends DefaultErrorAttributes {

    private final String currentApiVersion;

    @Override
    public Map<String, Object> getErrorAttributes(WebRequest webRequest, boolean includeStackTrace) {
        final Map<String, Object> defaultErrorAttributes = super.getErrorAttributes(webRequest, false);
        final ApiError error = ApiError.fromDefaultAttributeMap(
                currentApiVersion, defaultErrorAttributes);
        return error.toAttributeMap();
    }
}