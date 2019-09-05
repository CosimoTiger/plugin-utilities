package memecat.fatcat.utilities.menu.menus;

import com.google.common.base.Preconditions;
import memecat.fatcat.utilities.menu.MenuManager;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;


/**
 * Represents a collection of functional actions and features of an inventory that multiple viewers can see and interact
 * with, containing many different features that can be customised and extended by a developer.
 *
 * <p> A developer can subclass this class, override the methods or add them to customise the ways of processing inputs
 * for inventory menu events or modifying the inventories.
 *
 * @author Alan B.
 * @see InventoryMenu
 * @see PropertyMenu
 */
public abstract class AbstractMenu {

    /**
     * Main, constant part of an {@link AbstractMenu} that identifies it.
     */
    private final Inventory inventory;

    /**
     * A {@link MenuManager} instance that is available for the control of this {@link AbstractMenu}.
     */
    protected MenuManager menuManager;

    /**
     * The default constructor for all subclasses.
     *
     * @param inventory   Not null {@link Inventory} that will be wrapped and controlled by an {@link AbstractMenu}
     * @param menuManager Not null {@link MenuManager} that will be used for (un)registering this {@link AbstractMenu}
     *                    and passing events to it
     * @throws IllegalArgumentException If the {@link Inventory} or {@link MenuManager} argument is null
     */
    public AbstractMenu(@NotNull Inventory inventory, @NotNull MenuManager menuManager) {
        Preconditions.checkArgument(inventory != null, "Inventory argument can't be null");
        Preconditions.checkArgument(menuManager != null, "MenuManager argument can't be null");

        this.inventory = inventory;
        this.menuManager = menuManager;
    }

    /**
     * Acts as an event handler for an ItemStack movement from source to destination inventory.
     *
     * <p> This event handler is most likely called in rare cases; when this {@link AbstractMenu} inventory belongs to
     * a container object and another object or inventory tries to move items to it. An example of this happening can be
     * a chest block container inventory with a hopper connecting to it that is trying to move items into it.
     *
     * @param event         {@link InventoryMoveItemEvent} event
     * @param isDestination Whether this {@link org.bukkit.inventory.Inventory} is equal to the {@link
     *                      InventoryMoveItemEvent#getDestination()}
     */
    public void onItemMove(@NotNull InventoryMoveItemEvent event, boolean isDestination) {
        event.setCancelled(true);
    }

    /**
     * Handles any {@link InventoryClickEvent} related to this inventory menu.
     *
     * <p> By default, the {@link InventoryAction} {@code COLLECT_TO_CURSOR} and {@code MOVE_TO_OTHER_INVENTORY} are
     * cancelled and any menu action is cancelled from interaction.
     *
     * @param event    {@link InventoryClickEvent} event
     * @param external Whether the clicked inventory is not this menu, possibly not any
     * @see memecat.fatcat.utilities.menu.slot.AbstractSlotProperty
     * @see memecat.fatcat.utilities.menu.slot.SlotProperty
     */
    public void onClick(@NotNull InventoryClickEvent event, boolean external) {
        switch (event.getAction()) {
            case COLLECT_TO_CURSOR:
            case MOVE_TO_OTHER_INVENTORY:
                event.setCancelled(true);
                break;
        }

        if (external) {
            return;
        }

        event.setCancelled(true);
    }

    /**
     * Handles the {@link PluginDisableEvent} of {@link MenuManager}'s {@link MenuManager#getPlugin}.
     *
     * @param event {@link PluginDisableEvent} event
     */
    public void onDisable(@NotNull PluginDisableEvent event) {
    }

    /**
     * Handles the inventory close events of this menu and opens a next menu after this one.
     * <p>
     * To open a new {@link AbstractMenu} for the given {@link InventoryCloseEvent} of a {@link HumanEntity}, perform
     * that task on the next tick. For an example: {@code Bukkit.getScheduler().runTask(plugin, () ->
     * open(event.getPlayer()))}
     *
     * @param event {@link InventoryCloseEvent} event
     */
    public void onClose(@NotNull InventoryCloseEvent event) {
    }

    /**
     * Acts as an event handler for the inventory opening event.
     *
     * @param event {@link InventoryOpenEvent} event
     */
    public void onOpen(@NotNull InventoryOpenEvent event) {
    }

    /**
     * Acts as an event handler for the inventory item dragging event.
     *
     * <p> By default, any item dragging in the menu will be cancelled, while outside of it (bottom inventory) won't.
     *
     * @param event {@link InventoryDragEvent} event
     */
    public void onDrag(@NotNull InventoryDragEvent event) {
        int topEndSlot = event.getView().getTopInventory().getSize() - 1;

        for (int slot : event.getRawSlots()) {
            if (slot <= topEndSlot) {
                event.setCancelled(true);
                return;
            }
        }
    }

    /**
     * Sets an {@link ItemStack} at this {@link AbstractMenu}'s given slot(s).
     *
     * @param item  {@link ItemStack} to set at given slots
     * @param slots Slots that the {@link ItemStack} will be placed at
     * @return This instance, useful for chaining
     * @throws IndexOutOfBoundsException If a slot in the slot array argument is out of this inventory's boundaries
     * @throws IllegalArgumentException  If the slot array argument is null
     */
    @NotNull
    public AbstractMenu set(@Nullable ItemStack item, @NotNull int... slots) {
        Preconditions.checkArgument(slots != null, "Array of slots can't be null");

        for (int slot : slots) {
            InventoryMenu.checkElement(slot, getSize());
            getInventory().setItem(slot, item);
        }

        return this;
    }

    /**
     * Opens this {@link AbstractMenu} for the given viewers, fail-fast.
     *
     * <p> If any {@link HumanEntity} in the viewers argument is null, a {@link NullPointerException} is thrown - this
     * is to prevent this {@link AbstractMenu} from being registered unnecessarily to it's {@link MenuManager}.
     *
     * @param viewers {@link Collection}&lt;{@link HumanEntity}&gt; of which each will see this {@link AbstractMenu}
     *                {@link Inventory}
     * @return This instance, useful for chaining
     * @throws IllegalArgumentException If the {@link Collection}&lt;{@link HumanEntity}&gt; argument is null
     * @throws IllegalStateException    If this {@link AbstractMenu}'s {@link MenuManager} isn't registered for handling
     *                                  events
     * @throws NullPointerException     If a {@link HumanEntity} is null
     */
    public AbstractMenu open(@NotNull Collection<HumanEntity> viewers) {
        Preconditions.checkArgument(!viewers.isEmpty(), "Collection<HumanEntity> viewers argument can't be null");
        Preconditions.checkState(menuManager.isRegistered(),
                "MenuManager has no plugin registered to handle inventory menu events");
        viewers.forEach(viewer -> Objects.requireNonNull(viewer, "HumanEntity viewer in the Collection of viewers can't be null"));

        menuManager.registerMenu(this);

        viewers.forEach(viewer -> viewer.openInventory(getInventory()));

        return this;
    }

    /**
     * Unregisters this {@link AbstractMenu} from it's previous {@link MenuManager}, sets this one and registers itself
     * to the new, given {@link MenuManager}.
     *
     * @param menuManager Not null {@link MenuManager} that this {@link AbstractMenu} will switch over to
     * @return Previous {@link MenuManager} of this {@link AbstractMenu}
     * @throws IllegalArgumentException If the {@link MenuManager} argument is null
     */
    @NotNull
    public AbstractMenu menuManager(@NotNull MenuManager menuManager) {
        Preconditions.checkArgument(menuManager != null, "MenuManager argument can't be null");

        this.menuManager.unregisterMenu(this);
        this.menuManager = menuManager;
        menuManager.registerMenu(this);

        return this;
    }

    /**
     * Opens this {@link AbstractMenu} for the given viewers, fail-fast.
     *
     * <p> If any {@link HumanEntity} in the viewers argument is null, a {@link NullPointerException} is thrown - this
     * is to prevent this {@link AbstractMenu} from being registered unnecessarily to it's {@link MenuManager}.
     *
     * @param viewers Array of {@link HumanEntity} of which each will see this {@link AbstractMenu} {@link Inventory}
     * @return This instance, useful for chaining
     * @throws IllegalArgumentException If the {@link HumanEntity} array argument is null
     * @throws IllegalStateException    If this {@link AbstractMenu}'s {@link MenuManager} isn't registered for handling
     *                                  events
     * @throws NullPointerException     If a {@link HumanEntity} is null
     */
    public AbstractMenu open(@NotNull HumanEntity... viewers) {
        return open(Arrays.asList(viewers));
    }

    /**
     * Closes all {@link AbstractMenu}s of this instance for all viewers who are viewing it.
     *
     * <p> Closing an {@link AbstractMenu} for a {@link HumanEntity} might not always work because their {@link
     * #onClose(InventoryCloseEvent)} can choose to open a new {@link AbstractMenu}, possibly the same one.
     *
     * @return This instance, useful for chaining
     */
    @NotNull
    public final AbstractMenu close() {
        new ArrayList<>(getInventory().getViewers()).forEach(HumanEntity::closeInventory);
        return this;
    }

    /**
     * Clears all of this inventory menu's contents.
     *
     * @return This instance, useful for chaining
     */
    @NotNull
    public AbstractMenu clear() {
        getInventory().clear();
        return this;
    }

    /**
     * Returns an ItemStack at the given slot of this inventory menu or null if it doesn't exist.
     *
     * @param slot Slot index location of the item in the inventory
     * @return {@link Optional} of a nullable {@link ItemStack}
     * @throws IndexOutOfBoundsException If the given slot argument is out of the inventory's array bounds
     */
    @NotNull
    public Optional<ItemStack> getItem(int slot) {
        InventoryMenu.checkElement(slot, getSize());
        return Optional.ofNullable(getInventory().getItem(slot));
    }

    /**
     * Returns the {@link Inventory} that's wrapped and controlled by this {@link AbstractMenu}.
     *
     * @return Not null, always the same {@link Inventory}
     */
    @NotNull
    public final Inventory getInventory() {
        return inventory;
    }

    /**
     * Returns the {@link MenuManager} that is used by this {@link AbstractMenu} to (un)register itself, listen to
     * events and access other {@link AbstractMenu}s.
     *
     * @return Not null {@link MenuManager}
     */
    @NotNull
    public MenuManager getMenuManager() {
        return menuManager;
    }

    /**
     * Returns the slot amount that this {@link AbstractMenu} has.
     *
     * @return Amount of slots of this inventory {@link AbstractMenu}
     */
    public final int getSize() {
        return getInventory().getSize();
    }
}