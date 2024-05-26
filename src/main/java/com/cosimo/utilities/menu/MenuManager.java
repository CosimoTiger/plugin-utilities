package com.cosimo.utilities.menu;

import com.google.common.base.Preconditions;
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

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A {@link Listener} that filters {@link org.bukkit.event.inventory.InventoryEvent}s to registered {@link Inventory}
 * {@link AbstractMenu}s.
 *
 * <p><strong>Note:</strong> {@link AbstractMenu}s should be unregistered from their {@link MenuManager} when they're
 * not in use anymore, such as when the last viewer closes an {@link AbstractMenu}. The unfollowing of this rule will
 * cause the increase in memory usage because the {@link AbstractMenu} reference(s) won't be removed from a
 * {@link MenuManager}'s {@link java.util.Map}.
 *
 * <p><strong>Warning:</strong> {@link AbstractMenu}s in multiple instances of {@link MenuManager}s might cause
 * multiple event handler calls.
 *
 * @author CosimoTiger
 */
public class MenuManager implements Listener {

    // TODO: WeakHashMap? Inventories might be referenced only by their Bukkit viewers.
    /**
     * <strong>Multiple {@link AbstractMenu}s of a single {@link Inventory} won't work in one {@link MenuManager}
     * due to unique keys.</strong>
     */
    private final Map<Inventory, AbstractMenu> menus = new HashMap<>(4);
    private final Plugin provider;

    /**
     * Creates a new instance that registers itself with the enabled {@link Listener} provider {@link Plugin}.
     *
     * @param provider Not null enabled {@link Plugin} for registering this {@link Listener} instance's event handlers
     * @throws IllegalArgumentException If the {@link Plugin} argument is null
     * @throws IllegalStateException    If the {@link Plugin} argument is not enabled
     */
    public MenuManager(@Nonnull Plugin provider) {
        Preconditions.checkArgument(provider != null, "Plugin provider argument can't be null");
        Preconditions.checkState(provider.isEnabled(), "Plugin provider argument can't be disabled");

        Bukkit.getPluginManager().registerEvents(this, this.provider = provider);
    }

    /**
     * Removes an {@link AbstractMenu} that matches a given {@link Inventory} from this {@link MenuManager}'s
     * {@link HashMap}.
     *
     * @param inventory Not null {@link Inventory} whose {@link AbstractMenu} will be unregistered
     * @return {@link AbstractMenu} that was a value to the given {@link Inventory}'s {@link AbstractMenu}
     * @throws IllegalArgumentException If the {@link Inventory} argument is null
     */
    @Nonnull
    public Optional<AbstractMenu> unregisterMenu(@Nonnull Inventory inventory) {
        return Optional.ofNullable(this.menus.remove(inventory));
    }

    /**
     * Removes a given {@link AbstractMenu} from this {@link MenuManager}'s {@link HashMap}.
     *
     * @param menu Not null {@link AbstractMenu} that'll be unregistered
     * @return {@link AbstractMenu} that was a value to the given {@link AbstractMenu}'s {@link Inventory}
     * @throws IllegalArgumentException If the {@link AbstractMenu} argument is null
     */
    @Nonnull
    public Optional<AbstractMenu> unregisterMenu(@Nonnull AbstractMenu menu) {
        return Optional.ofNullable(this.menus.remove(menu.getInventory()));
    }

    /**
     * Puts a given {@link AbstractMenu} into this {@link MenuManager}'s {@link HashMap}, causing any next events to be
     * handled to the {@link AbstractMenu} until it's unregistered.
     *
     * @param menu Not null {@link AbstractMenu} that'll be registered
     * @return Previous {@link AbstractMenu} that was a value to the given {@link AbstractMenu}'s {@link Inventory}
     * @throws IllegalArgumentException If the {@link AbstractMenu} argument is null
     */
    @Nonnull
    public Optional<AbstractMenu> registerMenu(@Nonnull AbstractMenu menu) {
        return Optional.ofNullable(this.menus.put(menu.getInventory(), menu));
    }

    /**
     * Closes all inventory menus that are currently registered.
     *
     * <p>Closing an {@link AbstractMenu} for a {@link HumanEntity} might not always work because their {@link
     * AbstractMenu#onClose(InventoryCloseEvent)} can choose to open a new {@link AbstractMenu}, possibly the same one.
     *
     * @return This instance, useful for chaining
     */
    @Nonnull
    public MenuManager closeMenus() {
        // Closing each menu calls an InventoryCloseEvent which may unregister a menu from the MenuManager Map of menus.
        // Therefore, a ConcurrentModificationException needs to be avoided using an Iterator.
        
        //noinspection ForLoopReplaceableByForEach
        for (var iterator = this.menus.entrySet().iterator(); iterator.hasNext(); ) {
            iterator.next().getValue().close();
        }

        return this;
    }

    /**
     * Returns the {@link AbstractMenu} that matches a given {@link Inventory} argument.
     *
     * @param inventory Inventory that possibly belongs to an {@link AbstractMenu}
     * @return Optional of nullable {@link AbstractMenu} that matches the given inventory
     * @throws IllegalArgumentException If the {@link Inventory} argument is null
     */
    @Nonnull
    public Optional<AbstractMenu> getMenu(@Nonnull Inventory inventory) {
        return Optional.ofNullable(this.menus.get(inventory));
    }

    /**
     * Returns the current inventory {@link AbstractMenu} that a given player is viewing, assuming it's the top
     * inventory of their {@link org.bukkit.inventory.InventoryView}.
     *
     * @param viewer {@link HumanEntity} that's viewing an inventory
     * @return Optional of nullable {@link AbstractMenu} of the given viewer's top inventory
     * @throws IllegalArgumentException If the viewer argument is null
     */
    @Nonnull
    public Optional<AbstractMenu> getMenu(@Nonnull HumanEntity viewer) {
        return this.getMenu(viewer.getOpenInventory().getTopInventory());
    }

    /**
     * Returns the unmodifiable map ({@link Collections#unmodifiableMap(Map)}) view of this {@link MenuManager}'s menu
     * {@link HashMap} of {@link Inventory} keys to {@link AbstractMenu} values.
     *
     * @return Unmodifiable view of this {@link MenuManager}'s {@link HashMap}
     */
    public Map<Inventory, AbstractMenu> getMap() {
        return Collections.unmodifiableMap(this.menus);
    }

    /**
     * Returns the {@link Plugin} that registers this {@link Listener}'s event handlers.
     *
     * @return {@link Plugin} of this {@link MenuManager}
     */
    @Nonnull
    public Plugin getPlugin() {
        return this.provider;
    }

    /**
     * Handles any inventory ItemStack or slot click event that is related to an inventory menu.
     *
     * @param event InventoryClickEvent event
     * @see AbstractMenu#onClick(InventoryClickEvent, boolean)
     */
    @EventHandler
    public void onInventoryClick(@Nonnull InventoryClickEvent event) {
        this.getMenu(event.getInventory())
                .ifPresent(menu -> menu.onClick(event, !menu.getInventory().equals(event.getClickedInventory())));
    }

    /**
     * Handles any inventory closing event that is related to an inventory menu, and opens next inventory menus.
     *
     * @param event InventoryCloseEvent event
     * @see AbstractMenu#onClose(InventoryCloseEvent)
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(@Nonnull InventoryCloseEvent event) {
        this.getMenu(event.getInventory()).ifPresent(menu -> {
            if (menu.getInventory().getViewers().size() < 2) {
                this.unregisterMenu(menu);
            }

            menu.onClose(event);
        });
    }

    /**
     * Handles any inventory opening events that are related to inventory menus.
     *
     * @param event InventoryOpenEvent event object
     * @see AbstractMenu#onOpen(InventoryOpenEvent)
     */
    @EventHandler
    public void onInventoryOpen(@Nonnull InventoryOpenEvent event) {
        this.getMenu(event.getInventory()).ifPresent(menu -> menu.onOpen(event));
    }

    /**
     * Handles any inventory item stack dragging events that are related to inventory menus.
     *
     * @param event InventoryDragEvent event object
     * @see AbstractMenu#onDrag(InventoryDragEvent)
     */
    @EventHandler
    public void onInventoryDrag(@Nonnull InventoryDragEvent event) {
        this.getMenu(event.getInventory()).ifPresent(menu -> menu.onDrag(event));
    }

    /**
     * Handles any inventory item stack movement events that are related to inventory menus.
     *
     * @param event InventoryMoveItemEvent event
     * @see AbstractMenu#onItemMove(InventoryMoveItemEvent, boolean)
     */
    @EventHandler
    public void onItemMove(@Nonnull InventoryMoveItemEvent event) {
        this.getMenu(event.getDestination()).ifPresentOrElse(menu -> menu.onItemMove(event, true),
                () -> this.getMenu(event.getSource()).ifPresent(menu -> menu.onItemMove(event, false)));
    }

    // TODO: Test whether this is called at all, since the listener is being
    //  unregistered for the plugin that's being disabled

    /**
     * Handles a {@link PluginDisableEvent} of this {@link MenuManager}'s {@link Listener} provider to all inventory
     * menus.
     *
     * @param event PluginDisableEvent event
     * @see AbstractMenu#onDisable(PluginDisableEvent)
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginDisable(@Nonnull PluginDisableEvent event) {
        if (this.getPlugin().equals(event.getPlugin())) {
            for (var iterator = this.menus.entrySet().iterator(); iterator.hasNext(); ) {
                try {
                    iterator.next().getValue().onDisable(event);
                } catch (Exception e) {
                    // TODO: See how logging is properly done
                    Bukkit.getLogger().warning("An error occurred while handling a menu plugin disable event:");
                    e.printStackTrace();
                }
            }
        }
    }
}