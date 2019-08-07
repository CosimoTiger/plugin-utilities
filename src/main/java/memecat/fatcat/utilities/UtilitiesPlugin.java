package memecat.fatcat.utilities;

import memecat.fatcat.utilities.menu.MenuManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;

/**
 * Allows plugin-utilities to function as it's own plugin.
 */
public class UtilitiesPlugin extends JavaPlugin {

    private static UtilitiesPlugin instance;

    @Override
    public void onEnable() {
        MenuManager.getInstance(instance = this);
    }

    /**
     * Returns the nullable singleton {@link JavaPlugin} instance of this {@link UtilitiesPlugin}.
     *
     * @return The {@link Optional} nullable singleton instance of this class, not null when enabled
     */
    public static Optional<UtilitiesPlugin> getInstance() {
        return Optional.ofNullable(instance);
    }
}