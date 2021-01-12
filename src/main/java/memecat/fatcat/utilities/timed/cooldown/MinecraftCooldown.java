package memecat.fatcat.utilities.timed.cooldown;

import memecat.fatcat.utilities.timed.type.MinecraftTimed;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class MinecraftCooldown extends AbstractCooldown implements MinecraftTimed {

    public MinecraftCooldown(long duration, @NotNull TimeUnit unit) {
        this(unit.toMillis(duration) / 50);
    }

    public MinecraftCooldown(long tickDuration) {
        end = getCurrentTime() + tickDuration;
    }
}