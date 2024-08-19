package com.cosimo.utilities.timed;

import lombok.NonNull;

import java.util.concurrent.TimeUnit;

public class ImmutableCooldown implements ICooldown {

    /**
     * Timestamp of when the {@link ImmutableCooldown} will end.
     */
    private final long end;

    /**
     * Creates a new {@link ImmutableCooldown} from a given duration expressed in the given {@link TimeUnit}.
     *
     * @param duration For how long this cooldown will last
     * @param unit     {@link TimeUnit} of the given cooldown duration parameter
     * @see ImmutableCooldown Duration unit conversion to milliseconds
     */
    public ImmutableCooldown(long duration, @NonNull TimeUnit unit) {
        this.end = this.getCurrentTime() + this.toThisTime(duration, unit);
    }

    /**
     * Creates a new {@link ImmutableCooldown} from a given duration expressed in milliseconds.
     *
     * @param duration For how long this cooldown will last in milliseconds
     */
    public ImmutableCooldown(long duration) {
        this.end = this.getCurrentTime() + duration;
    }

    /**
     * Returns the {@link #getCurrentTime()} time at which this cooldown should expire.
     *
     * @return {@link #getCurrentTime()} time at which this cooldown should expire
     */
    public long getExpiration() {
        return this.end;
    }
}