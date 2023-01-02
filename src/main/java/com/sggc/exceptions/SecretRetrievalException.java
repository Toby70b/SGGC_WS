package com.sggc.exceptions;

/**
 * Represents an exception to be thrown when an error occurs trying to retrieve a secret from the AWS Secrets manager
 */
public class SecretRetrievalException extends Exception {
    
    public SecretRetrievalException(String secretId, Throwable cause) {
        super(String.format("Exception occurred when attempting to retrieve secret [%s] from AWS secrets manager."
                , secretId), cause);
    }
}

