package memecat.fatcat.utilities.timed.holder;

import memecat.fatcat.utilities.timed.type.MinecraftTimed;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

/**
 * Constructor-only implementation of {@link AbstractCooldown} and {@link MinecraftTimed}.
 * @see MinecraftTimed
 */
public class MinecraftCooldown extends AbstractCooldown implements MinecraftTimed {

    public MinecraftCooldown(long duration, @NotNull TimeUnit unit) {
        this.end = this.getCurrentTime() + this.toEquivalentTime(duration, unit);
    }

    public MinecraftCooldown(long tickDuration) {
        this.end = this.getCurrentTime() + tickDuration;
    }
}