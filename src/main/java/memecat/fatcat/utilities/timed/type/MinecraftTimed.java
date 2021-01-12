package memecat.fatcat.utilities.timed.type;

import memecat.fatcat.utilities.timed.ITimed;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;

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
}