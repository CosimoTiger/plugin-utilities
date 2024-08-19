package com.cosimo.utilities.timed;

import lombok.NonNull;

import java.util.concurrent.TimeUnit;

public interface ICooldown extends TimeStandard {

    /**
     * Returns whether this cooldown is expired.
     *
     * @return Whether this cooldown is expired
     */
    default boolean isExpired() {
        return this.getExpiration() <= this.getCurrentTime();
    }

    /**
     * Returns how much time is left until the end of this cooldown, expressed in a given {@link TimeUnit}.
     *
     * @param unit {@link TimeUnit} in which the remaining milliseconds should be converted to
     * @return The remaining cooldown time, expressed as a double in the given {@link TimeUnit}
     */
    default long getRemaining(@NonNull TimeUnit unit) {
        return this.fromThisTime(this.getRemaining(), unit);
    }

    /**
     * Returns how much time is left until the end of this cooldown, even negative if it has already expired.
     *
     * @return Remaining time of any sign
     */
    default long getRemaining() {
        return this.getExpiration() - this.getCurrentTime();
    }

    /**
     * Returns the {@link TimeStandard}-wise time at which this cooldown should expire.
     *
     * @return {@link #getCurrentTime()} time at which this cooldown should expire
     */
    long getExpiration();
}