package memecat.fatcat.utilities.cooldown;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * A class that keeps track of timed cooldowns stored inside a {@link HashMap}, updated lazily, on access. It is
 * enough to store one instance per project.
 *
 * @param <K> Key type of this class's {@link HashMap}, {@link String} is suggested as it provides many variations for
 *            unique keys
 */
public class Cooldowns<K> {

    /**
     * Contains cooldown entries consisting of unique name keys and a {@link System#currentTimeMillis()} end time
     * value.
     */
    private Map<K, Long> cooldowns = new HashMap<>(8);

    /**
     * Puts a cooldown entry converted into milliseconds from a given time duration expressed in the given {@link
     * TimeUnit} if the same cooldown expired or doesn't exist.
     *
     * @param newKey   Unique key that the cooldown will be stored under
     * @param unit     {@link TimeUnit} of the given time parameter
     * @param duration Time in the given time unit for how long the cooldown will last for
     * @return {@link System#currentTimeMillis()} ending time of the given cooldown or the current one that wasn't
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
     * @return Calculated {@link System#currentTimeMillis()} ending time of this cooldown
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
     * @return {@link System#currentTimeMillis()} ending time of the given cooldown or the current one that wasn't
     * replaced
     * @throws IllegalArgumentException If the cooldowns key is null
     */
    public long putIfAbsent(@NotNull K newKey, long duration) {
        Preconditions.checkArgument(newKey != null, "Cooldowns key can't be null");

        final long current = System.currentTimeMillis();

        return cooldowns.compute(newKey, (currentKey, value) -> value == null || value <= current ? current + duration : value);
    }

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

        long newValue = System.currentTimeMillis() + duration;
        cooldowns.put(newKey, newValue);

        return newValue;
    }

    /**
     * Removes a {@link K} entry from the cooldown map and returns the state of it.
     *
     * @param key Unique key that a cooldown is stored under
     * @return New {@link Cooldown} object for this query
     * @throws IllegalArgumentException If the cooldowns key argument is null
     */
    @NotNull
    public Cooldown remove(@NotNull K key) {
        Preconditions.checkArgument(key != null, "Cooldowns key can't be null");

        Long value = cooldowns.remove(key);
        long current = System.currentTimeMillis();

        return new Cooldown(value == null ? current : value, current);
    }

    /**
     * Removes cooldown entries that have expired.
     *
     * @return This instance, useful for chaining
     */
    @NotNull
    public Cooldowns cleanup() {
        final long end = System.currentTimeMillis();
        cooldowns.entrySet().removeIf(entry -> entry.getValue() <= end);

        return this;
    }

    /**
     * Removes all cooldown entries in this instance.
     *
     * @return This instance, useful for chaining
     */
    @NotNull
    public Cooldowns clear() {
        cooldowns.clear();
        return this;
    }


    /**
     * Returns a {@link Cooldown} object describing the given current state of a given cooldown.
     *
     * @param key Unique key that a cooldown is stored under
     * @return New {@link Cooldown} object for this query
     * @throws IllegalArgumentException If the cooldowns key argument is null
     */
    @NotNull
    public Cooldown get(@NotNull K key) {
        Preconditions.checkArgument(key != null, "Cooldowns key can't be null");

        Long time = cooldowns.get(key);
        long end = System.currentTimeMillis();

        if (time == null) {
            time = end;
        } else if (time <= end) {
            cooldowns.remove(key);
        }

        return new Cooldown(time, end);
    }

    /**
     * Returns the unmodifiable {@link Map}&lt;{@link K}, {@link Long}&gt; view of this class.
     *
     * @return Unmodifiable {@link Map}&lt;{@link K}, {@link Long}&gt; view of this class.
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

    /**
     * Represents data that describes a cooldown at a specific time it was checked at; it's remaining duration that's
     * left, the {@link System#currentTimeMillis()} time it was checked at and the {@link System#currentTimeMillis()}
     * time it should expire at.
     */
    static class Cooldown {

        private final long currentTime;
        private final long endTime;

        /**
         * Creates a new instance with a given cooldown end time, time passed or needed till end and whether a cooldown
         * has actually expired.
         *
         * @param endTime     {@link System#currentTimeMillis()} time of when the cooldown should end
         * @param currentTime Current {@link System#currentTimeMillis()} time
         */
        private Cooldown(long endTime, long currentTime) {
            this.currentTime = currentTime;
            this.endTime = endTime;
        }

        /**
         * Returns how much time is left until the end of this cooldown, expressed in a given {@link TimeUnit}.
         *
         * @param unit {@link TimeUnit} in which the remaining milliseconds should be converted to
         * @return The remaining cooldown time, expressed as a double in the given {@link TimeUnit}
         */
        public double getRemaining(@NotNull TimeUnit unit) {
            Preconditions.checkArgument(unit != null, "TimeUnit argument can't be null");
            return TimeUnit.MILLISECONDS.convert(getRemaining(), unit);
        }

        /**
         * Returns whether this cooldown's ending time is lower than current {@link System#currentTimeMillis()} time.
         *
         * @return Whether the current {@link System#currentTimeMillis()} time is higher or equal to the end time of
         * this cooldown
         */
        public boolean hasExpired() {
            return getRemaining() <= 0;
        }

        /**
         * Returns the {@link System#currentTimeMillis()} time at which this cooldown should expire.
         *
         * @return {@link System#currentTimeMillis()} time at which this cooldown should expire
         */
        public long getExpiration() {
            return endTime;
        }

        /**
         * Returns how much time is left until the end of this cooldown, possibly negative if it has already expired.
         *
         * @return Positive or negative time in milliseconds
         */
        public long getRemaining() {
            return endTime - currentTime;
        }

        /**
         * Returns the {@link System#currentTimeMillis()} time of when the snapshot of this cooldown was taken at.
         *
         * @return "Current" millisecond time of this cooldown snapshot
         */
        public long getSnapshot() {
            return currentTime;
        }
    }
}