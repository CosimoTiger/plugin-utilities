package memecat.fatcat.utilities.cooldown;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * A class that keeps track of timed cooldowns stored inside a {@link HashMap} and updated on access. It is enough to
 * store one instance per plugin.
 */
public class Cooldowns {

    /**
     * Contains cooldown entries consisting of unique name keys and a system millisecond (System.currentTimeMillis())
     * end time value.
     */
    private Map<String, Long> cooldowns;

    /**
     * Creates a new {@link Cooldowns} with a given initial capacity and load factor arguments for it's {@link
     * HashMap}.
     *
     * @param initialCapacity Initial capacity of this instance's {@link HashMap}
     * @param loadFactor      Load facor of this instance's {@link HashMap}
     */
    public Cooldowns(int initialCapacity, float loadFactor) {
        cooldowns = new HashMap<>(initialCapacity, loadFactor);
    }

    /**
     * Creates a new {@link Cooldowns} with a given initial capacity argument for it's {@link HashMap}.
     *
     * @param initialCapacity Initial capacity of this instance's {@link HashMap}
     */
    public Cooldowns(int initialCapacity) {
        cooldowns = new HashMap<>(initialCapacity);
    }

    /**
     * Creates a new {@link Cooldowns} with a default {@link HashMap}.
     */
    public Cooldowns() {
        cooldowns = new HashMap<>();
    }

    /**
     * Puts a cooldown entry converted into milliseconds from a given {@link TimeUnit} if the same cooldown expired or
     * doesn't exist.
     *
     * @param name Unique name that the cooldown will be stored under
     * @param unit {@link TimeUnit} of the given time parameter
     * @param time Time in the given time unit for how long the cooldown will last for
     * @return System millisecond ending time of the given cooldown or the current one that wasn't replaced
     * @throws IllegalArgumentException If the cooldowns name or TimeUnit argument is null
     */
    public long putIfAbsent(@NotNull String name, @NotNull TimeUnit unit, long time) {
        Preconditions.checkArgument(unit != null, "TimeUnit argument can't be null");
        return putIfAbsent(name, unit.toMillis(time));
    }

    /**
     * Puts a cooldown entry converted into milliseconds from a given {@link TimeUnit}.
     *
     * @param name Unique name that the cooldown will be stored under
     * @param unit {@link TimeUnit} of the given time parameter
     * @param time Time in the given time unit for how long the cooldown will last for
     * @return Calculated system millisecond ending time of this cooldown
     * @throws IllegalArgumentException If the cooldowns name or TimeUnit argument is null
     */
    public long put(@NotNull String name, @NotNull TimeUnit unit, long time) {
        Preconditions.checkArgument(unit != null, "TimeUnit argument can't be null");
        return put(name, unit.toMillis(time));
    }

    /**
     * Puts a cooldown entry in milliseconds of given time if the same cooldown expired or doesn't exist.
     *
     * @param name Unique name that the cooldown will be stored under
     * @param time Time in milliseconds for how long the cooldown will last for
     * @return System millisecond ending time of the given cooldown or the current one that wasn't replaced
     * @throws IllegalArgumentException If the cooldowns name is null
     */
    public long putIfAbsent(@NotNull String name, long time) {
        Preconditions.checkArgument(name != null, "Cooldowns key name can't be null");

        final long current = System.currentTimeMillis();

        return cooldowns.compute(name, (key, value) -> value == null || value <= current ? current + time : value);
    }

    /**
     * Puts a cooldown entry in milliseconds from a given time length expressed in milliseconds.
     *
     * @param name Unique name that the cooldown will be stored under
     * @param time Time in milliseconds for how long the cooldown will last for
     * @return Calculated system millisecond ending time of this cooldown
     * @throws IllegalArgumentException If the cooldowns name argument is null
     */
    public long put(@NotNull String name, long time) {
        Preconditions.checkArgument(name != null, "Cooldowns key name can't be null");

        long newValue = System.currentTimeMillis() + time;
        cooldowns.put(name, newValue);

        return newValue;
    }

    /**
     * Removes a cooldown entry from the cooldown map.
     *
     * @param name Unique name that a cooldown is stored under
     * @return Cooldown that was found or else an expired empty
     * @throws IllegalArgumentException If the cooldowns name argument is null
     */
    @NotNull
    public Cooldown remove(@NotNull String name) {
        Preconditions.checkArgument(name != null, "Cooldowns key name can't be null");

        Long value = cooldowns.remove(name);
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
     * Returns a {@link Cooldown} object that contains two status variables (boolean expired and time left) describing
     * the given current state of a given cooldown.
     *
     * @param name Unique name or key that a cooldown is stored under
     * @return Cooldown object containing two status variables
     * @throws IllegalArgumentException If the cooldowns name argument is null
     */
    @NotNull
    public Cooldown get(@NotNull String name) {
        Preconditions.checkArgument(name != null, "Cooldowns key name can't be null");

        Long time = cooldowns.get(name);
        long end = System.currentTimeMillis();

        if (time == null) {
            time = end;
        } else if (time <= end) {
            cooldowns.remove(name);
        }

        return new Cooldown(time, end);
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
     * Represents data that describes a cooldown at a specific time it was checked at.
     */
    static class Cooldown {

        private final long currentTime;
        private final long endTime;

        /**
         * Creates a new instance with a given cooldown end time, time passed or needed till end and whether a cooldown
         * has actually expired.
         *
         * @param endTime     System millisecond time of when the cooldown should end
         * @param currentTime Current system millisecond time
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
         * Returns whether this cooldown's ending time is lower than current system millisecond time.
         *
         * @return Whether the current system millisecond time is higher or equal to the end time of this cooldown
         */
        public boolean hasExpired() {
            return getRemaining() <= 0;
        }

        /**
         * Returns the system millisecond time at which this cooldown should expire.
         *
         * @return System millisecond time at which this cooldown should expire
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
         * Returns the system millisecond time of when the snapshot of this cooldown was taken at.
         *
         * @return "Current" millisecond time of this cooldown snapshot
         */
        public long getSnapshot() {
            return currentTime;
        }
    }
}