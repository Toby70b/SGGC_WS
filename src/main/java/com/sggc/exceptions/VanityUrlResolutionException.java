package com.sggc.exceptions;

import com.sggc.errors.ApiError;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class VanityUrlResolutionException extends WebAppException{
    private final String vanityUrl;

    @Override
    public ApiError toApiError() {
        return new ApiError(
                "VanityUrlResolutionException",
                "Vanity Url: " + vanityUrl + " could not be resolved to a steam id",
                null
        );
    }
}
