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

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Implementation of {@link InventoryMenu} with an {@link AbstractSlotProperty} array with the same size as the
 * inventory, with many methods for working with these properties.
 *
 * @see AbstractSlotProperty
 * @see InventoryMenu
 */
public class PropertyMenu extends InventoryMenu implements Iterable<PropertyMenu.SlotData> {

    /**
     * Properties of each slot in this inventory are stored in an array, linear like inventories.
     */
    protected AbstractSlotProperty[] properties;

    /**
     * Creates a new {@link PropertyMenu} from the given inventory type, holder and display name (title).
     *
     * @param type   Type of an inventory
     * @param holder Object that this inventory belongs to (chest, player, horse..)
     * @param title  Display name of this inventory
     */
    public PropertyMenu(@NotNull InventoryType type, @Nullable InventoryHolder holder, @Nullable String title) {
        super(type, holder, title);
        properties = new AbstractSlotProperty[getSize()];
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
        properties = new AbstractSlotProperty[getSize()];
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
     */
    public PropertyMenu(@NotNull Inventory inventory) {
        super(inventory);
        properties = new AbstractSlotProperty[getSize()];
    }

    /**
     * {@inheritDoc}
     * By default, runs an {@link AbstractSlotProperty} at the event's slot or cancels the event if it doesn't exist.
     */
    @Override
    public void onClick(@NotNull InventoryClickEvent event, boolean external) {
        super.onClick(event, external);

        if (event.isCancelled()) {
            return;
        }

        /*
         * By default, a slot that lacks a slot property will cancel any interactions with it. This is why every existing
         * slot property in a slot needs to control the event result via Event.setCancelled(boolean cancelled).
         */

        if (!runProperty(event)) {
            event.setCancelled(true);
        }
    }

    /**
     * Fills inventory slots with slot properties by skipping an amount of given slots from a start to the end.
     * <p>
     * This method places a property in the first slot and keeps on adding the skipForSlots amount until the
     * current slot is bigger than toSlot.
     *
     * @param property     Slot property object or null
     * @param fromSlot     Start index location of a slot in an inventory
     * @param toSlot       End index location of a slot in an inventory
     * @param skipForSlots Amount of slots to be skipped till next property placement
     * @return This instance, useful for chaining
     */
    @NotNull
    public PropertyMenu fillSkip(@Nullable AbstractSlotProperty property, int fromSlot, int toSlot, int skipForSlots) {
        if (fromSlot > toSlot) {
            throw new IllegalArgumentException("From-slot index argument (" + fromSlot + ") is greater than to-slot index (" + toSlot + ") argument.");
        }

        Preconditions.checkPositionIndex(fromSlot, getSize(), "Invalid from-slot index argument of " + fromSlot + " with size " + getSize());
        Preconditions.checkPositionIndex(toSlot, getSize(), "Invalid to-slot index argument of " + toSlot + " with size " + getSize());

        if (skipForSlots < 1) {
            for (int i = fromSlot; i < toSlot; i++) {
                setAndUpdate(property, i);
            }
        } else {
            for (int i = fromSlot; i < toSlot; i += skipForSlots) {
                setAndUpdate(property, i);
            }
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
     */
    @NotNull
    public PropertyMenu set(@Nullable AbstractSlotProperty property, @Nullable ItemStack item, int... slots) {
        for (int slot : slots) {
            Preconditions.checkElementIndex(slot, getSize(), "Invalid slot property index of " + slot + " with size " + getSize());
            setAndUpdate(property, slot);
            setAndUpdate(item, slot);
        }

        return this;
    }

    /**
     * Modifies an {@link AbstractSlotProperty} located at a given slot with given operations to perform.
     *
     * @param applyProperty Lambda method that'll take a slot property object as an argument and perform operations on it
     * @param slot          Slot at which an {@link AbstractSlotProperty} that is being modified is located at
     * @return This instance, useful for chaining
     */
    @NotNull
    public PropertyMenu changeProperty(@NotNull Consumer<AbstractSlotProperty> applyProperty, int slot) {
        Preconditions.checkArgument(applyProperty != null, "Consumer<AbstractSlotProperty> argument shouldn't be null");
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
     */
    @NotNull
    public PropertyMenu fillInterval(@Nullable AbstractSlotProperty property, int fromSlot, int toSlot) {
        if (fromSlot > toSlot) {
            throw new IllegalArgumentException("From-slot index argument (" + fromSlot + ") is greater than to-slot index (" + toSlot + ") argument.");
        }

        Preconditions.checkPositionIndex(fromSlot, getSize(), "Invalid from-slot index argument of " + fromSlot + " with size " + getSize());
        Preconditions.checkPositionIndex(toSlot, getSize(), "Invalid to-slot index argument of " + toSlot + " with size " + getSize());

        for (int i = fromSlot; i < toSlot; i++) {
            setAndUpdate(property, i);
        }

        return this;
    }

    /**
     * Sets all or only empty inventory slot properties to equal to the given property object.
     *
     * @param property {@link AbstractSlotProperty} that will be set in all slots
     * @param replace  Whether existing properties should be replaced with a new one
     * @return This instance, useful for chaining
     */
    @NotNull
    public PropertyMenu fillAll(@Nullable AbstractSlotProperty property, boolean replace) {
        for (int i = 0; i < getSize(); i++) {
            if (replace || !getSlotProperty(i).isPresent()) {
                setAndUpdate(property, i);
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
     */
    @NotNull
    public PropertyMenu set(@Nullable AbstractSlotProperty property, int... slots) {
        for (int slot : slots) {
            Preconditions.checkElementIndex(slot, getSize(), "Invalid slot property index of " + slot + " with size " + getSize());
            setAndUpdate(property, slot);
        }

        return this;
    }

    /**
     * Directly sets a property at the appropriate (expected) given slot index.
     *
     * @param property Slot property object
     * @param slot     Inventory slot index
     */
    protected void setAndUpdate(@Nullable AbstractSlotProperty property, int slot) {
        properties[slot] = property;
    }


    /**
     * Runs a property in this menu at the given slot with the given event and this menu as arguments.
     *
     * @param event InventoryClickEvent event
     * @return Whether a property at the given slot exists
     */
    public boolean runProperty(@Nullable InventoryClickEvent event) {
        if (event == null) {
            return false;
        }

        return getSlotProperty(event.getSlot()).map(property -> {
            property.run(event, this);
            return true;
        }).orElse(false);
    }

    /**
     * Clears the whole inventory array of slot properties.
     *
     * @return This instance, useful for chaining
     */
    @NotNull
    public PropertyMenu clearProperties() {
        properties = new AbstractSlotProperty[getSize()];
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
     */
    @NotNull
    public Optional<AbstractSlotProperty> getSlotProperty(int slot) {
        Preconditions.checkElementIndex(slot, getSize(), "Invalid slot property index of " + slot + " with size " + getSize());
        return Optional.ofNullable(properties[slot]);
    }

    /**
     * Creates and returns a new {@link SlotIterator} with the given beginning index for this inventory menu.
     *
     * @param start Beginning slot index, ranging from -1 to [getSize() - 1]
     * @return New instance of {@link SlotIterator}
     */
    @NotNull
    public SlotIterator iterator(int start) {
        return new SlotIterator(start);
    }

    /**
     * Creates and returns a new {@link SlotIterator} for this inventory menu.
     *
     * @return New instance of {@link SlotIterator}
     */
    @NotNull
    public SlotIterator iterator() {
        return iterator(0);
    }

    /**
     * Allows iteration through this inventory menu's slots and modifications of their contents.
     *
     * @see SlotData
     */
    public class SlotIterator implements Iterator<SlotData> {
        private final int lastSlot = getSize() - 1;
        private int currentIndex;

        /**
         * Creates a new instance with a given beginning index.
         *
         * @param start Beginning slot index in this inventory
         */
        public SlotIterator(int start) {
            Preconditions.checkPositionIndex(start, getSize(), "Invalid starting index for new SlotIterator of " + start + " with size " + getSize());
            currentIndex = start;
        }

        /**
         * Creates a new instance with the beginning index of 0.
         */
        public SlotIterator() {
            this(0);
        }

        /**
         * Sets the current {@link SlotData}'s {@link AbstractSlotProperty} and {@link ItemStack} to equal null values.
         */
        @Override
        public void remove() {
            new SlotData(currentIndex).set(null, null);
        }

        /**
         * Returns the {@link PropertyMenu} that this {@link SlotIterator} belongs to.
         *
         * @return {@link PropertyMenu} that this {@link SlotIterator} belongs to
         */
        @NotNull
        public PropertyMenu getMenu() {
            return PropertyMenu.this;
        }

        /**
         * Returns the slot index of this inventory menu that this {@link SlotIterator} is currently at.
         *
         * @return Current slot index, ranging from -1 to [getSize() - 1]
         */
        public int getCurrentIndex() {
            return currentIndex;
        }

        /**
         * Returns whether this slot iterator can have a previous element by using the previous() method.
         *
         * @return Whether this slot iterator can have a previous element
         */
        public boolean hasPrevious() {
            return currentIndex > 0;
        }

        /**
         * Returns the {@link SlotData} of the slot that is previous in this iteration, and decreases the current index.
         *
         * @return {@link SlotData} of the previous slot
         */
        @NotNull
        public SlotData previous() {
            if (currentIndex == 0) {
                throw new NoSuchElementException();
            }

            return new SlotData(currentIndex--);
        }

        /**
         * Returns whether this slot iterator can have a next element by using the next() method.
         *
         * @return Whether this slot iterator can have a next element
         */
        public boolean hasNext() {
            return currentIndex < lastSlot;
        }

        /**
         * Returns the {@link SlotData} of the slot that is next in this iteration, and increases the current index.
         *
         * @return {@link SlotData} of the next slot
         */
        @NotNull
        @Override
        public SlotData next() {
            if (currentIndex == getSize()) {
                throw new NoSuchElementException();
            }

            return new SlotData(currentIndex++);
        }
    }

    /**
     * Represents an element of the {@link SlotIterator} or {@link PropertyMenu} that can lazily get both the property
     * and item at a specific slot.
     */
    public class SlotData {

        private int slot;

        /**
         * Creates a new instance from the given slot in this inventory.
         *
         * @param slot Slot index
         * @throws IllegalArgumentException If the slot index argument is out of bounds
         */
        public SlotData(int slot) {
            Preconditions.checkElementIndex(slot, getSize(), "Invalid element index for new SlotData: " + slot + ", size: " + getSize());
            this.slot = slot;
        }

        /**
         * Sets a new {@link AbstractSlotProperty} and {@link ItemStack} for this inventory at the slot of this {@link SlotData}.
         *
         * @param item     Nullable {@link ItemStack}
         * @param property Nullable {@link AbstractSlotProperty}
         * @return This instance, useful for chaining
         */
        @NotNull
        public SlotData set(@Nullable AbstractSlotProperty property, @Nullable ItemStack item) {
            return setProperty(property).setItem(item);
        }

        /**
         * Sets a new slot property for this inventory at this slot.
         *
         * @param property Nullable new property to set
         * @return This instance, useful for chaining
         */
        @NotNull
        public SlotData setProperty(@Nullable AbstractSlotProperty property) {
            PropertyMenu.this.setAndUpdate(property, slot);
            return this;
        }

        /**
         * Sets a new item for this inventory at this slot.
         *
         * @param item ItemStack object, or null
         * @return This instance, useful for chaining
         */
        @NotNull
        public SlotData setItem(@Nullable ItemStack item) {
            PropertyMenu.this.setAndUpdate(item, slot);
            return this;
        }

        /**
         * Returns the slot property that is stored at the slot of this inventory.
         *
         * @return Nullable slot property object
         */
        @NotNull
        public Optional<AbstractSlotProperty> getProperty() {
            return Optional.ofNullable(properties[slot]);
        }

        /**
         * Returns the item stack that is stored at the slot of this inventory.
         *
         * @return ItemStack object, or null
         */
        @NotNull
        public Optional<ItemStack> getItem() {
            return Optional.ofNullable(getInventory().getItem(slot));
        }

        /**
         * Returns the slot index of this {@link SlotData} in this inventory.
         *
         * @return Slot index of this {@link SlotData} in this inventory
         */
        public int getSlot() {
            return slot;
        }
    }
}