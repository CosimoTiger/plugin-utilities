package memecat.fatcat.utilities.timed.type;

import memecat.fatcat.utilities.timed.ITimed;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

/**
 * Implementation of {@link ITimed} that functions by using the Minecraft 'tick' unit equivalent to 50 milliseconds,
 * 0.05 seconds or 20th part of a second, therefore 20 ticks per second which is equal to 1000 milliseconds.
 */
public interface MinecraftTimed extends ITimed {

    /**
     * Returns the counted time in ticks since the {@link org.bukkit.Server}'s {@link CraftServer}'s
     * {@link net.minecraft.server.v1_16_R3.DedicatedServer} started up.
     *
     * @return Time in ticks since the Minecraft server's boot
     */
    @Override
    default long getCurrentTime() {
        return ((CraftServer) Bukkit.getServer()).getServer().ai();
    }

    /**
     * Converts the given time from the given {@link TimeUnit} into a duration in the unit equivalent to
     * {@link #getCurrentTime()}'s unit.
     *
     * @param unit {@link TimeUnit} of the given duration argument
     * @param duration Time duration to be converted
     * @return Time duration in an unit equivalent to {@link #getCurrentTime()}'s
     */
    @Override
    default long toEquivalentTime(long duration, @NotNull TimeUnit unit) {
        return unit.toMillis(duration) / 50;
    }

    @Override
    default long fromEquivalentTime(long duration, @NotNull TimeUnit unit) {
        return TimeUnit.MILLISECONDS.convert(duration * 50, unit);
    }
}