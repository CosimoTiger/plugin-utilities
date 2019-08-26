package memecat.fatcat.utilities.menu;

import com.google.common.base.Preconditions;
import memecat.fatcat.utilities.UtilitiesPlugin;
import memecat.fatcat.utilities.menu.menus.AbstractMenu;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * A singleton class that controls inventory menus, distinguishes inventory menu events and passes them to their menus.
 * <p>
 * On instantiation, {@link AbstractMenu}s should try to make themselves an {@link InventoryHolder} of their own
 * {@link Inventory} if it has no holder. This will make them faster to find for all of the events that this
 * {@link MenuManager} supports, except for {@link PluginDisableEvent} which is actually faster for the special case of
 * menus that are stored in this class's {@link HashSet} of {@link AbstractMenu}s with an already occupied
 * {@link InventoryHolder}.
 * <p>
 * <strong>Warning: Multiple instances of this class that are also registered as {@link Listener}s will cause multiple event
 * handler calls for every event. It is advised that you use {@link UtilitiesPlugin#getMenuManager()} or
 * {@link UtilitiesPlugin#getMenuManager(Plugin)} to register your own {@link MenuManager} instance there, accessible to
 * all {@link Plugin}s that are using this library.</strong>
 *
 * @author Alan B.
 */
public class MenuManager implements Listener {

    /**
     * Menus that have an actual {@link InventoryHolder} (not {@link AbstractMenu}) are stored here while being viewed
     * to allow events to access them as their {@link InventoryHolder} variable is already taken by a tile block or
     * entity (chest, furnace, horse..).
     */
    private static Set<AbstractMenu> holderMenus = new HashSet<>(2);

    /**
     * Currently registered plugin that has this listener registered under it's instance.
     */
    private Plugin provider;

    /**
     * Creates a new instance that registers itself with the enabled {@link Listener} provider {@link Plugin}.
     *
     * @param provider Not null enabled {@link Plugin} for registering this {@link Listener} instance's
     *                 event handlers
     * @throws IllegalArgumentException If the provider argument is null
     * @throws IllegalStateException    If the provider argument is not enabled
     */
    public MenuManager(@NotNull Plugin provider) {
        Bukkit.getPluginManager().registerEvents(this, this.provider = UtilitiesPlugin.checkProvider(provider));
    }

    /**
     * Closes the current inventory (menu) and opens a given {@link AbstractMenu} inventory for a given viewer.
     *
     * @param menu    Given {@link AbstractMenu} that will be shown to the given viewer
     * @param viewers Viewers who will see the given menu
     * @throws IllegalArgumentException If the menu or viewer argument is null
     * @throws IllegalStateException    If this inventory menu manager isn't registered for handling events
     * @throws NullPointerException     If the inventory of the given menu is null ({@link AbstractMenu#getInventory()})
     */
    public void openMenu(@NotNull AbstractMenu menu, @NotNull HumanEntity... viewers) {
        Preconditions.checkState(isRegistered(), "MenuManager has no plugin registered to handle inventory menu events");
        Preconditions.checkArgument(menu != null, "Menu argument can't be null");
        Preconditions.checkArgument(viewers != null && viewers.length > 0 && viewers[0] != null, "Viewers array argument can't be null");

        Inventory inventory = Objects.requireNonNull(menu.getInventory(), "Menu inventory can't be null (AbstractMenu.getInventory()) for menu " + menu.getClass().getSimpleName());

        if (!(inventory.getHolder() instanceof AbstractMenu)) {
            holderMenus.add(menu);
        }

        for (HumanEntity viewer : viewers) {
            Objects.requireNonNull(viewer, "Viewer argument in the viewers array can't be null");
            Optional<AbstractMenu> currentMenu = getMenu(viewer.getOpenInventory().getTopInventory());

            if (currentMenu.isPresent()) {
                currentMenu.get().setOpenNext(menu);
                viewer.closeInventory();
            } else {
                viewer.openInventory(inventory);
            }
        }
    }

    /**
     * Closes all {@link AbstractMenu} inventories that are a subclass of a given menu class.
     *
     * @param menuClass {@link AbstractMenu} subclass class, or the superclass (in that case, it'll close all menus)
     */
    public void closeMenus(@NotNull Class<? extends AbstractMenu> menuClass) {
        Preconditions.checkArgument(menuClass != null, "Menu class argument can't be null");

        if (menuClass == AbstractMenu.class) {
            closeAllMenus();
            return;
        }

        for (AbstractMenu holderMenu : holderMenus) {
            if (menuClass.isInstance(holderMenu)) {
                holderMenu.close();
            }
        }

        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (menuClass.isInstance(viewer.getOpenInventory().getTopInventory().getHolder())) {
                viewer.closeInventory();
            }
        }
    }

    /**
     * Provides a new {@link Plugin} to register this {@link Listener}'s events if it's current provider is null or
     * disabled.
     *
     * @param provider Not null enabled {@link Plugin} for registering this {@link Listener} instance's event handlers
     * @return Whether this {@link Listener} was successfully registered
     * @throws IllegalArgumentException If the provider argument is null
     * @throws IllegalStateException    If the provider argument is not enabled
     */
    public boolean provide(@NotNull Plugin provider) {
        if (isRegistered()) {
            return false;
        }

        Bukkit.getPluginManager().registerEvents(this, this.provider = UtilitiesPlugin.checkProvider(provider));
        return true;
    }

    /**
     * Closes all inventory menus that are currently open.
     * <p>
     * Not all inventory menus might be closed because each menu handler can decide to be reopened.
     */
    public void closeAllMenus() {
        holderMenus.forEach(AbstractMenu::close);
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewer.getOpenInventory().getTopInventory().getHolder() instanceof AbstractMenu) {
                viewer.closeInventory();
            }
        }
    }

    /**
     * Returns the {@link AbstractMenu} {@link InventoryHolder} stored in the given viewer's top inventory.
     *
     * @param viewer Viewer whose top inventory might be an {@link AbstractMenu}
     * @return Optional of nullable {@link AbstractMenu} {@link InventoryHolder} of the given viewer's top inventory
     * @throws IllegalArgumentException If the viewer argument is null
     */
    @NotNull
    public static Optional<AbstractMenu> getHeldMenu(@NotNull HumanEntity viewer) {
        Preconditions.checkArgument(viewer != null, "Viewer argument can't be null");
        return getHeldMenu(viewer.getOpenInventory().getTopInventory());
    }

    /**
     * Returns the {@link AbstractMenu} {@link InventoryHolder} stored in the given inventory through
     * {@link Inventory#getHolder()}.
     *
     * @param inventory Inventory that might have an {@link AbstractMenu} {@link InventoryHolder}
     * @return Optional of nullable {@link AbstractMenu} {@link InventoryHolder} of a given matching inventory
     * @throws IllegalArgumentException If the inventory argument is null
     */
    @NotNull
    public static Optional<AbstractMenu> getHeldMenu(@NotNull Inventory inventory) {
        Preconditions.checkArgument(inventory != null, "Inventory argument can't be null");

        InventoryHolder holder = inventory.getHolder();

        return Optional.ofNullable(holder instanceof AbstractMenu && holder.getInventory().equals(inventory) ? (AbstractMenu) holder : null);
    }

    /**
     * Returns the {@link AbstractMenu} inventory that matches the given viewer's top {@link Inventory} without an
     * {@link AbstractMenu} {@link InventoryHolder}.
     *
     * @param viewer Viewer of an inventory menu that has an {@link InventoryHolder} that's not an {@link AbstractMenu}
     * @return Optional of nullable {@link AbstractMenu} of the given viewer's top inventory
     * @throws IllegalArgumentException If the viewer argument is null
     */
    @NotNull
    public static Optional<AbstractMenu> getTileMenu(@NotNull HumanEntity viewer) {
        Preconditions.checkArgument(viewer != null, "Viewer argument can't be null");
        return getTileMenu(viewer.getOpenInventory().getTopInventory());
    }

    /**
     * Returns the {@link AbstractMenu} inventory that matches the given {@link Inventory} without an
     * {@link AbstractMenu} {@link InventoryHolder}.
     *
     * @param inventory Inventory menu that has an {@link InventoryHolder} that's not an {@link AbstractMenu}
     * @return Optional of nullable {@link AbstractMenu} that matches the given inventory
     * @throws IllegalArgumentException If the inventory argument is null
     */
    @NotNull
    public static Optional<AbstractMenu> getTileMenu(@NotNull Inventory inventory) {
        Preconditions.checkArgument(inventory != null, "Inventory argument can't be null");

        for (AbstractMenu holderMenu : holderMenus) {
            if (holderMenu.getInventory().equals(inventory)) {
                return Optional.of(holderMenu);
            }
        }

        return Optional.empty();
    }

    /**
     * Returns the {@link AbstractMenu} that matches a given {@link Inventory} argument.
     * <p>
     * This method first uses the method {@link #getHeldMenu(Inventory)} and then {@link #getTileMenu(Inventory)} if an
     * {@link AbstractMenu} is not found.
     *
     * @param inventory Inventory that possibly belongs to an {@link AbstractMenu}
     * @return Optional of nullable {@link AbstractMenu} that matches the given inventory
     * @throws IllegalArgumentException If the inventory argument is null
     */
    @NotNull
    public static Optional<AbstractMenu> getMenu(@NotNull Inventory inventory) {
        Optional<AbstractMenu> menu = getHeldMenu(inventory);
        return menu.isPresent() ? menu : getTileMenu(inventory);
    }

    /**
     * Returns the current inventory {@link AbstractMenu} that a given player is viewing.
     * <p>
     * This method first uses the method {@link #getHeldMenu(Inventory)} and then {@link #getTileMenu(Inventory)} if an
     * {@link AbstractMenu} is not found.
     *
     * @param viewer Player that's viewing an inventory
     * @return Optional of nullable {@link AbstractMenu} of the given viewer's top inventory
     * @throws IllegalArgumentException If the viewer argument is null
     */
    @NotNull
    public static Optional<AbstractMenu> getMenu(@NotNull HumanEntity viewer) {
        Preconditions.checkArgument(viewer != null, "Viewer argument can't be null");
        return getMenu(viewer.getOpenInventory().getTopInventory());
    }

    /**
     * Returns the {@link Optional} of a nullable registered plugin that registers this {@link Listener}'s event handlers.
     *
     * @return Optional of a nullable registered plugin
     */
    @NotNull
    public Optional<Plugin> getPlugin() {
        return Optional.ofNullable(provider);
    }

    /**
     * Returns whether this listener has it's events registered under a plugin that's enabled.
     *
     * @return Whether this listener has it's events registered under a plugin that's enabled
     */
    public boolean isRegistered() {
        return provider != null && provider.isEnabled();
    }

    /**
     * Handles any inventory ItemStack or slot click event that is related to an inventory menu.
     *
     * @param event InventoryClickEvent event
     * @see AbstractMenu#onClick(InventoryClickEvent, boolean)
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        getMenu(event.getInventory()).ifPresent(menu -> {
            try {
                menu.onClick(event, event.getClickedInventory() == null || !event.getClickedInventory().equals(menu.getInventory()));
            } catch (Exception e) {
                getPlugin().ifPresent(p -> p.getLogger().warning("An error occurred while handling a menu click event."));
                e.printStackTrace();
            }
        });
    }

    /**
     * Handles any inventory closing event that is related to an inventory menu, and opens next inventory menus.
     *
     * @param event InventoryCloseEvent event
     * @throws NullPointerException If the {@link AbstractMenu#getInventory} function returns null
     * @see AbstractMenu#onClose(InventoryCloseEvent)
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        getMenu(event.getInventory()).ifPresent(menu -> {
            if (!holderMenus.isEmpty() && menu.getViewers().size() < 2) {
                holderMenus.remove(menu);
            }

            try {
                menu.onClose(event);
            } catch (Exception e) {
                getPlugin().ifPresent(p -> p.getLogger().warning("An error occurred while handling a menu closing event."));
                e.printStackTrace();
            }

            try {
                menu.getOpenNext().ifPresent(nextMenu -> {
                    final HumanEntity viewer = event.getPlayer();
                    getPlugin().ifPresent(plugin -> Bukkit.getScheduler().runTask(plugin, () -> openMenu(nextMenu, viewer)));
                });
            } catch (Exception e) {
                getPlugin().ifPresent(p -> p.getLogger().warning("An error occurred while attempting to open the next menu."));
                e.printStackTrace();
            }
        });
    }

    /**
     * Handles any inventory opening events that are related to inventory menus.
     *
     * @param event InventoryOpenEvent event object
     * @see AbstractMenu#onOpen(InventoryOpenEvent)
     */
    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        getMenu(event.getInventory()).ifPresent(menu -> {
            try {
                menu.onOpen(event);
            } catch (Exception e) {
                getPlugin().ifPresent(p -> p.getLogger().warning("An error occurred while handling a menu opening event."));
                e.printStackTrace();
            }
        });
    }

    /**
     * Handles any inventory item stack dragging events that are related to inventory menus.
     *
     * @param event InventoryDragEvent event object
     * @see AbstractMenu#onDrag(InventoryDragEvent)
     */
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        getMenu(event.getInventory()).ifPresent(menu -> {
            try {
                menu.onDrag(event);
            } catch (Exception e) {
                getPlugin().ifPresent(p -> p.getLogger().warning("An error occurred while handling a menu open event."));
                e.printStackTrace();
            }
        });
    }

    /**
     * Handles any inventory item stack movement events that are related to inventory menus.
     *
     * @param event InventoryMoveItemEvent event
     * @see AbstractMenu#onItemMove(InventoryMoveItemEvent, boolean)
     */
    @EventHandler
    public void onItemMove(InventoryMoveItemEvent event) {
        boolean isDestination = false;
        Optional<AbstractMenu> menu = getHeldMenu(event.getDestination());

        if (menu.isPresent()) {
            isDestination = true;
        } else if (!(menu = getHeldMenu(event.getSource())).isPresent()) {
            for (AbstractMenu holderMenu : holderMenus) {
                if (holderMenu.getInventory().equals(event.getDestination())) {
                    menu = Optional.of(holderMenu);
                    isDestination = true;
                    break;
                } else if (holderMenu.getInventory().equals(event.getSource())) {
                    menu = Optional.of(holderMenu);
                    break;
                }
            }

            if (!menu.isPresent()) {
                return;
            }
        }

        try {
            menu.get().onItemMove(event, isDestination);
        } catch (Exception e) {
            getPlugin().ifPresent(p -> p.getLogger().warning("An error occurred while handling a menu item movement event."));
            e.printStackTrace();
        }
    }

    /**
     * Handles a {@link PluginDisableEvent} of this {@link MenuManager}'s {@link Listener} provider to all inventory menus.
     *
     * @param event PluginDisableEvent event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginDisable(PluginDisableEvent event) {
        if (!event.getPlugin().equals(provider)) {
            return;
        }

        provider = null;

        for (AbstractMenu holderMenu : holderMenus) {
            try {
                holderMenu.onDisable(event, this);
            } catch (Exception e) {
                getPlugin().ifPresent(p -> p.getLogger().warning("An error occurred while handling a menu plugin disable event."));
                e.printStackTrace();
            }
        }

        Set<AbstractMenu> toDisable = new HashSet<>();

        Bukkit.getOnlinePlayers().forEach(player -> getHeldMenu(player).ifPresent(toDisable::add));

        for (AbstractMenu menu : toDisable) {
            try {
                menu.onDisable(event, this);
            } catch (Exception e) {
                getPlugin().ifPresent(p -> p.getLogger().warning("An error occurred while handling a menu plugin disable event."));
                e.printStackTrace();
            }
        }
    }
}