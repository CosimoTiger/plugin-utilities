package com.cosimo.utilities.timed;

import lombok.NonNull;

import java.util.concurrent.TimeUnit;

/**
 * Mutable implementation where the cooldown can be extended by a duration.
 */
public class Cooldown implements ICooldown {

    /**
     * Timestamp of when the {@link Cooldown} will end.
     */
    // Protected access for subclassing ability.
    protected long end;

    /**
     * Creates a new {@link Cooldown} from a given duration expressed in the given {@link TimeUnit}.
     *
     * @param duration For how long this cooldown will last
     * @param unit     {@link TimeUnit} of the given cooldown duration parameter
     */
    public Cooldown(long duration, @NonNull TimeUnit unit) {
        this.end = this.getCurrentTime() + this.toThisTime(duration, unit);
    }

    /**
     * Creates a new {@link Cooldown} from a given duration expressed in the implementation's time unit.
     *
     * @param duration For how long this cooldown will last in the implementation's time unit
     */
    public Cooldown(long duration) {
        this.end = this.getCurrentTime() + duration;
    }

    /**
     * Prolongs this cooldown by adding the given duration in the given {@link TimeUnit} to it.
     *
     * @param duration Duration to add, in the given {@link TimeUnit}
     * @param unit     {@link TimeUnit} that the given duration argument is in
     * @return Previous ending time of this {@link Cooldown}
     */
    public long extend(long duration, @NonNull TimeUnit unit) {
        return this.extend(this.toThisTime(duration, unit));
    }

    /**
     * Prolongs this cooldown by adding the given duration.
     *
     * @param duration Duration to add
     * @return Previous ending time of this {@link Cooldown}
     */
    public long extend(long duration) {
        final long previous = this.getExpiration();
        this.end += duration;

        return previous;
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