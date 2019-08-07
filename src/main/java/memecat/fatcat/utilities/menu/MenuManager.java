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
    private static MenuManager instance;

    /**
     * Currently registered plugin that has this listener registered under it's instance.
     */
    private static Plugin plugin;

    private MenuManager() {
    }

    /**
     * Closes the current inventory (menu) and opens a given {@link AbstractMenu} inventory for a given viewer.
     *
     * @param menu    Given {@link AbstractMenu} that will be shown to the given viewer
     * @param viewers Players who will see the given menu
     * @throws IllegalArgumentException If the menu or viewer argument is null
     * @throws IllegalStateException    If this inventory menu manager isn't registered for handling events
     * @throws NullPointerException     If the inventory of the given menu is null ({@link AbstractMenu#getInventory()})
     */
    public void openMenu(@NotNull AbstractMenu menu, @NotNull Player... viewers) {
        Preconditions.checkState(isRegistered(), "MenuManager has no plugins registered to handle inventory menu events.");
        Preconditions.checkArgument(menu != null, "Menu argument should never be null");
        Preconditions.checkArgument(viewers != null && viewers.length > 0 && viewers[0] != null, "Viewers array argument should never be null");

        Inventory inventory = Objects.requireNonNull(menu.getInventory(), "Menu inventory should never be null (AbstractMenu.getInventory()) for menu " + menu.getClass().getSimpleName());

        if (!(inventory.getHolder() instanceof AbstractMenu)) {
            holderMenus.add(menu);
            System.out.println("Added menu to holderMenus");
        }

        for (Player viewer : viewers) {
            Objects.requireNonNull(viewer, "Viewer argument in the viewers array should never be null");
            Optional<AbstractMenu> currentMenu = getMenu(viewer.getOpenInventory().getTopInventory());

            if (currentMenu.isPresent()) {
                currentMenu.get().setOpenNext(menu);
                viewer.closeInventory();
                System.out.println("Viewer for-loop if");
            } else {
                viewer.openInventory(inventory);
                System.out.println("Viewer for-loop else");
            }
        }
    }

    /**
     * Closes all {@link AbstractMenu} inventories that are a subclass of a given menu class.
     *
     * @param menuClass {@link AbstractMenu} subclass class, or the superclass (in that case, it'll close all menus)
     */
    public void closeMenus(@NotNull Class<? extends AbstractMenu> menuClass) {
        Preconditions.checkArgument(menuClass != null, "Menu class argument should never be null");

        if (menuClass == AbstractMenu.class) {
            closeAllMenus();
            return;
        }

        for (AbstractMenu openMenu : holderMenus) {
            if (menuClass.isInstance(openMenu)) {
                openMenu.getViewers().forEach(HumanEntity::closeInventory);
            }
        }

        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (menuClass.isInstance(viewer.getOpenInventory().getTopInventory().getHolder())) {
                viewer.closeInventory();
            }
        }
    }

    /**
     * Closes an {@link AbstractMenu} inventory for a given viewer.
     *
     * @param viewer Instance of a human entity, an inventory viewer whose {@link AbstractMenu} inventory will be closed
     * @return {@link AbstractMenu} of the given viewer, null if a menu hasn't been found or the viewer argument is null
     */
    @NotNull
    public Optional<AbstractMenu> closeMenu(@NotNull HumanEntity viewer) {
        Preconditions.checkArgument(viewer != null, "Viewer argument should never be null");
        Optional<AbstractMenu> openMenu = getMenu(viewer);
        openMenu.ifPresent(menu -> viewer.closeInventory());

        return openMenu;
    }

    /**
     * Closes all inventory menus that are currently open.
     * <p>
     * Not all inventory menus might be closed because each menu handler can decide to be reopened.
     */
    public static void closeAllMenus() {
        holderMenus.forEach(menu -> menu.getViewers().forEach(HumanEntity::closeInventory));
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            getHeldMenu(viewer).ifPresent(menu -> viewer.closeInventory());
        }
    }

    /**
     * Returns the current inventory {@link AbstractMenu} that a given player is viewing.
     *
     * @param viewer Player that's viewing an inventory
     * @return Optional of nullable {@link AbstractMenu} of the given viewer
     */
    @NotNull
    public Optional<AbstractMenu> getMenu(@NotNull HumanEntity viewer) {
        Preconditions.checkArgument(viewer != null, "Viewer argument shouldn't be null");
        return getMenu(viewer.getOpenInventory().getTopInventory());
    }

    /**
     * Returns the {@link AbstractMenu} that matches a given {@link Inventory} argument.
     *
     * @param inventory Inventory that possibly belongs to an {@link AbstractMenu}
     * @return Optional of nullable {@link AbstractMenu} that matches the given inventory
     */
    @NotNull
    public Optional<AbstractMenu> getMenu(@NotNull Inventory inventory) {
        Optional<AbstractMenu> menu = getHeldMenu(inventory);
        return menu.isPresent() ? menu : getTileMenu(inventory);
    }

    /**
     * Returns the {@link AbstractMenu} {@link InventoryHolder} stored in the given viewer's top inventory, or null if
     * there isn't any.
     *
     * @param viewer Viewer whose top inventory might be an {@link AbstractMenu}
     * @return Nullable optional of{@link AbstractMenu} {@link InventoryHolder} of this viewer's top inventory, null
     * if it's not a menu or viewer argument is null
     */
    @NotNull
    public static Optional<AbstractMenu> getHeldMenu(@NotNull HumanEntity viewer) {
        Preconditions.checkArgument(viewer != null, "Viewer argument shouldn't be null");
        return getHeldMenu(viewer.getOpenInventory().getTopInventory());
    }

    /**
     * Returns the {@link AbstractMenu} {@link InventoryHolder} stored in the given inventory, or null if there isn't any.
     *
     * @param inventory Inventory that might have an {@link AbstractMenu} {@link InventoryHolder}
     * @return Nullable optional of {@link AbstractMenu} {@link InventoryHolder} of a given matching inventory
     */
    @NotNull
    public static Optional<AbstractMenu> getHeldMenu(@NotNull Inventory inventory) {
        Preconditions.checkArgument(inventory != null, "Inventory argument shouldn't be null");

        InventoryHolder holder = inventory.getHolder();
        if (holder == null || !inventory.equals(holder.getInventory())) {
            return Optional.empty();
        }

        return Optional.ofNullable(holder instanceof AbstractMenu ? (AbstractMenu) holder : null);
    }

    /**
     * Returns the {@link AbstractMenu} inventory that matches the given viewer's top {@link Inventory} without an
     * {@link AbstractMenu} {@link InventoryHolder}.
     *
     * @param viewer Viewer of an inventory menu that has an {@link InventoryHolder} that's not an {@link AbstractMenu}
     * @return {@link AbstractMenu} or null
     */
    @NotNull
    public Optional<AbstractMenu> getTileMenu(@NotNull HumanEntity viewer) {
        Preconditions.checkArgument(viewer != null, "Viewer argument shouldn't be null");
        return getTileMenu(viewer.getOpenInventory().getTopInventory());
    }

    /**
     * Returns the {@link AbstractMenu} inventory that matches the given {@link Inventory} without an
     * {@link AbstractMenu} {@link InventoryHolder}.
     *
     * @param inventory Inventory menu that has an {@link InventoryHolder} that's not an {@link AbstractMenu}
     * @return {@link AbstractMenu} or null
     */
    @NotNull
    public Optional<AbstractMenu> getTileMenu(@NotNull Inventory inventory) {
        Preconditions.checkArgument(inventory != null, "Inventory argument shouldn't be null");

        for (AbstractMenu openMenu : holderMenus) {
            if (openMenu.getInventory().equals(inventory)) {
                return Optional.of(openMenu);
            }
        }

        return Optional.empty();
    }

    /**
     * Registers a new plugin for registering this {@link MenuManager} as a {@link Listener}
     *
     * @param newPlugin New enabled plugin that will register this {@link MenuManager} as a {@link Listener}
     * @return This {@link MenuManager} singleton
     * @throws IllegalArgumentException If the given plugin argument is of null value
     * @throws IllegalStateException    If the given plugin argument is not enabled
     */
    @NotNull
    public static MenuManager getInstance(@NotNull Plugin newPlugin) {
        if (instance == null || !isRegistered()) {
            Preconditions.checkArgument(newPlugin != null, "New plugin argument for registration should never be null");
            Preconditions.checkState(newPlugin.isEnabled(), "New plugin for registration is not enabled");

            Bukkit.getPluginManager().registerEvents(instance = new MenuManager(), plugin = newPlugin);
        }

        return instance;
    }

    /**
     * Returns the singleton of this {@link MenuManager} or throws an exception.
     *
     * @return This {@link MenuManager} singleton
     * @throws IllegalStateException If this singleton is not registered as a {@link Listener} yet through {@link #getInstance(Plugin)}
     */
    @NotNull
    public static MenuManager getInstance() {
        if (!isRegistered()) {
            throw new IllegalStateException("MenuManager currently has no plugin registered for it's Listener event handlers.");
        }

        return instance;
    }

    /**
     * Returns the {@link Optional} of a nullable registered plugin that registers this {@link Listener}'s event handlers.
     *
     * @return Optional of a nullable registered plugin
     */
    @NotNull
    public static Optional<Plugin> getPlugin() {
        return Optional.ofNullable(plugin);
    }

    /**
     * Returns whether this listener has it's events registered under a plugin that's enabled.
     *
     * @return Whether this listener has it's events registered under a plugin that's enabled
     */
    public static boolean isRegistered() {
        return plugin != null && plugin.isEnabled();
    }

    /**
     * Handles any inventory ItemStack or slot click event that is related to an inventory menu.
     *
     * @param event InventoryClickEvent event
     * @see AbstractMenu#onClick(InventoryClickEvent, boolean)
     */
    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        getMenu(event.getWhoClicked()).ifPresent(menu -> {
            try {
                menu.onClick(event, event.getClickedInventory() == null || event.getClickedInventory() != menu.getInventory());
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
            if ((menu.getViewers().size() < 2) && !holderMenus.isEmpty()) {
                holderMenus.remove(menu);
            }

            try {
                menu.onClose(event);
            } catch (Exception e) {
                getPlugin().ifPresent(p -> p.getLogger().warning("An error occurred while handling a menu closing event."));
                e.printStackTrace();
            } finally {
                try {
                    Optional<AbstractMenu> nextMenu = menu.getOpenNext();

                    if (nextMenu.isPresent()) {
                        Inventory inventory = Objects.requireNonNull(nextMenu.get().getInventory(), "Menu Inventory should never be null (AbstractMenu.getInventory()) for menu " + menu.getClass().getSimpleName());

                        if (!(inventory.getHolder() instanceof AbstractMenu)) {
                            holderMenus.add(nextMenu.get());
                        }

                        final HumanEntity viewer = event.getPlayer();
                        getPlugin().ifPresent(plugin -> Bukkit.getScheduler().runTask(plugin, () -> viewer.openInventory(inventory)));
                    }
                } catch (Exception e) {
                    getPlugin().ifPresent(p -> p.getLogger().warning("An error occurred while attempting to open the next menu."));
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Handles any inventory opening events that are related to inventory menus.
     *
     * @param event InventoryOpenEvent event object
     * @see AbstractMenu#onOpen(InventoryOpenEvent)
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
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
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        getMenu(event.getWhoClicked()).ifPresent(menu -> {
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
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onItemMove(InventoryMoveItemEvent event) {
        boolean equalsDestination = false;
        Optional<AbstractMenu> menu = getHeldMenu(event.getDestination());

        if (menu.isPresent()) {
            equalsDestination = true;
        } else if (!(menu = getHeldMenu(event.getSource())).isPresent()) {
            for (AbstractMenu openMenu : holderMenus) {
                if (openMenu.getInventory().equals(event.getDestination())) {
                    menu = Optional.of(openMenu);
                    equalsDestination = true;
                    break;
                } else if (openMenu.getInventory().equals(event.getSource())) {
                    menu = Optional.of(openMenu);
                    break;
                }
            }

            if (!menu.isPresent()) {
                return;
            }
        }

        try {
            menu.get().onItemMove(event, equalsDestination);
        } catch (Exception e) {
            getPlugin().ifPresent(p -> p.getLogger().warning("An error occurred while handling a menu item movement event."));
            e.printStackTrace();
        }
    }

    /**
     * Handles a {@link PluginDisableEvent} to all inventory menus and stops passing it to menus if a menu registers a
     * new {@link Plugin} to {@link MenuManager}.
     *
     * @param event PluginDisableEvent event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPluginDisable(PluginDisableEvent event) {
        Optional<Plugin> p = getPlugin();

        if (p.isPresent() && event.getPlugin().equals(p.get())) {
            plugin = UtilitiesPlugin.getInstance().isPresent() ? UtilitiesPlugin.getInstance().get() : null;
            for (AbstractMenu menu : holderMenus) {
                if (isRegistered()) {
                    return;
                }

                menu.onDisable(event, this);
            }

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (isRegistered()) {
                    return;
                }

                getHeldMenu(player).ifPresent(menu -> menu.onDisable(event, this));
            }
        }
    }
}