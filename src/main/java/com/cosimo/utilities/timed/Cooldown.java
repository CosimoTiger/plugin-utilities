package com.cosimo.utilities.timed;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class Cooldown implements ITimed {

    /**
     * UNIX timestamp of when the {@link Cooldown} will end.
     */
    // Protected access for subclassing ability.
    protected long end;

    /**
     * Creates a new {@link Cooldown} from a given duration expressed in the given {@link TimeUnit}.
     *
     * @param duration For how long this cooldown will last
     * @param unit     {@link TimeUnit} of the given cooldown duration parameter
     * @see Cooldown - duration unit conversion to milliseconds
     */
    public Cooldown(long duration, @NotNull TimeUnit unit) {
        Preconditions.checkArgument(unit != null, "TimeUnit argument can't be null");
        this.end = this.getCurrentTime() + this.toEquivalentTime(duration, unit);
    }

    /**
     * Creates a new {@link Cooldown} from a given duration expressed in milliseconds.
     *
     * @param duration For how long this cooldown will last in milliseconds
     */
    private Cooldown(long duration) {
        this.end = this.getCurrentTime() + duration;
    }

    /**
     * Prolongs this cooldown by adding the given duration in the given {@link TimeUnit} to it.
     *
     * @param duration Duration to add, in the given {@link TimeUnit}
     * @param unit     {@link TimeUnit} that the given duration argument is in
     * @return Previous ending time of this {@link Cooldown}
     */
    public long extend(long duration, @NotNull TimeUnit unit) {
        Preconditions.checkArgument(unit != null, "TimeUnit argument can't be null");
        return this.extend(this.toEquivalentTime(duration, unit));
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
     * Returns how much time is left until the end of this cooldown, expressed in a given {@link TimeUnit}.
     *
     * @param unit {@link TimeUnit} in which the remaining milliseconds should be converted to
     * @return The remaining cooldown time, expressed as a double in the given {@link TimeUnit}
     */
    public long getRemaining(@NotNull TimeUnit unit) {
        Preconditions.checkArgument(unit != null, "TimeUnit argument can't be null");
        return this.fromEquivalentTime(this.getRemaining(), unit);
    }

    /**
     * Returns whether this cooldown's ending time is lower than the current {@link #getCurrentTime()} value.
     *
     * @return Whether this cooldown has expired
     */
    public boolean hasExpired() {
        return this.getExpiration() <= this.getCurrentTime();
    }

    /**
     * Returns the {@link #getCurrentTime()} time at which this cooldown should expire.
     *
     * @return {@link #getCurrentTime()} time at which this cooldown should expire
     */
    public long getExpiration() {
        return this.end;
    }

    /**
     * Returns how much time is left until the end of this cooldown, even negative if it has already expired.
     *
     * @return Positive, negative or 0 remaining time
     */
    public long getRemaining() {
        return this.getExpiration() - this.getCurrentTime();
    }
}