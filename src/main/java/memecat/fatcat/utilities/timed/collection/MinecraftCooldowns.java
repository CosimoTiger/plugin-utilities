package memecat.fatcat.utilities.timed.collection;

import com.google.common.base.Preconditions;
import memecat.fatcat.utilities.timed.type.MinecraftTimed;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

/**
 * Constructor-only implementation of {@link AbstractCooldowns} and {@link MinecraftTimed}.
 * @see MinecraftTimed
 */
public class MinecraftCooldowns<K> extends AbstractCooldowns<K> implements MinecraftTimed {

    /**
     * Creates a new {@link MinecraftCooldowns} holder by creating a new {@link HashMap} from the entires of the given
     * {@link AbstractCooldowns}.
     *
     * @param initial The original {@link AbstractCooldowns} whose contents will be transferred into this new one
     */
    public MinecraftCooldowns(@NotNull AbstractCooldowns<K> initial) {
        Preconditions.checkArgument(initial != null, "Initial Cooldowns object can't be null");
        cooldowns = new HashMap<>(initial.cooldowns);
    }

    /**
     * Creates a new {@link MinecraftCooldowns} holder by creating a new {@link HashMap} with initialCapacity of 8.
     */
    public MinecraftCooldowns() {
        cooldowns = new HashMap<>(8);
    }
}