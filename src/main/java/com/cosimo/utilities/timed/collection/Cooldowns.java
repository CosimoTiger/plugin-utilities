package com.cosimo.utilities.timed.collection;

import com.google.common.base.Preconditions;
import com.cosimo.utilities.timed.holder.AbstractCooldown;

import javax.annotation.Nonnull;
import java.util.HashMap;

/**
 * Constructor-only implementation of {@link AbstractCooldown}.
 */
public class Cooldowns<K> extends AbstractCooldowns<K> {

    /**
     * Creates a new {@link Cooldowns} holder by creating a new {@link HashMap} from the entires of the given
     * {@link AbstractCooldowns}.
     *
     * @param initial The original {@link AbstractCooldowns} whose contents will be transferred into this new one
     */
    public Cooldowns(@Nonnull AbstractCooldowns<K> initial) {
        Preconditions.checkArgument(initial != null, "Initial Cooldowns object can't be null");
        this.cooldowns = new HashMap<>(initial.cooldowns);
    }

    /**
     * Creates a new {@link Cooldowns} holder by creating a new {@link HashMap} with initialCapacity of 8.
     */
    public Cooldowns() {
        this.cooldowns = new HashMap<>(8);
    }
}