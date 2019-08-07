package memecat.fatcat.utilities.menu.menus;

import memecat.fatcat.utilities.menu.MenuManager;
import memecat.fatcat.utilities.menu.attribute.Rows;
import memecat.fatcat.utilities.menu.slot.AbstractSlotProperty;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
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

import java.util.List;
import java.util.Optional;


/**
 * Represents a collection of functional actions and features of an inventory that multiple viewers can see and interact
 * with, containing many different features that can be customised and extended by a developer.
 * <p>
 * This means that a developer can extend this class and create their own menu with their own features and ways of
 * processing input, although it's limited to exist with some of the preexisting features of this API. This is done by
 * overriding methods even though most of them are already defined, but superclass methods can be called from subclasses
 * with the form super.superClassMethod(arguments) if those methods are useful or needed.
 *
 * @author Alan B.
 * @see InventoryMenu
 * @see PropertyMenu
 */
public abstract class AbstractMenu implements InventoryHolder {

    /**
     * An inventory that is set to be opened after this one's closed.
     *
     * @see #setOpenNext(AbstractMenu)
     * @see #getOpenNext()
     */
    private AbstractMenu openNext = null;

    /**
     * Acts as an event handler for an ItemStack movement from source to destination inventory.
     * <p>
     * This event handler is most likely called in rare cases; when this {@link AbstractMenu} inventory belongs to a
     * container object and another object or inventory tries to move items to it. An example of this happening can be a
     * chest block container inventory with a hopper connecting to it that is trying to move items into it.
     *
     * @param event             InventoryMoveItemEvent event
     * @param equalsDestination Whether these contents are the destination inventory of this event
     */
    public void onItemMove(@NotNull InventoryMoveItemEvent event, boolean equalsDestination) {
        if (equalsDestination) {
            event.setCancelled(true);
        }
    }

    /**
     * Handles the {@link MenuManager} {@link MenuManager#getPlugin} {@link Plugin} event handler.
     *
     * @param event   PluginDisableEvent event
     * @param manager {@link MenuManager} that can receive a new {@link Plugin}, stopping all other menus from having to
     *                handle the given event
     */
    public void onDisable(@NotNull PluginDisableEvent event, @NotNull MenuManager manager) {
    }

    /**
     * Acts as an event handler for an inventory click related to this menu's inventory, with a parameter denoting
     * whether the click is outside of the inventory.
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
            default:
                break;
        }
    }

    /**
     * Handles the inventory close events of this menu and opens a next menu after this one.
     * <p>
     * Inventory menus can be immediately reopened by {@link MenuManager} after a menu processes
     * {@link InventoryCloseEvent} with this method. This can be done by setting a menu that will be opened after the next inventory close
     * event with the {@link #setOpenNext(AbstractMenu)} method, including inside this handler.
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
     *
     * @param event InventoryDragEvent event
     */
    public void onDrag(@NotNull InventoryDragEvent event) {
        int topEndSlot = event.getView().getTopInventory().getSize() - 1;

        for (int i : event.getRawSlots()) {
            if (i <= topEndSlot) {
                event.setCancelled(true);
                return;
            }
        }
    }

    /**
     * Sets a slot property object at the given inventory {@link AbstractMenu} slot(s).
     *
     * @param property {@link AbstractSlotProperty} object
     * @param slots    Slots that these properties will belong to
     * @return This instance, useful for chaining
     */
    @NotNull
    public abstract AbstractMenu set(@Nullable AbstractSlotProperty property, int... slots);

    /**
     * Sets an item stack at the given inventory {@link AbstractMenu} slot(s).
     *
     * @param item  Item stack object
     * @param slots Slots that this item stack will be placed at
     * @return This instance, useful for chaining
     */
    @NotNull
    public abstract AbstractMenu set(@Nullable ItemStack item, int... slots);

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
    public void openMenu(@NotNull Player... viewers) {
        MenuManager.getInstance().openMenu(this, viewers);
    }

    /**
     * Clears all of this inventory menu's contents.
     *
     * @return This instance, useful for chaining
     */
    @NotNull
    public abstract AbstractMenu clear();

    /**
     * Closes all {@link AbstractMenu}s of this instance for all viewers who are viewing it.
     * <p>
     * Closing inventories might not always work because their event handlers might disallow them to be closed.
     *
     * @return This instance, useful for chaining
     */
    @NotNull
    public final AbstractMenu closeMenus() {
        MenuManager.getInstance().closeMenus(getClass());
        return this;
    }

    /**
     * Returns an ItemStack at the given slot of this inventory menu or null if it doesn't exist.
     *
     * @param slot Slot index location of the item in the inventory
     * @return Optional of a nullable ItemStack
     */
    @NotNull
    public abstract Optional<ItemStack> getItem(int slot);

    /**
     * Returns a List of human entities (usually players) that are currently viewing this inventory menu.
     *
     * @return List of human entities (usually players)
     */
    @NotNull
    public abstract List<HumanEntity> getViewers();

    /**
     * Returns the {@link AbstractMenu} that will be opened next after this one's closed.
     *
     * @return {@link AbstractMenu} or null
     */
    @NotNull
    public Optional<AbstractMenu> getOpenNext() {
        return Optional.ofNullable(openNext);
    }

    /**
     * Returns the inventory that this menu represents.
     *
     * @return Not null {@link Inventory}
     */
    @NotNull
    public abstract Inventory getInventory();

    /**
     * Returns the inventory type of this {@link AbstractMenu}.
     * <p>
     * Inventory type for an inventory with rows is always a CHEST.
     *
     * @return Not null inventory type of this inventory {@link AbstractMenu}
     */
    @NotNull
    public abstract InventoryType getType();

    /**
     * Returns the amount of rows that this {@link AbstractMenu} has.
     *
     * @return {@link Rows} enum
     */
    @NotNull
    public abstract Optional<Rows> getRows();

    /**
     * Returns the slot amount that this {@link AbstractMenu} has.
     *
     * @return Amount of slots of this inventory {@link AbstractMenu}
     */
    public abstract int getSize();
}