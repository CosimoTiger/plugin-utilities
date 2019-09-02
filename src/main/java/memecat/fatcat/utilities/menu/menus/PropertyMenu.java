package memecat.fatcat.utilities.menu.menus;

import com.google.common.base.Preconditions;
import memecat.fatcat.utilities.menu.attribute.Rows;
import memecat.fatcat.utilities.menu.slot.AbstractSlotProperty;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Instantiable implementation of {@link InventoryMenu} with an {@link AbstractSlotProperty} array of the same size as
 * the inventory, with many methods for working with these properties. This class is generified to allow different
 * subclasses of {@link AbstractSlotProperty} to be used.
 *
 * @author Alan B.
 * @see AbstractSlotProperty
 * @see InventoryMenu
 */
public class PropertyMenu<E extends AbstractSlotProperty> extends InventoryMenu {

    /**
     * Properties of each slot in this inventory are stored in an array, linear like inventories.
     */
    protected E[] properties;

    /**
     * Creates a new {@link PropertyMenu} from the given inventory type, holder and display name (title).
     *
     * @param type   Type of an inventory
     * @param holder Object that this inventory belongs to (chest, player, horse..)
     * @param title  Display name of this inventory
     */
    public PropertyMenu(@NotNull InventoryType type, @Nullable InventoryHolder holder, @Nullable String title) {
        super(type, holder, title);

        properties = (E[]) new AbstractSlotProperty[getSize()];
    }

    /**
     * Creates a new {@link InventoryMenu} from the given inventory type and holder.
     *
     * @param type   Type of an inventory
     * @param holder Object that this inventory belongs to (chest, player, horse..)
     */
    public PropertyMenu(@NotNull InventoryType type, @Nullable InventoryHolder holder) {
        this(type, holder, null);
    }

    /**
     * Creates a new {@link InventoryMenu} from the given inventory type and display name (title).
     *
     * @param type  Type of an inventory
     * @param title Display name of this inventory
     */
    public PropertyMenu(@NotNull InventoryType type, @Nullable String title) {
        this(type, null, title);
    }

    /**
     * Creates a new {@link InventoryMenu} from the given inventory type.
     *
     * @param type Type of an inventory
     */
    public PropertyMenu(@NotNull InventoryType type) {
        this(type, null, null);
    }

    /**
     * Creates a new chest {@link InventoryMenu} from the given amount of rows, it's holder and display name (title).
     *
     * @param rows   {@link Rows} enum, amount of rows in this chest inventory
     * @param holder Object that this inventory belongs to (chest, player, horse..)
     * @param title  Display name of this inventory
     */
    public PropertyMenu(@NotNull Rows rows, @Nullable InventoryHolder holder, @Nullable String title) {
        super(rows, holder, title);
        properties = (E[]) new AbstractSlotProperty[getSize()];
    }

    /**
     * Creates a new chest {@link InventoryMenu} from the given amount of rows and it's holder.
     *
     * @param rows   {@link Rows} enum, amount of rows in this chest inventory
     * @param holder Object that this inventory belongs to (chest, player, horse..)
     */
    public PropertyMenu(@NotNull Rows rows, @Nullable InventoryHolder holder) {
        this(rows, holder, null);
    }

    /**
     * Creates a new chest {@link InventoryMenu} from the given amount of rows and display name (title).
     *
     * @param rows  {@link Rows} enum, amount of rows in this chest inventory
     * @param title Display name of this inventory
     */
    public PropertyMenu(@NotNull Rows rows, @Nullable String title) {
        this(rows, null, title);
    }

    /**
     * Creates a new chest {@link InventoryMenu} from the given amount of rows.
     *
     * @param rows {@link Rows} enum, amount of rows in this chest inventory
     */
    public PropertyMenu(@NotNull Rows rows) {
        this(rows, null, null);
    }

    /**
     * Creates a new {@link InventoryMenu} with the given inventory and it's attributes equal to it.
     *
     * @param inventory Inventory that'll function as a menu
     * @throws IllegalArgumentException If the inventory argument is null or already has an {@link AbstractMenu} as it's
     *                                  {@link InventoryHolder}
     */
    public PropertyMenu(@NotNull Inventory inventory) {
        super(inventory);
        properties = (E[]) new AbstractSlotProperty[getSize()];
    }

    /**
     * {@inheritDoc} By default, runs an {@link AbstractSlotProperty} at the event's slot or cancels the event if it
     * doesn't exist.
     */
    @Override
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

        /*
         * By default, a slot that lacks a slot property will cancel any interactions with it. This is why every existing
         * slot property in a slot needs to control the event result via Event.setCancelled(boolean cancelled).
         */
        Optional<E> property = getSlotProperty(event.getSlot());

        if (property.isPresent()) {
            property.get().run(event, this);
        } else {
            event.setCancelled(true);
        }
    }

    /**
     * Fills inventory slots with slot properties by skipping an amount of given slots from a start to the end.
     * <p>
     * This method places a property in the first slot and keeps on adding the skipForSlots amount until the current
     * slot is bigger than toSlot.
     *
     * @param property     Slot property object or null
     * @param fromSlot     Start index location of a slot in an inventory
     * @param toSlot       End index location of a slot in an inventory
     * @param skipForSlots Amount of slots to be skipped till next property placement
     * @return This instance, useful for chaining
     * @throws IndexOutOfBoundsException If the from-slot or to-slot argument aren't within the inventory's boundaries,
     * @throws IllegalArgumentException  If the from-slot is greater than the to-slot argument or the skipForSlots
     *                                   argument is lower than 1
     */
    public PropertyMenu<E> fillSkip(@Nullable E property, int fromSlot, int toSlot, int skipForSlots) {
        checkRange(fromSlot, toSlot, getSize());
        Preconditions.checkArgument(skipForSlots > 0, "Skip-for-slots argument (" + skipForSlots + ") can't be smaller than 1");

        for (int slot = fromSlot; slot < toSlot; slot += skipForSlots) {
            properties[slot] = property;
        }

        return this;
    }

    /**
     * Sets an {@link AbstractSlotProperty} and an {@link ItemStack} for the given slots in the inventory.
     *
     * @param item     Nullable {@link ItemStack} that will be set in all given slots
     * @param property Nullable {@link AbstractSlotProperty} that will be set in all given slots
     * @param slots    Slots in which the given item and property will be set
     * @return This instance, useful for chaining
     * @throws IndexOutOfBoundsException If a slot in the slot array argument is out of this inventory's array bounds
     * @throws IllegalArgumentException  If the slot array argument is null
     */
    public PropertyMenu<E> set(@Nullable E property, @Nullable ItemStack item, int... slots) {
        Preconditions.checkArgument(slots != null, "Array of slots can't be null");

        for (int slot : slots) {
            checkElement(slot, getSize());
            properties[slot] = property;
            getInventory().setItem(slot, item);
        }

        return this;
    }

    /**
     * Modifies an {@link AbstractSlotProperty} located at a given slot with given operations to perform.
     *
     * @param applyProperty Lambda method that'll take a slot property object as an argument and perform operations on
     *                      it
     * @param slot          Slot at which an {@link AbstractSlotProperty} that is being modified is located at
     * @return This instance, useful for chaining
     * @throws IndexOutOfBoundsException If the slot argument is out of this inventory's array boundaries
     * @throws IllegalArgumentException  If the Consumer&lt;AbstractSlotProperty&gt; argument is null
     */
    public PropertyMenu<E> changeProperty(@NotNull Consumer<E> applyProperty, int slot) {
        Preconditions.checkArgument(applyProperty != null, "Consumer<AbstractSlotProperty> argument can't be null");
        getSlotProperty(slot).ifPresent(applyProperty);
        return this;
    }

    /**
     * Fills inventory slots with a slot property from a beginning slot to an ending slot.
     * <p>
     * An "interval" in the case of this method can be defined as a set of whole numbers ranging from the given
     * beginning slot index (inclusive) to the given slot index (exclusive). This is referenced to mathematical
     * intervals, or simply shown with symbols: [fromSlot, toSlot&gt; or firstSlot = fromSlot, endSlot = (toSlot - 1).
     *
     * @param property Slot property object or null
     * @param fromSlot Beginning index of a slot in an inventory
     * @param toSlot   Ending index of a slot in an inventory
     * @return This instance, useful for chaining
     * @throws IndexOutOfBoundsException If the from-slot or to-slot argument aren't within the inventory's boundaries
     * @throws IllegalArgumentException  If the from-slot is greater than the to-slot argument
     */
    public PropertyMenu<E> fillInterval(@Nullable E property, int fromSlot, int toSlot) {
        return fillSkip(property, fromSlot, toSlot, 1);
    }

    /**
     * Sets all or only empty inventory slot properties to equal to the given property object.
     *
     * @param property {@link AbstractSlotProperty} that will be set in all slots
     * @param replace  Whether existing properties should be replaced with a new one
     * @return This instance, useful for chaining
     */
    public PropertyMenu<E> fill(@Nullable E property, boolean replace) {
        if (replace) {
            return fillSkip(property, 0, getSize(), 1);
        } else {
            for (int slot = 0; slot < getSize(); slot++) {
                if (!getItem(slot).isPresent()) {
                    properties[slot] = property;
                }
            }
        }

        return this;
    }

    /**
     * Sets a slot property object at the given inventory {@link AbstractMenu} slot(s).
     *
     * @param property {@link AbstractSlotProperty} object
     * @param slots    Slots that these properties will belong to
     * @return This instance, useful for chaining
     * @throws IllegalArgumentException  If the array of slots is null
     * @throws IndexOutOfBoundsException If a slot in the slot array argument is out of this inventory's boundaries
     */
    public PropertyMenu<E> set(@Nullable E property, @NotNull int... slots) {
        Preconditions.checkArgument(slots != null, "Array of slots can't be null");

        for (int slot : slots) {
            checkElement(slot, getSize());
            properties[slot] = property;
        }

        return this;
    }

    /**
     * Clears the whole inventory array of slot properties.
     *
     * @return This instance, useful for chaining
     */
    public PropertyMenu<E> clearProperties() {
        properties = (E[]) new AbstractSlotProperty[getSize()];
        return this;
    }

    /**
     * Clears the whole inventory of it's contents and slot properties.
     *
     * @return This instance, useful for chaining
     */
    @NotNull
    @Override
    public PropertyMenu clear() {
        clearContents();
        return clearProperties();
    }

    /**
     * Returns an {@link AbstractSlotProperty} at the given slot of this inventory menu or null if it doesn't exist.
     *
     * @param slot Slot index location of the {@link AbstractSlotProperty} in the inventory
     * @return {@link Optional} of nullable {@link AbstractSlotProperty}
     * @throws IndexOutOfBoundsException If the given slot argument is out of this inventory's array bounds
     */
    @NotNull
    public Optional<E> getSlotProperty(int slot) {
        checkElement(slot, getSize());
        return Optional.ofNullable(properties[slot]);
    }
}