package com.sggc.util;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class DateUtil {
    private final Clock clock;

    public DateUtil(Clock clock) {
        this.clock = clock;
    }

    public Instant getTimeOneDayFromNow(){
        return clock.instant().plus(1, ChronoUnit.DAYS);
    }
}
