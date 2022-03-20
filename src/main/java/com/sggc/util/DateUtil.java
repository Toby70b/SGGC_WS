package com.sggc.util;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Utility class for generic date functionality
 */
//TODO: this isnt really a util class, refactor in a cleanup ticket
public class DateUtil {
    private final Clock clock;

    public DateUtil(Clock clock) {
        this.clock = clock;
    }

    public Instant getTimeOneDayFromNow(){
        return clock.instant().plus(1, ChronoUnit.DAYS);
    }
}
