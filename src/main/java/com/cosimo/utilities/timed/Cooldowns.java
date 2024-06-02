package com.cosimo.utilities.timed;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * A class that keeps track of timed cooldowns stored inside a {@link Map}, updated lazily, on access[, which greatly
 * saves performance unlike some common ones that work on the basis of {@link Runnable}s]. It is enough to store one
 * instance per project.
 *
 * @param <K> Key type of this class's {@link Map}, {@link String} is suggested as it provides many variations for
 *            unique keys
 */
public class Cooldowns<K> implements ITimed {

    /**
     * Contains cooldown entries consisting of unique name keys and a {@link System#currentTimeMillis()} end time
     * value.
     */
    protected Map<K, Long> cooldowns;

    /**
     * Creates a new {@link Cooldowns} holder by creating a new {@link Map} by copying entries of the given
     * {@link Cooldowns}.
     *
     * @param toCopy The original {@link Cooldowns} whose contents will be transferred into this new one
     */
    public Cooldowns(@NotNull Cooldowns<K> toCopy) {
        this.cooldowns = new HashMap<>(toCopy.cooldowns);
    }

    /**
     * Creates a new {@link Cooldowns} holder that'll use the given {@link Map} implementation.
     *
     * @param mapImpl {@link Map} instance of any subclass
     */
    public Cooldowns(@NotNull Map<K, Long> mapImpl) {
        this.cooldowns = mapImpl;
    }

    /**
     * Creates a new default {@link Cooldowns} holder with a {@link HashMap} with initialCapacity of 8.
     */
    public Cooldowns() {
        this(new HashMap<>(8));
    }

    /**
     * Puts a cooldown entry converted into {@link #getCurrentTime()} unit from a given time duration expressed in the
     * given {@link TimeUnit} if the same cooldown expired or doesn't exist.
     *
     * @param newKey   Unique key that the cooldown will be stored under
     * @param duration Time in the given time unit for how long the cooldown will last for
     * @param unit     {@link TimeUnit} of the given time parameter
     * @return {@link #getCurrentTime()} ending time of the given cooldown or the current one that wasn't replaced
     */
    public long putIfAbsent(@NotNull K newKey, long duration, @NotNull TimeUnit unit) {
        return this.putIfAbsent(newKey, this.toEquivalentTime(duration, unit));
    }

    /**
     * Puts a cooldown entry converted into milliseconds from a given time duration expressed in the given
     * {@link TimeUnit}.
     *
     * @param newKey   Unique key that the cooldown will be stored under
     * @param duration Time in the given {@link TimeUnit} for how long the cooldown will last for
     * @param unit     {@link TimeUnit} of the given time parameter
     * @return Calculated {@link #getCurrentTime()} ending time of this cooldown
     */
    public long put(@NotNull K newKey, long duration, @NotNull TimeUnit unit) {
        return this.put(newKey, this.toEquivalentTime(duration, unit));
    }

    /**
     * Prolongs the specified cooldown by adding the given duration in the given {@link TimeUnit} to it.
     *
     * @param key      Unique key that a cooldown is stored under
     * @param duration Duration to add, in the given {@link TimeUnit}
     * @param unit     {@link TimeUnit} that the given duration argument is in
     * @return New ending time of the specified cooldown, e.g. it can be nonexistent
     */
    public long extend(@NotNull K key, long duration, @NotNull TimeUnit unit) {
        return this.extend(key, this.toEquivalentTime(duration, unit));
    }

    /**
     * Puts a cooldown entry in milliseconds of given time if the same cooldown expired or doesn't exist.
     *
     * @param newKey   Unique key that the cooldown will be stored under
     * @param duration Time in milliseconds for how long the cooldown will last for
     * @return {@link #getCurrentTime()} ending time of the given cooldown or the current one that wasn't replaced
     */
    public long putIfAbsent(@NotNull K newKey, long duration) {
        final long current = this.getCurrentTime();
        return this.cooldowns.compute(newKey, (currentKey, value) -> value == null || value <= current ? current + duration : value);
    }

    /**
     * Puts a cooldown entry in milliseconds from a given time duration expressed in milliseconds.
     *
     * @param newKey   Unique key that the cooldown is going to be stored under
     * @param duration Time in {@link TimeUnit#MILLISECONDS} for how long the cooldown will last for
     * @return Calculated {@link System#currentTimeMillis()} ending time of this cooldown
     */
    public long put(@NotNull K newKey, long duration) {
        final long newValue = this.getCurrentTime() + duration;
        this.cooldowns.put(newKey, newValue);
        return newValue;
    }

    /**
     * Prolongs the specified cooldown by adding the given duration.
     *
     * @param key      Unique key that a cooldown is stored under
     * @param duration Duration to add to the specified cooldown
     * @return New ending time of the specified cooldown, e.g. it can be nonexistent
     */
    public long extend(@NotNull K key, long duration) {
        final long current = this.getCurrentTime();
        return this.cooldowns.compute(key, (currentKey, value) -> value == null ? current + duration : value + duration);
    }

    /**
     * Removes cooldown entries that have expired.
     *
     * @return This instance, useful for chaining
     */
    @NotNull
    public Cooldowns<K> cleanup() {
        final long end = this.getCurrentTime();
        this.cooldowns.entrySet().removeIf(entry -> entry.getValue() <= end);
        return this;
    }

    /**
     * Removes all cooldown entries in this instance.
     *
     * @return This instance, useful for chaining
     */
    @NotNull
    public Cooldowns<K> clear() {
        this.cooldowns.clear();
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
        final Long value = this.cooldowns.remove(key);
        return value == null ? this.getCurrentTime() : value;
    }

    /**
     * Returns whether the specific key's cooldown has expired, i.e. if the {@link #getCurrentTime()} currently returns
     * a greater value than the cooldown ending.
     *
     * @param key Unique key that a cooldown is stored under
     * @return Whether the cooldown expired
     */
    public boolean hasExpired(@NotNull K key) {
        final Long time = this.cooldowns.get(key);
        return time == null || this.getCurrentTime() - time > 0;
    }

    /**
     * Returns a {@link Cooldown} object describing the given current state of a given cooldown.
     *
     * @param key Unique key that a cooldown is stored under
     * @return New {@link Cooldown} object for this query
     */
    public long get(@NotNull K key) {
        final Long time = this.cooldowns.get(key);
        return time == null ? this.getCurrentTime() : (time <= this.getCurrentTime() ? this.cooldowns.remove(key) : time);
    }

    /**
     * Returns the unmodifiable ({@link Collections#unmodifiableMap(Map)}) {@link Map}&lt;{@code K}, {@link Long}&gt;
     * view of this instance.
     *
     * @return Unmodifiable {@link Map} view of this instance
     */
    @NotNull
    public Map<K, Long> getMap() {
        return Collections.unmodifiableMap(this.cooldowns);
    }

    /**
     * Returns the amount of stored cooldown entries in this instance.
     *
     * @return Amount of stored cooldown entries
     */
    public int size() {
        return this.cooldowns.size();
    }
}