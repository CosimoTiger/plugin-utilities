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

/**
 * A class that filters {@link org.bukkit.event.inventory.InventoryEvent}s to registered {@link Inventory} {@link
 * AbstractMenu}s.
 *
 * <p><strong>Note:</strong> {@link AbstractMenu}s should be unregistered from their {@link MenuManager} when they're
 * not in use anymore, such as when the last viewer closes an {@link AbstractMenu}. The unfollowing of this rule will
 * cause the increase in memory usage because the {@link AbstractMenu} reference(s) won't be removed from a {@link
 * MenuManager}'s {@link java.util.Map}. It is okay if the menus will be frequently used, opened and closed and visible
 * to everyone.
 *
 * <p><strong>Warning:</strong> {@link AbstractMenu}s in multiple instances of {@link MenuManager}s might cause
 * multiple event handler calls. It is advised that you use {@link UtilitiesPlugin#getMenuManager(Plugin)} which is
 * accessible and common to all {@link Plugin}s that are using this library.
 *
 * @author Alan B.
 */
public class MenuManager implements Listener {

    /**
     * All menus are stored here at a 1:1 ratio while being viewed, compared to a {@link HumanEntity} key to {@link
     * AbstractMenu} value which can grower much larger (example: 50 players viewing the same menu would cause 50 keys),
     * while in this case it's one {@link Inventory} key for one {@link AbstractMenu} value. <strong>Multiple {@link
     * AbstractMenu}s of a single {@link Inventory} won't work in one {@link MenuManager} because of unique key
     * mappings.</strong>
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
     * @throws IllegalArgumentException If the {@link Plugin} argument is null
     * @throws IllegalStateException    If the {@link Plugin} argument is not enabled
     */
    public MenuManager(@NotNull Plugin provider) {
        Bukkit.getPluginManager().registerEvents(this, this.provider = UtilitiesPlugin.checkProvider(provider));
    }

    /**
     * Removes an {@link AbstractMenu} that matches a given {@link Inventory} from this {@link MenuManager}'s {@link
     * HashMap}.
     *
     * @param inventory Not null {@link Inventory} whose {@link AbstractMenu} will be unregistered
     * @return {@link AbstractMenu} that was a value to the given {@link Inventory}'s {@link AbstractMenu}
     * @throws IllegalArgumentException If the {@link Inventory} argument is null
     */
    public Optional<AbstractMenu> unregisterMenu(@NotNull Inventory inventory) {
        Preconditions.checkArgument(inventory != null, "Inventory argument can't be null");
        return Optional.ofNullable(menus.remove(inventory));
    }

    /**
     * Removes a given {@link AbstractMenu} from this {@link MenuManager}'s {@link HashMap}.
     *
     * @param menu Not null {@link AbstractMenu} that'll be unregistered
     * @return {@link AbstractMenu} that was a value to the given {@link AbstractMenu}'s {@link Inventory}
     * @throws IllegalArgumentException If the {@link AbstractMenu} argument is null
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
     * @throws IllegalArgumentException If the {@link AbstractMenu} argument is null
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
     * @throws IllegalArgumentException If the {@link Plugin} argument is null
     * @throws IllegalStateException    If the {@link Plugin} argument is not enabled
     */
    public boolean provide(@NotNull Plugin provider) {
        if (isRegistered()) {
            return false;
        }

        Bukkit.getPluginManager().registerEvents(this, this.provider = UtilitiesPlugin.checkProvider(provider));
        return true;
    }

    /**
     * Closes all inventory menus that are currently registered.
     *
     * <p>Closing an {@link AbstractMenu} for a {@link HumanEntity} might not always work because their {@link
     * AbstractMenu#onClose(InventoryCloseEvent)} can choose to open a new {@link AbstractMenu}, possibly the same one.
     *
     * @return This instance, useful for chaining
     */
    public MenuManager closeMenus() {
        new HashMap<>(menus).forEach((inventory, menu) -> menu.close());
        return this;
    }

    /**
     * Returns the {@link AbstractMenu} that matches a given {@link Inventory} argument.
     *
     * @param inventory Inventory that possibly belongs to an {@link AbstractMenu}
     * @return Optional of nullable {@link AbstractMenu} that matches the given inventory
     * @throws IllegalArgumentException If the {@link Inventory} argument is null
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
     * Returns the unmodifiable map ({@link Collections#unmodifiableMap(Map)}) view of this {@link MenuManager}'s menu
     * {@link HashMap} of {@link Inventory} keys to {@link AbstractMenu} values.
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
        AbstractMenu menu = menus.get(event.getInventory());

        if (menu == null) {
            return;
        }

        menu.onClick(event, event.getClickedInventory() == null || !event.getClickedInventory().equals(menu.getInventory()));
    }

    /**
     * Handles any inventory closing event that is related to an inventory menu, and opens next inventory menus.
     *
     * @param event InventoryCloseEvent event
     * @see AbstractMenu#onClose(InventoryCloseEvent)
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(@NotNull InventoryCloseEvent event) {
        AbstractMenu menu = menus.get(event.getInventory());

        if (menu == null) {
            return;
        }

        menu.onClose(event);
    }


    /**
     * Handles any inventory opening events that are related to inventory menus.
     *
     * @param event InventoryOpenEvent event object
     * @see AbstractMenu#onOpen(InventoryOpenEvent)
     */
    @EventHandler
    public void onInventoryOpen(@NotNull InventoryOpenEvent event) {
        AbstractMenu menu = menus.get(event.getInventory());

        if (menu == null) {
            return;
        }

        menu.onOpen(event);
    }

    /**
     * Handles any inventory item stack dragging events that are related to inventory menus.
     *
     * @param event InventoryDragEvent event object
     * @see AbstractMenu#onDrag(InventoryDragEvent)
     */
    @EventHandler
    public void onInventoryDrag(@NotNull InventoryDragEvent event) {
        AbstractMenu menu = menus.get(event.getInventory());

        if (menu == null) {
            return;
        }

        menu.onDrag(event);
    }

    /**
     * Handles any inventory item stack movement events that are related to inventory menus.
     *
     * @param event InventoryMoveItemEvent event
     * @see AbstractMenu#onItemMove(InventoryMoveItemEvent, boolean)
     */
    @EventHandler
    public void onItemMove(@NotNull InventoryMoveItemEvent event) {
        AbstractMenu menu = menus.get(event.getDestination());
        boolean isDestination = false;

        if (menu == null) {
            menu = menus.get(event.getSource());
        } else {
            isDestination = true;
        }

        menu.onItemMove(event, isDestination);
    }

    /**
     * Handles a {@link PluginDisableEvent} of this {@link MenuManager}'s {@link Listener} provider to all inventory
     * menus.
     *
     * @param event PluginDisableEvent event
     * @see AbstractMenu#onDisable(PluginDisableEvent)
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginDisable(@NotNull PluginDisableEvent event) {
        if (!event.getPlugin().equals(provider)) {
            return;
        }

        provider = null;

        new HashMap<>(menus).forEach((inventory, menu) -> {
            try {
                menu.onDisable(event);
            } catch (Exception e) {
                Bukkit.getLogger().warning("An error occurred while handling a menu plugin disable event.");
                e.printStackTrace();
            }
        });
    }
}