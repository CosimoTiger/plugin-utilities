package memecat.fatcat.utilities.menu;

import com.google.common.base.Preconditions;
import memecat.fatcat.utilities.UtilitiesPlugin;
import memecat.fatcat.utilities.menu.menus.AbstractMenu;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
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
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A class that controls inventory menus, distinguishes inventory menu events and passes them to their menus.
 * <p>
 * <strong>Warning: Multiple instances of this class that are also registered as {@link Listener}s may cause multiple
 * event handler calls for every event of a menu that's registered in multiple {@link MenuManager}. It is advised that
 * you use {@link UtilitiesPlugin#getMenuManager()} or {@link UtilitiesPlugin#getMenuManager(Plugin)} to register your
 * own {@link MenuManager} instance there, accessible to all {@link Plugin}s that are using this library.</strong>
 *
 * @author Alan B.
 */
public class MenuManager implements Listener {

    /**
     * All menus are stored here at a 1:1 ratio (not tested) while being viewed, compared to a {@link HumanEntity} key
     * to {@link AbstractMenu} value which can grower much larger (example: 50 players viewing the same menu would cause
     * 50 keys), while in this case it's one {@link Inventory} key for one {@link AbstractMenu} value.
     */
    private Map<Inventory, AbstractMenu> menus = new HashMap<>(8);

    /**
     * Currently registered plugin that has this listener registered under it's instance.
     */
    private Plugin provider;

    /**
     * Creates a new instance that registers itself with the enabled {@link Listener} provider {@link Plugin}.
     *
     * @param provider Not null enabled {@link Plugin} for registering this {@link Listener} instance's event handlers
     * @throws IllegalArgumentException If the provider argument is null
     * @throws IllegalStateException    If the provider argument is not enabled
     */
    public MenuManager(@NotNull Plugin provider) {
        Bukkit.getPluginManager().registerEvents(this, this.provider = UtilitiesPlugin.checkProvider(provider));
    }

    /**
     * Removes a given {@link AbstractMenu} from this {@link MenuManager}'s {@link HashMap}, causing it to not be
     * detectable by this {@link MenuManager}.
     *
     * @param menu Not null {@link AbstractMenu} that'll be unregistered
     * @return {@link AbstractMenu} that was a value to the given {@link AbstractMenu}'s {@link Inventory}
     */
    public Optional<AbstractMenu> unregisterMenu(@NotNull AbstractMenu menu) {
        Preconditions.checkArgument(menu != null, "AbstractMenu argument can't be null");
        return Optional.ofNullable(menus.remove(menu.getInventory()));
    }

    /**
     * Puts a given {@link AbstractMenu} into this {@link MenuManager}'s {@link HashMap}, causing any next events to be
     * handled to the {@link AbstractMenu} until it's unregistered.
     *
     * @param menu Not null {@link AbstractMenu} that'll be registered
     * @return Previous {@link AbstractMenu} that was a value to the given {@link AbstractMenu}'s {@link Inventory}
     */
    public Optional<AbstractMenu> registerMenu(@NotNull AbstractMenu menu) {
        Preconditions.checkArgument(menu != null, "AbstractMenu argument can't be null");
        return Optional.ofNullable(menus.put(menu.getInventory(), menu));
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
    public void closeMenus() {
        menus.forEach((inventory, menu) -> menu.close());
    }

    /**
     * Returns the {@link AbstractMenu} that matches a given {@link Inventory} argument.
     *
     * @param inventory Inventory that possibly belongs to an {@link AbstractMenu}
     * @return Optional of nullable {@link AbstractMenu} that matches the given inventory
     * @throws IllegalArgumentException If the inventory argument is null
     */
    @NotNull
    public Optional<AbstractMenu> getMenu(@NotNull Inventory inventory) {
        Preconditions.checkArgument(inventory != null, "Inventory argument can't be null");
        return Optional.ofNullable(menus.get(inventory));
    }

    /**
     * Returns the current inventory {@link AbstractMenu} that a given player is viewing.
     *
     * @param viewer {@link HumanEntity} that's viewing an inventory
     * @return Optional of nullable {@link AbstractMenu} of the given viewer's top inventory
     * @throws IllegalArgumentException If the viewer argument is null
     */
    @NotNull
    public Optional<AbstractMenu> getMenu(@NotNull HumanEntity viewer) {
        Preconditions.checkArgument(viewer != null, "Viewer argument can't be null");
        return getMenu(viewer.getOpenInventory().getTopInventory());
    }

    /**
     * Returns the unmodifiable map ({@link Collections#unmodifiableMap(Map)}) of this {@link MenuManager}'s menu {@link
     * HashMap} of {@link Inventory} keys to {@link AbstractMenu} values.
     *
     * @return Unmodifiable view of this {@link MenuManager}'s {@link HashMap}
     */
    public Map<Inventory, AbstractMenu> getMap() {
        return Collections.unmodifiableMap(menus);
    }

    /**
     * Returns the {@link Optional} of a nullable {@link Plugin} that registers this {@link Listener}'s event handlers.
     *
     * @return {@link Optional} of a nullable {@link Plugin}
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
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
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
     * <p>
     * An {@link AbstractMenu} is removed in this event handler when it's viewer count is less than 2. This is to make
     * sure that {@link AbstractMenu}s are removed and not piled up into the {@link HashMap}&lt;{@link Inventory},
     * {@link AbstractMenu}&gt; which could result in too much memory usage.
     *
     * @param event InventoryCloseEvent event
     * @see AbstractMenu#onClose(InventoryCloseEvent)
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(@NotNull InventoryCloseEvent event) {
        getMenu(event.getInventory()).ifPresent(menu -> {
            if (menu.getInventory().getViewers().size() < 2) {
                menus.remove(menu.getInventory());
            }

            try {
                menu.onClose(event);
            } catch (Exception e) {
                getPlugin().ifPresent(p -> p.getLogger().warning("An error occurred while handling a menu closing event."));
                e.printStackTrace();
            }

            try {
                menu.getNext().ifPresent(nextMenu -> {
                    final HumanEntity viewer = event.getPlayer();
                    getPlugin().ifPresent(plugin -> Bukkit.getScheduler().runTask(plugin, () -> nextMenu.open(viewer)));
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
    public void onInventoryOpen(@NotNull InventoryOpenEvent event) {
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
    public void onInventoryDrag(@NotNull InventoryDragEvent event) {
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
    public void onItemMove(@NotNull InventoryMoveItemEvent event) {
        Optional<AbstractMenu> m = getMenu(event.getDestination());
        AtomicBoolean isDestination = new AtomicBoolean();

        if (m.isPresent()) {
            isDestination.set(true);
        } else {
            m = getMenu(event.getSource());
        }

        m.ifPresent(menu -> {
            try {
                menu.onItemMove(event, isDestination.get());
            } catch (Exception e) {
                getPlugin().ifPresent(p -> p.getLogger().warning("An error occurred while handling a menu item movement event."));
                e.printStackTrace();
            }
        });
    }

    /**
     * Handles a {@link PluginDisableEvent} of this {@link MenuManager}'s {@link Listener} provider to all inventory
     * menus.
     *
     * @param event PluginDisableEvent event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginDisable(@NotNull PluginDisableEvent event) {
        if (!event.getPlugin().equals(provider)) {
            return;
        }

        provider = null;

        menus.forEach((inventory, menu) -> {
            try {
                menu.onDisable(event);
            } catch (Exception e) {
                getPlugin().ifPresent(p -> p.getLogger().warning("An error occurred while handling a menu plugin disable event."));
                e.printStackTrace();
            }
        });
    }
}