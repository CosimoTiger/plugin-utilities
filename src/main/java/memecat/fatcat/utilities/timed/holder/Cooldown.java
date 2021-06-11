package memecat.fatcat.utilities.timed.holder;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

/**
 * Implementation of {@link AbstractCooldown} that functions by using the default {@link System#currentTimeMillis()}.
 */
public class Cooldown extends AbstractCooldown {

    /**
     * Creates a new {@link Cooldown} from a given duration expressed in the given {@link TimeUnit}.
     *
     * @param duration For how long this cooldown will last
     * @param unit {@link TimeUnit} of the given cooldown duration parameter
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
}