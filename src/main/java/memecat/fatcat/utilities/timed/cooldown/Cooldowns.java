package memecat.fatcat.utilities.timed.cooldown;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * A class that keeps track of timed cooldowns stored inside a {@link HashMap}, updated lazily, on access[, which
 * greatly saves performance unlike some common ones that work on the basis of {@link Runnable}s]. It is enough
 * to store one instance per project.
 *
 * @param <K> Key type of this class's {@link HashMap}, {@link String} is suggested as it provides many variations for
 *            unique keys
 */
public class Cooldowns<K> extends AbstractCooldowns<K> {

    /**
     * Creates a new {@link Cooldowns} holder by creating a new {@link HashMap} from the entires of the given
     * {@link Cooldowns}.
     *
     * @param initial The original {@link Cooldowns} whose contents will be transferred into this new one
     */
    public Cooldowns(@NotNull Cooldowns<K> initial) {
        Preconditions.checkArgument(initial != null, "Initial Cooldowns object can't be null");
        cooldowns = new HashMap<>(initial.cooldowns);
    }

    /**
     * Creates a new {@link Cooldowns} holder by creating a new {@link HashMap} with initialCapacity of 8.
     */
    public Cooldowns() {
        cooldowns = new HashMap<>(8);
    }

    /**
     * Puts a cooldown entry converted into milliseconds from a given time duration expressed in the given {@link
     * TimeUnit} if the same cooldown expired or doesn't exist.
     *
     * @param newKey   Unique key that the cooldown will be stored under
     * @param unit     {@link TimeUnit} of the given time parameter
     * @param duration Time in the given time unit for how long the cooldown will last for
     * @return {@link #getCurrentTime()} ending time of the given cooldown or the current one that wasn't
     * replaced
     * @throws IllegalArgumentException If the cooldowns name or TimeUnit argument is null
     */
    public long putIfAbsent(@NotNull K newKey, @NotNull TimeUnit unit, long duration) {
        Preconditions.checkArgument(unit != null, "TimeUnit argument can't be null");
        return putIfAbsent(newKey, unit.toMillis(duration));
    }

    /**
     * Puts a cooldown entry converted into milliseconds from a given time duration expressed in the given {@link
     * TimeUnit}.
     *
     * @param newKey   Unique key that the cooldown will be stored under
     * @param unit     {@link TimeUnit} of the given time parameter
     * @param duration Time in the given {@link TimeUnit} for how long the cooldown will last for
     * @return Calculated {@link #getCurrentTime()} ending time of this cooldown
     * @throws IllegalArgumentException If the cooldowns key or {@link TimeUnit} argument is null
     */
    public long put(@NotNull K newKey, @NotNull TimeUnit unit, long duration) {
        Preconditions.checkArgument(unit != null, "TimeUnit argument can't be null");
        return put(newKey, unit.toMillis(duration));
    }

    /**
     * Puts a cooldown entry in milliseconds of given time if the same cooldown expired or doesn't exist.
     *
     * @param newKey   Unique key that the cooldown will be stored under
     * @param duration Time in milliseconds for how long the cooldown will last for
     * @return {@link #getCurrentTime()} ending time of the given cooldown or the current one that wasn't
     * replaced
     * @throws IllegalArgumentException If the cooldowns key is null
     */
    public long putIfAbsent(@NotNull K newKey, long duration) {
        Preconditions.checkArgument(newKey != null, "Cooldowns key can't be null");

        final long current = getCurrentTime();

        return cooldowns.compute(newKey, (currentKey, value) -> value == null || value <= current ? current + duration : value);
    }

    /**
     * Removes cooldown entries that have expired.
     *
     * @return This instance, useful for chaining
     */
    @NotNull
    public Cooldowns<K> cleanup() {
        final long end = System.currentTimeMillis();
        cooldowns.entrySet().removeIf(entry -> entry.getValue() <= end);

        return this;
    }
}