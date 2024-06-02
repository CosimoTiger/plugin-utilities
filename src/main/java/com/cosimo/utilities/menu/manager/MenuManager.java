package com.cosimo.utilities.menu.manager;

import com.cosimo.utilities.menu.IMenu;
import com.google.common.base.Preconditions;
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

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A {@link Listener} that filters {@link org.bukkit.event.inventory.InventoryEvent}s to registered {@link Inventory}
 * {@link IMenu}s. Menu inventory listening is delegated and centralised in one {@link MenuManager} because of the
 * assumption that constantly registering and unregistering new {@link Listener}s for each {@link IMenu} is slow,
 * especially because of the use of Reflection.
 *
 * <p><strong>Note:</strong> {@link IMenu}s should be unregistered from their {@link MenuManager} when they're
 * not in use anymore, such as when the last viewer closes an {@link IMenu}. The unfollowing of this rule will cause a
 * memory leak because the {@link IMenu} reference(s) won't be removed from a {@link MenuManager}'s {@link Map}.
 *
 * <p><strong>Warning:</strong> {@link IMenu}s in multiple {@link MenuManager} instances may cause
 * duplicate event handler calls.
 *
 * @param <E> {@link IMenu} subclass restriction that's always expected to be received or added to this
 *            {@link MenuManager}
 * @author CosimoTiger
 */
public class MenuManager<E extends IMenu> implements Listener {

    // TODO: WeakHashMap? Inventories might be referenced only by their Bukkit viewers.
    //  WeakHashMap<Inventory, WeakReference<IMenu>> = new WeakHashMap<>(4);
    /**
     * Stores {@link IMenu} associations to their {@link Inventory} instances for quick access.
     */
    private final Map<Inventory, E> menus;
    private final Plugin provider;

    /**
     * Creates a new instance that registers itself with the enabled {@link Listener} provider {@link Plugin}.
     *
     * @param provider Not null enabled {@link Plugin} for registering this {@link Listener} instance's event handlers
     * @param mapImpl  Customizable {@link Map} implementation
     * @throws IllegalArgumentException If the {@link Plugin} argument is null
     * @throws IllegalStateException    If the {@link Plugin} argument is not enabled
     */
    protected MenuManager(@Nonnull Plugin provider, @Nonnull Map<Inventory, E> mapImpl) {
        Preconditions.checkArgument(provider != null, "Plugin provider argument can't be null");
        Preconditions.checkState(provider.isEnabled(), "Plugin provider argument can't be disabled");

        Bukkit.getPluginManager().registerEvents(this, this.provider = provider);
        this.menus = mapImpl;
    }

    /**
     * Creates a new instance that registers itself with the enabled {@link Listener} provider {@link Plugin}.
     *
     * @param provider Not null enabled {@link Plugin} for registering this {@link Listener} instance's event handlers
     * @throws IllegalArgumentException If the {@link Plugin} argument is null
     * @throws IllegalStateException    If the {@link Plugin} argument is not enabled
     */
    public MenuManager(@Nonnull Plugin provider) {
        this(provider, new HashMap<>(4));
    }

    /**
     * Removes an {@link IMenu} that matches a given {@link Inventory} from this {@link MenuManager}'s {@link HashMap}.
     *
     * @param inventory Not null {@link Inventory} whose {@link IMenu} will be unregistered
     * @return {@link IMenu} that was a value to the given {@link Inventory}'s {@link IMenu}
     * @throws IllegalArgumentException If the {@link Inventory} argument is null
     */
    @Nonnull
    public Optional<E> unregisterMenu(@Nonnull Inventory inventory) {
        return Optional.ofNullable(this.menus.remove(inventory));
    }

    /**
     * Removes a given {@link IMenu} from this {@link MenuManager}'s {@link HashMap}.
     *
     * @param menu Not null {@link IMenu} that'll be unregistered
     * @return {@link IMenu} that was a value to the given {@link IMenu}'s {@link Inventory}
     * @throws IllegalArgumentException If the {@link IMenu} argument is null
     */
    @Nonnull
    public Optional<E> unregisterMenu(@Nonnull E menu) {
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
    @Nonnull
    public Optional<E> registerMenu(@Nonnull E menu) {
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
    @Nonnull
    public MenuManager<E> closeMenus() {
        // Closing each menu calls an InventoryCloseEvent which may unregister a menu from the MenuManager Map of menus.
        // Therefore, a ConcurrentModificationException needs to be avoided using an Iterator.
        //noinspection ForLoopReplaceableByForEach
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
    @Nonnull
    public Optional<E> getMenu(@Nonnull Inventory inventory) {
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
    @Nonnull
    public Optional<E> getMenu(@Nonnull HumanEntity viewer) {
        return this.getMenu(viewer.getOpenInventory().getTopInventory());
    }

    /**
     * Returns the unmodifiable map ({@link Collections#unmodifiableMap(Map)}) view of this {@link MenuManager}'s menu
     * {@link HashMap} of {@link Inventory} keys to {@link IMenu} values.
     *
     * @return Unmodifiable view of this {@link MenuManager}'s {@link HashMap}
     */
    public Map<Inventory, E> getMap() {
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
     * Passes any {@link InventoryClickEvent} to the {@link IMenu} it happened on.
     *
     * @param event InventoryClickEvent event
     * @see IMenu#onClick(InventoryClickEvent, boolean)
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(@Nonnull InventoryClickEvent event) {
        this.getMenu(event.getInventory())
                .ifPresent(menu -> menu.onClick(event, !menu.getInventory().equals(event.getClickedInventory())));
    }

    /**
     * Passes the {@link InventoryCloseEvent} to the {@link IMenu} it happened on.
     *
     * @param event InventoryCloseEvent event
     * @see IMenu#onClose(InventoryCloseEvent)
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
     * Passes any {@link InventoryOpenEvent} to the {@link IMenu} it happened on.
     *
     * @param event InventoryOpenEvent event object
     * @see IMenu#onOpen(InventoryOpenEvent)
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryOpen(@Nonnull InventoryOpenEvent event) {
        this.getMenu(event.getInventory()).ifPresent(menu -> menu.onOpen(event));
    }

    /**
     * Passes any {@link InventoryDragEvent} to the {@link IMenu} it happened on.
     *
     * @param event InventoryDragEvent event object
     * @see IMenu#onDrag(InventoryDragEvent)
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryDrag(@Nonnull InventoryDragEvent event) {
        this.getMenu(event.getInventory()).ifPresent(menu -> menu.onDrag(event));
    }

    // TODO: Test whether this is called at all, since the listener is being
    //  unregistered for the plugin that's being disabled

    /**
     * Passes a {@link PluginDisableEvent} of this {@link MenuManager}'s provider ({@link #getPlugin()}) to all
     * inventory menus.
     *
     * @param event PluginDisableEvent event
     * @see IMenu#onDisable(PluginDisableEvent)
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginDisable(@Nonnull PluginDisableEvent event) {
        if (this.getPlugin().equals(event.getPlugin())) {
            for (final var iterator = this.menus.entrySet().iterator(); iterator.hasNext(); ) {
                try {
                    iterator.next().getValue().onDisable(event);
                } catch (Exception e) {
                    // TODO: see how to properly log
                    Bukkit.getLogger().warning("An error occurred while handling a menu plugin disable event:");
                    e.printStackTrace();
                }
            }
        }
    }
}