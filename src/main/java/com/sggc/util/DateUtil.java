package com.sggc.util;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Utility class for generic date functionality
 */
public class DateUtil {
    private final Clock clock;

    public DateUtil(Clock clock) {
        this.clock = clock;
    }

    /**
     * Returns the time exactly one day in the future from the current time represented within the clock property
     * @return an Instant object representing exactly one day in the future from the current time represented in the clock
     */
    public Instant getTimeOneDayFromNow(){
        return clock.instant().plus(1, ChronoUnit.DAYS);
    }
}
