package memecat.fatcat.utilities.timed.holder;

import com.google.common.base.Preconditions;
import memecat.fatcat.utilities.timed.ITimed;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

public abstract class AbstractCooldown implements ITimed {

    protected long end;

    /**
     * Prolongs this cooldown by adding the given duration in the given {@link TimeUnit} to it.
     *
     * @param duration Duration to add, in the given {@link TimeUnit}
     * @param unit {@link TimeUnit} that the given duration argument is in
     * @return Previous ending time of this {@link AbstractCooldown}
     */
    public long extend(long duration, @Nonnull TimeUnit unit) {
        Preconditions.checkArgument(unit != null, "TimeUnit argument can't be null");
        return this.extend(this.toEquivalentTime(duration, unit));
    }

    /**
     * Prolongs this cooldown by adding the given duration.
     *
     * @param duration Duration to add
     * @return Previous ending time of this {@link AbstractCooldown}
     */
    public long extend(long duration) {
        long previous = this.end;
        this.end += duration;

        return previous;
    }

    /**
     * Returns how much time is left until the end of this cooldown, expressed in a given {@link TimeUnit}.
     *
     * @param unit {@link TimeUnit} in which the remaining milliseconds should be converted to
     * @return The remaining cooldown time, expressed as a double in the given {@link TimeUnit}
     */
    public long getRemaining(@Nonnull TimeUnit unit) {
        Preconditions.checkArgument(unit != null, "TimeUnit argument can't be null");
        return this.fromEquivalentTime(this.getRemaining(), unit);
    }

    /**
     * Returns whether this cooldown's ending time is lower than the current {@link #getCurrentTime()} value.
     *
     * @return Whether this cooldown has expired
     */
    public boolean hasExpired() {
        return this.end <= this.getCurrentTime();
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
        return this.end - this.getCurrentTime();
    }
}