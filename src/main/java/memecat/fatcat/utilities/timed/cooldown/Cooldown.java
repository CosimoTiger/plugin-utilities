package memecat.fatcat.utilities.timed.cooldown;

import com.google.common.base.Preconditions;

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
     * @see Cooldown(long) - duration unit conversion to milliseconds
     */
    public Cooldown(long duration, TimeUnit unit) {
        Preconditions.checkArgument(unit != null, "TimeUnit argument can't be null");
        end = getCurrentTime() + unit.toMillis(duration);
    }

    /**
     * Creates a new {@link Cooldown} from a given duration expressed in milliseconds.
     *
     * @param duration For how long this cooldown will last in milliseconds
     */
    private Cooldown(long duration) {
        end = getCurrentTime() + duration;
    }
}