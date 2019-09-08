package memecat.fatcat.utilities.menu.menus;

import com.google.common.base.Preconditions;
import memecat.fatcat.utilities.menu.MenuManager;
import memecat.fatcat.utilities.menu.slot.AbstractSlotProperty;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
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
 * @param <E> Type that is a subclass of {@link AbstractSlotProperty}, can be left empty ({@code <>}, but never nothing)
 *            to allow any subclass
 * @author Alan B.
 * @see AbstractSlotProperty
 */
public class PropertyMenu<E extends AbstractSlotProperty> extends InventoryMenu {

    /**
     * Properties of each slot in this inventory are stored in an array, linear like inventories.
     */
    private E[] properties;

    /**
     * Creates a new {@link PropertyMenu} using the default constructor for {@link InventoryMenu}, with an array of this
     * instance's generic type.
     *
     * @param inventory   Not null {@link Inventory} that will be wrapped and controlled by an {@link AbstractMenu}
     * @param menuManager Not null {@link MenuManager} that will be used for (un)registering this {@link AbstractMenu}
     *                    and passing events to it
     */
    public PropertyMenu(@NotNull Inventory inventory, @NotNull MenuManager menuManager) {
        super(inventory, menuManager);

        properties = (E[]) new AbstractSlotProperty[getInventory().getSize()];
    }

    /**
     * {@inheritDoc} By default, runs an existing {@link AbstractSlotProperty} or cancels the {@link
     * InventoryClickEvent}. Every existing {@link AbstractSlotProperty} should decide whether to cancel the {@link
     * org.bukkit.event.inventory.InventoryClickEvent} through {@link org.bukkit.event.Cancellable#setCancelled(boolean)}
     * though.
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

        Optional<E> property = getSlotProperty(event.getSlot());

        if (property.isPresent()) {
            property.get().run(event, this);
        } else {
            event.setCancelled(true);
        }
    }

    /**
     * Fills inventory slots with slot properties by skipping an amount of given slots from a start to the end.
     *
     * <p>This method places a property in the first slot and keeps on adding the skipForSlots amount until the current
     * slot is bigger than toSlot.
     *
     * @param property     Slot property object or null
     * @param fromSlot     Start index location of a slot in an inventory
     * @param toSlot       End index location of a slot in an inventory
     * @param skipForSlots Amount of slots to be skipped till next property placement
     * @return This instance, useful for chaining
     * @throws IndexOutOfBoundsException If the from-slot or to-slot argument isn't within the inventory's boundaries
     * @throws IllegalArgumentException  If the from-slot is greater than the to-slot argument or the skipForSlots
     *                                   argument is lower than 1
     */
    public PropertyMenu<E> fillSkip(@Nullable E property, int fromSlot, int toSlot, int skipForSlots) {
        checkRange(fromSlot, toSlot, getInventory().getSize());
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
     * @throws IndexOutOfBoundsException If a slot in the slot array argument is out of this inventory's boundaries
     * @throws IllegalArgumentException  If the slot array argument is null
     */
    public PropertyMenu<E> set(@Nullable E property, @Nullable ItemStack item, int... slots) {
        Preconditions.checkArgument(slots != null, "Array of slots can't be null");

        for (int slot : slots) {
            checkElement(slot, getInventory().getSize());
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
     * @throws IllegalArgumentException  If the {@link Consumer}&lt;{@link AbstractSlotProperty}&gt; argument is null
     */
    public PropertyMenu<E> changeProperty(@NotNull Consumer<E> applyProperty, int slot) {
        Preconditions.checkArgument(applyProperty != null, "Consumer<AbstractSlotProperty> argument can't be null");
        getSlotProperty(slot).ifPresent(applyProperty);
        return this;
    }

    /**
     * Fills inventory slots with a slot property from a beginning slot to an ending slot.
     *
     * <p>An "interval" in the case of this method can be defined as a set of whole numbers ranging from the given
     * beginning slot index (inclusive) to the given slot index (exclusive). This is referenced to mathematical
     * intervals, or simply shown with symbols: [fromSlot, toSlot&gt; or firstSlot = fromSlot, endSlot = (toSlot - 1).
     *
     * @param property Slot property object or null
     * @param fromSlot Beginning index of a slot in an inventory
     * @param toSlot   Ending index of a slot in an inventory
     * @return This instance, useful for chaining
     * @throws IndexOutOfBoundsException If the from-slot or to-slot argument isn't within the inventory's boundaries
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
            for (int slot = 0; slot < getInventory().getSize(); slot++) {
                properties[slot] = property;
            }
        } else {
            for (int slot = 0; slot < getInventory().getSize(); slot++) {
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
        int size = getInventory().getSize();

        for (int slot : slots) {
            checkElement(slot, size);
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
        properties = (E[]) new AbstractSlotProperty[getInventory().getSize()];
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
     * @throws IndexOutOfBoundsException If the given slot argument is out of this inventory's boundaries
     */
    @NotNull
    public Optional<E> getSlotProperty(int slot) {
        checkElement(slot, getInventory().getSize());
        return Optional.ofNullable(properties[slot]);
    }
}