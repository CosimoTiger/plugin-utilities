package memecat.fatcat.utilities;

import com.google.common.base.Preconditions;
import memecat.fatcat.utilities.menu.MenuManager;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Allows plugin-utilities to function as its own plugin and lets other plugins use a common {@link MenuManager}.
 */
public class UtilitiesPlugin extends JavaPlugin {

    private static UtilitiesPlugin instance;
    private static MenuManager menuManager;

    @Override
    public void onEnable() {
        instance = this;
    }

    @Override
    public void onDisable() {
        instance = null;
    }

    /**
     * Checks whether a given {@link Plugin} argument is not null and enabled or else an exception is thrown.
     *
     * @param provider {@link Plugin} argument that's being checked for
     * @return The given {@link Plugin} argument
     * @throws IllegalArgumentException If the {@link Plugin} argument is null
     * @throws IllegalStateException    If the {@link Plugin} argument is not enabled
     */
    @NotNull
    public static Plugin checkProvider(@NotNull Plugin provider) {
        Preconditions.checkArgument(provider != null, "Plugin provider argument can't be null");
        Preconditions.checkState(provider.isEnabled(), "Plugin provider argument can't be disabled");

        return provider;
    }

    /**
     * Returns the {@link MenuManager} of this {@link UtilitiesPlugin} if it's enabled, or else the {@link Plugin}
     * argument is provided or a new {@link MenuManager} is instantiated with the given {@link Plugin} argument.
     *
     * <p>It is required to provide the {@link Plugin} only upon plugin start to ensure the {@link MenuManager} has a
     * plugin to begin with.
     *
     * @param provider Backup {@link Plugin} that will be used for the creation or providing for this {@link UtilitiesPlugin}'s
     *                {@link MenuManager} if the manager's without an enabled plugin
     * @return Not null registered {@link MenuManager}
     * @throws IllegalArgumentException If the {@link Plugin} argument is null when required
     * @throws IllegalStateException    If the {@link Plugin} argument is not enabled when required
     * @see MenuManager#MenuManager(Plugin)
     * @see MenuManager#provide(Plugin)
     */
    @NotNull
    public static MenuManager getMenuManager(@NotNull Plugin provider) {
        if (instance == null) {
            return menuManager == null ? menuManager = new MenuManager(provider) : menuManager.provide(provider);
        }

        return menuManager == null ? menuManager = new MenuManager(instance).provide(provider) : menuManager.provide(provider);
    }

    /**
     * Returns {@link Optional<MenuManager>} of the common {@link MenuManager} that might be null or not, depending on
     * the state.
     *
     * @return {@link Optional<MenuManager>} of nullable {@link MenuManager}
     */
    @NotNull
    public static Optional<MenuManager> getMenuManager() {
        return Optional.ofNullable(menuManager);
    }

    /**
     * Returns the nullable singleton {@link JavaPlugin} instance of this {@link UtilitiesPlugin}.
     *
     * @return {@link Optional} nullable singleton instance of this class, not null when enabled
     */
    @NotNull
    public static Optional<UtilitiesPlugin> getInstance() {
        return Optional.ofNullable(instance);
    }
}