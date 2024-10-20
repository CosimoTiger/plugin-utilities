package com.cosimo.utilities.menu;

import com.google.common.base.Preconditions;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

/**
 * A {@link Listener} that filters and dispatches {@link org.bukkit.event.inventory.InventoryEvent}s to registered
 * {@link Inventory} {@link IMenu}s. Menu inventory listening is delegated and centralised in one {@link MenuManager}
 * because of the assumption that constantly registering and unregistering new {@link Listener}s for each {@link IMenu}
 * is slow.
 *
 * <p><strong>Note:</strong> {@link IMenu}s should be unregistered from their {@link MenuManager} when they're
 * not in use anymore, such as when the last viewer closes an {@link IMenu}. The unfollowing of this rule will cause a
 * memory leak because the {@link IMenu} reference(s) won't be removed from a {@link MenuManager}'s {@link Map}.
 *
 * <p><strong>Warning:</strong> {@link IMenu}s in multiple {@link MenuManager} instances may cause
 * duplicate event handler calls.
 *
 * @author CosimoTiger
 */
public class MenuManager implements Listener {

    @Nullable
    @Setter
    private static MenuManager instance = null;

    @NonNull
    public static MenuManager getInstance() {
        Preconditions.checkState(instance != null, "MenuManager singleton instance is null");
        return instance;
    }

    /**
     * Stores {@link IMenu} associations to their {@link Inventory} instances for quick access.
     */
    private final Map<Inventory, IMenu> menus;
    private final Plugin provider;

    /**
     * Creates a new instance that registers itself with the enabled {@link Listener} provider {@link Plugin}.
     *
     * @param provider Not null enabled {@link Plugin} for registering this {@link Listener} instance's event handlers
     * @param mapImpl  Customizable {@link Map} implementation
     * @throws IllegalArgumentException If the {@link Plugin} argument is null
     * @throws IllegalStateException    If the {@link Plugin} argument is not enabled
     */
    protected MenuManager(@NonNull Plugin provider, @NonNull Map<Inventory, IMenu> mapImpl) {
        Preconditions.checkState(provider.isEnabled(), "Plugin provider argument can't be disabled");
        Bukkit.getPluginManager().registerEvents(this, this.provider = provider);

        this.menus = mapImpl;

        if (instance == null) {
            instance = this;
        }
    }

    /**
     * Creates a new instance that registers itself with the enabled {@link Listener} provider {@link Plugin}.
     *
     * @param provider Not null enabled {@link Plugin} for registering this {@link Listener} instance's event handlers
     * @throws IllegalArgumentException If the {@link Plugin} argument is null
     * @throws IllegalStateException    If the {@link Plugin} argument is not enabled
     */
    public MenuManager(@NonNull Plugin provider) {
        this(provider, new HashMap<>(4));
    }

    /**
     * Removes an {@link IMenu} that matches a given {@link Inventory} from this {@link MenuManager}'s {@link HashMap}.
     *
     * @param inventory Not null {@link Inventory} whose {@link IMenu} will be unregistered
     * @return {@link IMenu} that was a value to the given {@link Inventory}'s {@link IMenu}
     * @throws IllegalArgumentException If the {@link Inventory} argument is null
     */
    @NonNull
    public Optional<IMenu> unregisterMenu(@NonNull Inventory inventory) {
        return Optional.ofNullable(this.menus.remove(inventory));
    }

    /**
     * Removes a given {@link IMenu} from this {@link MenuManager}'s {@link HashMap}.
     *
     * @param menu Not null {@link IMenu} that'll be unregistered
     * @return {@link IMenu} that was a value to the given {@link IMenu}'s {@link Inventory}
     * @throws IllegalArgumentException If the {@link IMenu} argument is null
     */
    @NonNull
    public Optional<IMenu> unregisterMenu(@NonNull IMenu menu) {
        return Optional.ofNullable(this.menus.remove(menu.getInventory()));
    }

    /**
     * Puts a given {@link IMenu} into this {@link MenuManager}'s {@link HashMap}, causing any next events to be handled
     * to the {@link IMenu} until it's unregistered.
     *
     * @param menu Not null {@link IMenu} that'll be registered
     * @return Previous {@link IMenu} that was a value to the given {@link IMenu}'s {@link Inventory}
     * @throws IllegalArgumentException If the {@link IMenu} argument is null
     */
    @NonNull
    public Optional<IMenu> registerMenu(@NonNull IMenu menu) {
        return Optional.ofNullable(this.menus.put(menu.getInventory(), menu));
    }

    /**
     * Closes all inventory menus that are currently registered.
     *
     * <p>Closing an {@link IMenu} for a {@link HumanEntity} might not always work because their {@link
     * IMenu#onClose(InventoryCloseEvent)} can choose to open a new {@link IMenu}, possibly the same one.
     *
     * @return This instance, useful for chaining
     */
    @NonNull
    public MenuManager closeMenus() {
        /*
         * Closing each menu calls an InventoryCloseEvent which may unregister a menu from the MenuManager Map of menus.
         * Therefore, a ConcurrentModificationException needs to be avoided using an Iterator.
         */
        // noinspection ForLoopReplaceableByForEach
        for (final var iterator = this.menus.entrySet().iterator(); iterator.hasNext(); ) {
            iterator.next().getValue().close();
        }

        return this;
    }

    /**
     * Returns the {@link IMenu} that matches a given {@link Inventory} argument.
     *
     * @param inventory Inventory that possibly belongs to an {@link IMenu}
     * @return Optional of nullable {@link IMenu} that matches the given inventory
     * @throws IllegalArgumentException If the {@link Inventory} argument is null
     */
    @NonNull
    public Optional<IMenu> getMenu(@NonNull Inventory inventory) {
        return Optional.ofNullable(this.menus.get(inventory));
    }

    /**
     * Returns the current inventory {@link IMenu} that a given player is viewing, assuming it's the top inventory of
     * their {@link org.bukkit.inventory.InventoryView}.
     *
     * @param viewer {@link HumanEntity} that's viewing an inventory
     * @return Optional of nullable {@link IMenu} of the given viewer's top inventory
     * @throws IllegalArgumentException If the viewer argument is null
     */
    @NonNull
    public Optional<IMenu> getMenu(@NonNull HumanEntity viewer) {
        return this.getMenu(viewer.getOpenInventory().getTopInventory());
    }

    /**
     * Returns the ({@link Collections#unmodifiableMap(Map)}) view of this {@link MenuManager}'s menu {@link HashMap} of
     * {@link Inventory} keys to {@link IMenu} values.
     *
     * @return Unmodifiable view of this {@link MenuManager}'s {@link HashMap}
     */
    @NonNull
    @UnmodifiableView
    public Map<Inventory, IMenu> getMap() {
        return Collections.unmodifiableMap(this.menus);
    }

    /**
     * Returns the {@link Plugin} that registers this {@link Listener}'s event handlers.
     *
     * @return {@link Plugin} of this {@link MenuManager}
     */
    @NonNull
    public Plugin getPlugin() {
        return this.provider;
    }

    /**
     * Passes any {@link InventoryClickEvent} to the {@link IMenu} it happened on.
     *
     * @param event InventoryClickEvent event
     * @see IMenu#onClick(InventoryClickEvent)
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(@NonNull InventoryClickEvent event) {
        this.getMenu(event.getInventory()).ifPresent(menu -> menu.onClick(event));
    }

    /**
     * Passes the {@link InventoryCloseEvent} to the {@link IMenu} it happened on.
     *
     * @param event InventoryCloseEvent event
     * @see IMenu#onClose(InventoryCloseEvent)
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(@NonNull InventoryCloseEvent event) {
        this.getMenu(event.getInventory()).ifPresent(menu -> {
            if (MenuUtils.isAboutToBecomeDisposable(event)) {
                this.unregisterMenu(menu);
            }

            menu.onClose(event);
        });
    }

    /**
     * Passes any {@link InventoryOpenEvent} to the {@link IMenu} it happened on.
     *
     * @param event InventoryOpenEvent event object
     * @see IMenu#onOpen(InventoryOpenEvent)
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryOpen(@NonNull InventoryOpenEvent event) {
        this.getMenu(event.getInventory()).ifPresent(menu -> menu.onOpen(event));
    }

    /**
     * Passes any {@link InventoryDragEvent} to the {@link IMenu} it happened on.
     *
     * @param event InventoryDragEvent event object
     * @see IMenu#onDrag(InventoryDragEvent)
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryDrag(@NonNull InventoryDragEvent event) {
        this.getMenu(event.getInventory()).ifPresent(menu -> menu.onDrag(event));
    }

    /**
     * Passes a {@link PluginDisableEvent} of this {@link MenuManager}'s provider ({@link #getPlugin()}) to all
     * inventory menus.
     *
     * @param event PluginDisableEvent event
     * @see IMenu#onDisable(PluginDisableEvent)
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginDisable(@NonNull PluginDisableEvent event) {
        if (this.getPlugin().equals(event.getPlugin())) {
            for (final var iterator = this.menus.entrySet().iterator(); iterator.hasNext(); ) {
                try {
                    iterator.next().getValue().onDisable(event);
                } catch (Exception exception) {
                    Bukkit.getLogger()
                            .log(Level.WARNING, "An error occurred while handling a menu plugin disable event:",
                                 exception);
                }
            }
        }
    }
}