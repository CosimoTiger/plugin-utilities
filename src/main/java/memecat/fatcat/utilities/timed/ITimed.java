package memecat.fatcat.utilities.timed;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

/**
 * Interface for classes that use current time of a specific system (e.g. {@link System#currentTimeMillis()} returns the
 * System's counted time since start-up, while some games have their own counting since they started.)
 */
public interface ITimed {

    /**
     * Returns the current counted time of a specific system; {@link System}'s {@link System#currentTimeMillis()} by
     * default.
     *
     * @return Counted time since the beginning of a system, in the unit given by the context
     */
    default long getCurrentTime() {
        return System.currentTimeMillis();
    }

    /**
     * Converts the given time from the given {@link TimeUnit} into a duration in the unit equivalent to
     * {@link #getCurrentTime()}'s unit.
     *
     * @param unit {@link TimeUnit} of the given duration argument
     * @param duration Time duration to be converted
     * @return Time duration in a unit equivalent to {@link #getCurrentTime()}'s
     */
    default long toEquivalentTime(long duration, @NotNull TimeUnit unit) {
        return unit.toMillis(duration);
    }

    /**
     * Converts the given time in the unit equivalent to {@link #getCurrentTime()}'s unit to the duration in the given
     * {@link TimeUnit}.
     *
     * @param unit {@link TimeUnit} of the given duration argument
     * @param duration Time duration to be converted
     * @return Time duration in a unit equivalent to {@link #getCurrentTime()}'s
     */
    default long fromEquivalentTime(long duration, @NotNull TimeUnit unit) {
        return TimeUnit.MILLISECONDS.convert(duration, unit);
    }
}