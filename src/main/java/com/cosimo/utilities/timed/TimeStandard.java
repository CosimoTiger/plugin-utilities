package com.cosimo.utilities.timed;

import lombok.NonNull;

import java.util.concurrent.TimeUnit;

/**
 * Interface for classes that use a clock or time measurement source or standard of a specific system, such as the
 * default {@link System#currentTimeMillis()}, time in nanoseconds or different time units, or a video game current tick
 * time since startup.
 */
public interface TimeStandard {

    /**
     * Converts the given time from the given {@link TimeUnit} into a duration in the unit equivalent to this source's
     * unit.
     *
     * @param unit     {@link TimeUnit} of the given duration argument
     * @param duration Time duration to be converted
     * @return Time duration in a unit equivalent to {@link #getCurrentTime()}'s
     */
    default long toThisTime(long duration, @NonNull TimeUnit unit) {
        return unit.toMillis(duration);
    }

    /**
     * Converts the given time in the unit equivalent to this source's unit to the duration in the given
     * {@link TimeUnit}.
     *
     * @param unit     {@link TimeUnit} of the given duration argument
     * @param duration Time duration to be converted
     * @return Time duration in a unit equivalent to {@link #getCurrentTime()}'s
     */
    default long fromThisTime(long duration, @NonNull TimeUnit unit) {
        return unit.convert(duration, TimeUnit.MILLISECONDS);
    }

    /**
     * Returns the current counted time of a specific system; {@link System}'s {@link System#currentTimeMillis()} by
     * default.
     *
     * @return Counted time since the beginning of a system, in the unit given by the context
     */
    default long getCurrentTime() {
        return System.currentTimeMillis();
    }
}