package com.cosimo.utilities.timed;

import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * A {@link Map} of {@link ICooldown} updated lazily, on access, with the {@link #cleanup()} method for purging expired
 * entries when needed.
 *
 * @param <K> Key type of this class's {@link Map}, {@link String} is suggested as it provides many variations for
 *            unique keys
 * @param <V> Expected {@link ICooldown} implementation
 */
public class Cooldowns<K, V extends ICooldown> extends HashMap<K, V> {
    public Cooldowns(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public Cooldowns(int initialCapacity) {
        super(initialCapacity);
    }

    public Cooldowns() {
        super();
    }

    public Cooldowns(@NonNull Map<? extends K, ? extends V> map) {
        super(map);
    }

    /**
     * Removes all {@link ICooldown} that have expired.
     *
     * @return This instance, useful for chaining
     */
    @NonNull
    public Cooldowns<K, V> cleanup() {
        this.entrySet().removeIf(entry -> entry.getValue().isExpired());
        return this;
    }
}