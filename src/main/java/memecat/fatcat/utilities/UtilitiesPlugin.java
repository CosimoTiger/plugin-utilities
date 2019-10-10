package memecat.fatcat.utilities;

import com.google.common.base.Preconditions;
import memecat.fatcat.utilities.menu.MenuManager;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Allows plugin-utilities to function as it's own plugin and lets other plugins use a common {@link MenuManager}.
 */
public class UtilitiesPlugin extends JavaPlugin {

    private static UtilitiesPlugin instance;
    private static MenuManager menuManager;

    @Override
    public void onEnable() {
        instance = this;
    }

    /**
     * Checks whether a given {@link Plugin} argument is not null and enabled or else an exception is thrown.
     *
     * @param provider {@link Plugin} argument that's being checked for
     * @return The given {@link Plugin} provider argument
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
     * <p>Exceptions will arise from a bad argument if the {@link MenuManager} is uninitialised or currently not
     * registered.
     *
     * <p>May also be acquired as the following:
     * {@code MenuManager menuManager = UtilitiesPlugin.getMenuManager(UtilitiesPlugin.getInstance().orElse(otherPlugin))}
     *
     * @param failure {@link Plugin} that will be used for the creation or providing for this {@link UtilitiesPlugin}'s
     *                {@link MenuManager}
     * @return Not null registered {@link MenuManager}
     * @throws IllegalArgumentException If the {@link Plugin} argument is null
     * @throws IllegalStateException    If the {@link Plugin} argument is not enabled
     * @see MenuManager#MenuManager(Plugin)
     * @see MenuManager#provide(Plugin)
     */
    @NotNull
    public static MenuManager getMenuManager(@NotNull Plugin failure) {
        if (menuManager == null) {
            return menuManager = new MenuManager(failure);
        }

        menuManager.provide(failure);

        return menuManager;
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