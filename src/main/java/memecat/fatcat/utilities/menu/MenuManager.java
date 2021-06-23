package memecat.fatcat.utilities.menu;

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

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;

/**
 * A class that filters {@link org.bukkit.event.inventory.InventoryEvent}s to registered {@link Inventory} {@link
 * AbstractMenu}s.
 *
 * <p><strong>Note:</strong> {@link AbstractMenu}s should be unregistered from their {@link MenuManager} when they're
 * not in use anymore, such as when the last viewer closes an {@link AbstractMenu}. The unfollowing of this rule will
 * cause the increase in memory usage because the {@link AbstractMenu} reference(s) won't be removed from a {@link
 * MenuManager}'s {@link java.util.Map}.
 *
 * <p><strong>Warning:</strong> {@link AbstractMenu}s in multiple instances of {@link MenuManager}s might cause
 * multiple event handler calls. It is advised to use {@link UtilitiesPlugin#getMenuManager(Plugin)} which is accessible
 * and common to all {@link Plugin}s that are using this library.
 *
 * @author Alan B. | FatCat
 */
public class MenuManager implements Listener {

    /**
     * All menus are stored here at a 1:1 ratio ({@link Inventory} for {@link AbstractMenu}) while being viewed,
     * compared to a {@link HumanEntity} key to {@link AbstractMenu} value which can grower much larger (e.g. 50
     * players viewing the same menu would cause 50 keys). <strong>Multiple {@link AbstractMenu}s of a single
     * {@link Inventory} won't work in one {@link MenuManager} because of unique key mappings.</strong>
     * TODO: WeakHashMap? Inventories might be referenced only by their Bukkit viewers.
     */
    private final Map<Inventory, AbstractMenu> menus = new HashMap<>(8);
    private final Queue<Plugin> providers = new ArrayDeque<>();

    /**
     * Creates a new instance that registers itself with the enabled {@link Listener} provider {@link Plugin}.
     *
     * @param newProvider Not null enabled {@link Plugin} for registering this {@link Listener} instance's event
     *                    handlers
     * @throws IllegalArgumentException If the {@link Plugin} argument is null
     * @throws IllegalStateException    If the {@link Plugin} argument is not enabled
     */
    public MenuManager(@NotNull Plugin newProvider) {
        Bukkit.getPluginManager().registerEvents(this, UtilitiesPlugin.checkProvider(newProvider));
        this.providers.add(newProvider);
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
        return Optional.ofNullable(this.menus.remove(inventory));
    }

    /**
     * Removes a given {@link AbstractMenu} from this {@link MenuManager}'s {@link HashMap}.
     *
     * @param menu Not null {@link AbstractMenu} that'll be unregistered
     * @return {@link AbstractMenu} that was a value to the given {@link AbstractMenu}'s {@link Inventory}
     * @throws IllegalArgumentException If the {@link AbstractMenu} argument is null
     */
    public Optional<AbstractMenu> unregisterMenu(@NotNull AbstractMenu menu) {
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
    public Optional<AbstractMenu> registerMenu(@NotNull AbstractMenu menu) {
        return Optional.ofNullable(this.menus.put(menu.getInventory(), menu));
    }

    /**
     * Provides a new {@link Plugin} to register this {@link Listener}'s events if it's current provider is null or
     * disabled.
     *
     * @param newProvider Not null enabled {@link Plugin} for registering this {@link Listener} instance's event handlers
     *                    immediately or later
     * @return This instance, useful for chaining
     * @throws IllegalArgumentException If the {@link Plugin} argument is null
     * @throws IllegalStateException    If the {@link Plugin} argument is not enabled
     */
    public MenuManager provide(@NotNull Plugin newProvider) {
        if (!this.isRegistered()) {
            Bukkit.getPluginManager().registerEvents(this, UtilitiesPlugin.checkProvider(newProvider));
        }

        if (!this.providers.contains(newProvider)) {
            this.providers.add(newProvider);
        }

        return this;
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
        //noinspection ForLoopReplaceableByForEach
        for (Iterator<Map.Entry<Inventory, AbstractMenu>> iterator = this.menus.entrySet().iterator(); iterator.hasNext(); ) {
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
    @NotNull
    public Optional<AbstractMenu> getMenu(@NotNull Inventory inventory) {
        return Optional.ofNullable(this.menus.get(inventory));
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
     * Returns the {@link Optional} of a nullable {@link Plugin} that registers this {@link Listener}'s event handlers.
     *
     * @return {@link Optional} of a nullable {@link Plugin}
     */
    @NotNull
    public Optional<Plugin> getPlugin() {
        return Optional.ofNullable(this.providers.peek());
    }

    /**
     * Returns whether this listener has it's events registered under a plugin.
     *
     * @return Whether this listener has it's events registered under a plugin
     */
    public boolean isRegistered() {
        return !this.providers.isEmpty(); // Assuming that the disabled provider is always removed...
    }

    /**
     * Handles any inventory ItemStack or slot click event that is related to an inventory menu.
     *
     * @param event InventoryClickEvent event
     * @see AbstractMenu#onClick(InventoryClickEvent, boolean)
     */
    @EventHandler
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        Optional.ofNullable(this.menus.get(event.getInventory()))
                .ifPresent(menu -> menu.onClick(event, !menu.getInventory().equals(event.getClickedInventory())));
    }

    /**
     * Handles any inventory closing event that is related to an inventory menu, and opens next inventory menus.
     *
     * @param event InventoryCloseEvent event
     * @see AbstractMenu#onClose(InventoryCloseEvent)
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(@NotNull InventoryCloseEvent event) {
        Optional.ofNullable(this.menus.get(event.getInventory())).ifPresent(menu -> menu.onClose(event));
    }

    /**
     * Handles any inventory opening events that are related to inventory menus.
     *
     * @param event InventoryOpenEvent event object
     * @see AbstractMenu#onOpen(InventoryOpenEvent)
     */
    @EventHandler
    public void onInventoryOpen(@NotNull InventoryOpenEvent event) {
        Optional.ofNullable(this.menus.get(event.getInventory())).ifPresent(menu -> menu.onOpen(event));
    }

    /**
     * Handles any inventory item stack dragging events that are related to inventory menus.
     *
     * @param event InventoryDragEvent event object
     * @see AbstractMenu#onDrag(InventoryDragEvent)
     */
    @EventHandler
    public void onInventoryDrag(@NotNull InventoryDragEvent event) {
        Optional.ofNullable(this.menus.get(event.getInventory())).ifPresent(menu -> menu.onDrag(event));
    }

    /**
     * Handles any inventory item stack movement events that are related to inventory menus.
     *
     * @param event InventoryMoveItemEvent event
     * @see AbstractMenu#onItemMove(InventoryMoveItemEvent, boolean)
     */
    @EventHandler
    public void onItemMove(@NotNull InventoryMoveItemEvent event) {
        // Optional.ofNullable(MENUS.get(event.getDestination())).ifPresentOrElse(menu -> menu.onItemMove(event, true),
        // () -> Optional.ofNullable(MENUS.get(event.getSource())).ifPresent(menu -> menu.onItemMove(event, false)));
        Optional<AbstractMenu> menu = Optional.ofNullable(this.menus.get(event.getDestination()));

        if (menu.isPresent()) {
            menu.get().onItemMove(event, true);
        } else {
            Optional.ofNullable(this.menus.get(event.getSource())).ifPresent(m -> m.onItemMove(event, false));
        }
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
        Optional<Plugin> currentProvider = this.getPlugin();

        if (this.providers.remove(event.getPlugin())) {
            for (Iterator<Map.Entry<Inventory, AbstractMenu>> iterator = this.menus.entrySet().iterator(); iterator.hasNext(); ) {
                try {
                    iterator.next().getValue().onDisable(event);
                } catch (Exception e) {
                    Bukkit.getLogger().warning("An error occurred while handling a menu plugin disable event");
                    e.printStackTrace();
                }
            }

            currentProvider.flatMap(provider ->
                    Optional.ofNullable(this.providers.peek()))
                    .ifPresent(newProvider -> Bukkit.getPluginManager().registerEvents(this, newProvider));
        }
    }
}