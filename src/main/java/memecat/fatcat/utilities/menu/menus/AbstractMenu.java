package memecat.fatcat.utilities.menu.menus;

import com.google.common.base.Preconditions;
import memecat.fatcat.utilities.UtilitiesPlugin;
import memecat.fatcat.utilities.menu.MenuManager;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * Represents a collection of functional actions and features of an inventory that multiple viewers can see and interact
 * with, containing many different features that can be customised and extended by a developer.
 * <p>
 * A developer can subclass this class, override the methods or add them to customise the ways of processing inputs for
 * inventory menu events or modifying the inventories.
 *
 * @author Alan B.
 * @see InventoryMenu
 * @see PropertyMenu
 */
public abstract class AbstractMenu implements InventoryHolder {

    /**
     * Main, constant part of an {@link AbstractMenu} that identifies it
     */
    protected final Inventory inventory;

    /**
     * An {@link AbstractMenu} that is set to be opened after this one's closed, handled by a {@link MenuManager}.
     *
     * @see #setOpenNext(AbstractMenu)
     * @see #getOpenNext()
     */
    private AbstractMenu openNext;

    public AbstractMenu(@NotNull Inventory inventory) {
        Preconditions.checkArgument(inventory != null, "Inventory argument can't be null");
        this.inventory = inventory;
    }

    /**
     * Acts as an event handler for an ItemStack movement from source to destination inventory.
     * <p>
     * This event handler is most likely called in rare cases; when this {@link AbstractMenu} inventory belongs to a
     * container object and another object or inventory tries to move items to it. An example of this happening can be a
     * chest block container inventory with a hopper connecting to it that is trying to move items into it.
     *
     * @param event         InventoryMoveItemEvent event
     * @param isDestination Whether this {@link org.bukkit.inventory.Inventory} is equal to the {@link
     *                      InventoryMoveItemEvent#getDestination()}
     */
    public void onItemMove(@NotNull InventoryMoveItemEvent event, boolean isDestination) {
        event.setCancelled(true);
    }

    /**
     * Handles the {@link MenuManager} {@link MenuManager#getPlugin} {@link Plugin} event handler.
     *
     * @param event   PluginDisableEvent event
     * @param manager {@link MenuManager} that is passing this event
     */
    public void onDisable(@NotNull PluginDisableEvent event, @NotNull MenuManager manager) {
    }

    /**
     * Handles any {@link InventoryClickEvent} related to this inventory menu.
     * <p>
     * By default, the {@link InventoryAction} {@code COLLECT_TO_CURSOR} and {@code MOVE_TO_OTHER_INVENTORY} are
     * cancelled and any menu action is cancelled from interaction.
     *
     * @param event    InventoryClickEvent event
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
     * Handles the inventory close events of this menu and opens a next menu after this one.
     * <p>
     * Inventory menus can be immediately reopened by {@link MenuManager} after a menu processes {@link
     * InventoryCloseEvent} with this method. This can be done by setting a menu that will be opened after the next
     * inventory close event with the {@link #setOpenNext(AbstractMenu)} method, including inside this handler.
     *
     * @param event InventoryCloseEvent event
     * @see #getOpenNext()
     */
    public void onClose(@NotNull InventoryCloseEvent event) {
    }

    /**
     * Acts as an event handler for the inventory opening event.
     *
     * @param event InventoryOpenEvent event
     */
    public void onOpen(@NotNull InventoryOpenEvent event) {
    }

    /**
     * Acts as an event handler for the inventory item dragging event.
     * <p>
     * By default, any item dragging on the menu will be cancelled.
     *
     * @param event InventoryDragEvent event
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
     * Sets an item stack at the given inventory {@link AbstractMenu} slot(s).
     *
     * @param item  Item stack object
     * @param slots Slots that this item stack will be placed at
     * @return This instance, useful for chaining
     * @throws IndexOutOfBoundsException If a slot in the slot array argument is out of this inventory's array bounds
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
     * Sets the given menu to be opened immediately after this one's closed.
     *
     * @param nextMenu {@link AbstractMenu} or null
     * @return This instance, useful for chaining
     * @see #onClose(InventoryCloseEvent)
     */
    @NotNull
    public AbstractMenu setOpenNext(@Nullable AbstractMenu nextMenu) {
        openNext = nextMenu;
        return this;
    }

    /**
     * Opens this {@link AbstractMenu} for the given viewer(s).
     *
     * @param viewers Players that will see this {@link AbstractMenu} inventory
     */
    public void open(@NotNull HumanEntity... viewers) {
        UtilitiesPlugin.getMenuManager().ifPresent(manager -> manager.open(this, viewers));
    }

    /**
     * Closes all {@link AbstractMenu}s of this instance for all viewers who are viewing it.
     * <p>
     * Closing {@link AbstractMenu}s for a {@link HumanEntity} might not always work because their {@link
     * #onClose(InventoryCloseEvent)} can choose to open a new, possibly the same one.
     *
     * @return This instance, useful for chaining
     */
    @NotNull
    public final AbstractMenu close() {
        new ArrayList<>(getViewers()).forEach(HumanEntity::closeInventory);
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
     * Returns the {@link AbstractMenu} that will be opened next after this one's closed.
     *
     * @return {@link Optional} of a nullable {@link AbstractMenu}
     */
    @NotNull
    public Optional<AbstractMenu> getOpenNext() {
        return Optional.ofNullable(openNext);
    }

    /**
     * Returns a List of human entities (usually players) that are currently viewing this inventory menu.
     *
     * @return {@link List} of human entities (usually players)
     */
    @NotNull
    public List<HumanEntity> getViewers() {
        return getInventory().getViewers();
    }

    @NotNull
    @Override
    public final Inventory getInventory() {
        return inventory;
    }

    /**
     * Returns the inventory type of this {@link AbstractMenu}.
     * <p>
     * Inventory type for an inventory with rows is always a CHEST.
     *
     * @return Not null inventory type of this inventory {@link AbstractMenu}
     */
    @NotNull
    public InventoryType getType() {
        return getInventory().getType();
    }

    /**
     * Returns the slot amount that this {@link AbstractMenu} has.
     *
     * @return Amount of slots of this inventory {@link AbstractMenu}
     */
    public int getSize() {
        return getInventory().getSize();
    }
}