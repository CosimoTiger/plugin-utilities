package memecat.fatcat.utilities.timed.cooldown;

import com.google.common.base.Preconditions;
import memecat.fatcat.utilities.timed.ITimed;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * A class that keeps track of timed cooldowns stored inside a {@link HashMap}, updated lazily, on access[, which
 * greatly saves performance unlike some common ones that work on the basis of {@link Runnable}s]. It is enough
 * to store one instance per project.
 *
 * @param <K> Key type of this class's {@link HashMap}, {@link String} is suggested as it provides many variations for
 *            unique keys
 */
public abstract class AbstractCooldowns<K> implements ITimed {

    /**
     * Contains cooldown entries consisting of unique name keys and a {@link System#currentTimeMillis()} end time
     * value.
     */
    protected Map<K, Long> cooldowns;

    /**
     * Puts a cooldown entry in milliseconds from a given time duration expressed in milliseconds.
     *
     * @param newKey   Unique key that the cooldown is going to be stored under
     * @param duration Time in {@link TimeUnit#MILLISECONDS} for how long the cooldown will last for
     * @return Calculated {@link System#currentTimeMillis()} ending time of this cooldown
     * @throws IllegalArgumentException If the cooldowns key argument is null
     */
    public long put(@NotNull K newKey, long duration) {
        Preconditions.checkArgument(newKey != null, "Cooldowns key can't be null");

        long newValue = getCurrentTime() + duration;
        cooldowns.put(newKey, newValue);

        return newValue;
    }

    /**
     * Removes all cooldown entries in this instance.
     *
     * @return This instance, useful for chaining
     */
    @NotNull
    public AbstractCooldowns<K> clear() {
        cooldowns.clear();
        return this;
    }

    /**
     * Removes an entry from the cooldown map and returns the state of it.
     *
     * @param key Unique key that a cooldown is stored under
     * @return Ending time of cooldown or {@link #getCurrentTime()} if nonexistent
     * @throws IllegalArgumentException If the cooldowns key argument is null
     */
    public long remove(@NotNull K key) {
        Preconditions.checkArgument(key != null, "Cooldowns key can't be null");

        Long value = cooldowns.remove(key);

        return value == null ? getCurrentTime() : value;
    }

    public boolean hasExpired(@NotNull K key) {
        Preconditions.checkArgument(key != null, "Cooldowns key can't be null");
        Long time = cooldowns.get(key);

        return time == null || getCurrentTime() - time > 0;
    }

    /**
     * Returns a {@link Cooldown} object describing the given current state of a given cooldown.
     *
     * @param key Unique key that a cooldown is stored under
     * @return New {@link Cooldown} object for this query
     * @throws IllegalArgumentException If the cooldowns key argument is null
     */
    public long get(@NotNull K key) {
        Preconditions.checkArgument(key != null, "Cooldowns key can't be null");
        Long time = cooldowns.get(key);

        return time == null ? getCurrentTime() : (time <= getCurrentTime() ? cooldowns.remove(key) : time);
    }

    /**
     * Returns the unmodifiable ({@link Collections#unmodifiableMap(Map)}) {@link Map}&lt;K, {@link Long}&gt;
     * view of this instance.
     *
     * @return Unmodifiable {@link Map}&lt;K, {@link Long}&gt; view of this instance
     */
    @NotNull
    public Map<K, Long> getMap() {
        return Collections.unmodifiableMap(cooldowns);
    }

    /**
     * Returns the amount of stored cooldown entries in this instance.
     *
     * @return Amount of stored cooldown entries
     */
    public int size() {
        return cooldowns.size();
    }
}