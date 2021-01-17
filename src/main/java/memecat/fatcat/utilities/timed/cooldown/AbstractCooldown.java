package memecat.fatcat.utilities.timed.cooldown;

import com.google.common.base.Preconditions;
import memecat.fatcat.utilities.timed.ITimed;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public abstract class AbstractCooldown implements ITimed {

    protected long end;

    /**
     * Returns how much time is left until the end of this cooldown, expressed in a given {@link TimeUnit}.
     *
     * @param unit {@link TimeUnit} in which the remaining milliseconds should be converted to
     * @return The remaining cooldown time, expressed as a double in the given {@link TimeUnit}
     */
    public long getRemaining(@NotNull TimeUnit unit) {
        Preconditions.checkArgument(unit != null, "TimeUnit argument can't be null");
        return TimeUnit.MILLISECONDS.convert(getRemaining(), unit);
    }

    /**
     * Returns the {@link #getCurrentTime()} time at which this cooldown should expire.
     *
     * @return {@link #getCurrentTime()} time at which this cooldown should expire
     */
    public long getExpiration() {
        return end;
    }

    /**
     * Returns whether this cooldown's ending time is lower than the current {@link #getCurrentTime()} value.
     *
     * @return Whether this cooldown has expired
     */
    public boolean hasExpired() {
        return end <= getCurrentTime();
    }

    /**
     * Returns how much time is left until the end of this cooldown, even negative if it has already expired.
     *
     * @return Positive, negative or 0 remaining time
     */
    public long getRemaining() {
        return end - getCurrentTime();
    }
}