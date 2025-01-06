package com.cosimo.utilities.timed;

import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * A {@link Map} of {@link ICooldown} cleared lazily with the {@link #clearExpired()} method for purging expired entries
 * when needed.
 *
 * @param <K> Key type of this class's {@link Map}, {@link String} is suggested as it provides many variations for
 *            unique keys
 * @param <V> Expected {@link ICooldown} implementation
 */
@SuppressWarnings("unused")
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

    public Cooldowns(@NonNull Map<K, ? extends V> map) {
        super(map);
    }

    /**
     * Removes all {@link ICooldown} that have expired.
     *
     * @return Whether any {@link ICooldown} has expired and was removed
     */
    public boolean clearExpired() {
        return this.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
}