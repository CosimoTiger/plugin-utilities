package memecat.fatcat.utilities.timed;

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
}