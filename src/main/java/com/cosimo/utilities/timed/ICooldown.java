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
     * Returns how much time is left until the end of this cooldown, expressed in a given {@link TimeUnit}, minimally
     * zero.
     *
     * @param unit {@link TimeUnit} in which the remaining milliseconds should be converted to
     * @return The remaining cooldown time that can't be negative, expressed as a long in the given {@link TimeUnit}
     */
    default long getRemaining(@NonNull TimeUnit unit) {
        return Math.max(0, this.getDifference(unit));
    }

    /**
     * Returns the timestamp difference between current one and the end of this cooldown, minimally zero, which would
     * indicate it has already expired.
     *
     * @return Remaining time that can't be negative
     */
    default long getRemaining() {
        return Math.max(0, this.getDifference());
    }

    /**
     * Returns the timestamp difference between current one and the end of this cooldown, expressed in a given
     * {@link TimeUnit}.
     *
     * @param unit {@link TimeUnit} in which the remaining milliseconds should be converted to
     * @return The remaining cooldown time, expressed as a long in the given {@link TimeUnit}
     */
    default long getDifference(@NonNull TimeUnit unit) {
        return this.fromThisTime(this.getDifference(), unit);
    }

    /**
     * Returns how much time is left until the end of this cooldown, even negative if it has already expired.
     *
     * @return Remaining time of any mathematical sign
     */
    default long getDifference() {
        return this.getExpiration() - this.getCurrentTime();
    }

    /**
     * Returns the {@link TimeStandard}-wise time at which this cooldown should expire.
     *
     * @return {@link #getCurrentTime()} time at which this cooldown should expire
     */
    long getExpiration();
}