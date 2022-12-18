package com.sggc;

/**
 * Represents behaviour for removing resources within a service to ensure integration tests do not suffer from pollution.
 */
public interface TestResourceCleaner {

    /**
     * Removes any resources created from previous tests.
     */
    public void performCleanup();
}
