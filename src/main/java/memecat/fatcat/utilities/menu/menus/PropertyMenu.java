package memecat.fatcat.utilities.menu.menus;

import com.google.common.base.Preconditions;
import memecat.fatcat.utilities.menu.MenuManager;
import memecat.fatcat.utilities.menu.slot.ISlotProperty;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Implementation of {@link Menu} with an {@link Object} array of the same size as the inventory, with many
 * methods for working with these properties.
 *
 * @param <E> Single object type to be stored in the slots of this menu inventory
 * @author Alan B. | FatCat
 * @see ISlotProperty
 */
public class PropertyMenu<E> extends Menu {

    /**
     * Properties of each slot in this inventory are stored in an array, linear like inventories.
     */
    private E[] properties = (E[]) new Object[this.getInventory().getSize()];

    /**
     * Creates a new {@link PropertyMenu} using the default constructor for {@link Menu}, with an array of this
     * instance's generic type.
     *
     * @param inventory   Not null {@link Inventory} that will be wrapped and controlled by an {@link AbstractMenu}
     * @param menuManager Not null {@link MenuManager} that will be used for (un)registering this {@link AbstractMenu}
     *                    and passing events to it
     */
    public PropertyMenu(@NotNull Inventory inventory, @NotNull MenuManager menuManager) {
        super(inventory, menuManager);
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
     * @throws IndexOutOfBoundsException If the fromSlot or toSlot argument isn't within the inventory's boundaries
     * @throws IllegalArgumentException  If the fromSlot is greater than the toSlot argument or the skipForSlots
     *                                   argument is lower than 1
     */
    public PropertyMenu<E> fillSkip(@Nullable E property, int fromSlot, int toSlot, int skipForSlots) {
        checkRange(fromSlot, toSlot, this.getInventory().getSize());
        Preconditions.checkArgument(skipForSlots > 0, "skipForSlots argument (" + skipForSlots + ") can't be smaller than 1");

        for (int slot = fromSlot; slot < toSlot; slot += skipForSlots) {
            this.properties[slot] = property;
        }

        return this;
    }

    /**
     * Sets the same {@link ISlotProperty} and {@link ItemStack} instance at the given slots in the inventory.
     *
     * @param item     Nullable {@link ItemStack} that will be set in all given slots
     * @param property Nullable {@link ISlotProperty} that will be set in all given slots
     * @param slots    Slots in which the given item and property will be set
     * @return This instance, useful for chaining
     * @throws IndexOutOfBoundsException If a slot in the slot array argument is out of this inventory's boundaries
     * @throws IllegalArgumentException  If the slot array argument is null
     */
    public PropertyMenu<E> set(@Nullable E property, @Nullable ItemStack item, int... slots) {

        for (int slot : slots) {
            checkElement(slot, this.getInventory().getSize());
            this.properties[slot] = property;
            this.getInventory().setItem(slot, item);
        }

        return this;
    }

    /**
     * Modifies an {@link ISlotProperty} located at a given slot with given operations to perform.
     *
     * @param applyProperty Lambda method that'll take a slot property object as an argument and perform operations on
     *                      it
     * @param slot          Slot at which an {@link ISlotProperty} that is being modified is located at
     * @return This instance, useful for chaining
     * @throws IndexOutOfBoundsException If the slot argument is out of this inventory's array boundaries
     * @throws IllegalArgumentException  If the {@link Consumer}&lt;{@link ISlotProperty}&gt; argument is null
     */
    public PropertyMenu<E> changeProperty(@NotNull Consumer<E> applyProperty, int slot) {
        Preconditions.checkArgument(applyProperty != null, "Consumer<ISlotProperty> argument can't be null");
        this.getSlotProperty(slot).ifPresent(applyProperty);
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
     * @throws IndexOutOfBoundsException If the fromSlot or to-slot argument isn't within the inventory's boundaries
     * @throws IllegalArgumentException  If the fromSlot is greater than the to-slot argument
     */
    public PropertyMenu<E> fillInterval(@Nullable E property, int fromSlot, int toSlot) {
        return this.fillSkip(property, fromSlot, toSlot, 1);
    }

    /**
     * Sets all or only empty inventory slot properties to equal to the given property object.
     *
     * @param property {@link ISlotProperty} that will be set in all slots
     * @param replace  Whether existing properties should be replaced with a new one
     * @return This instance, useful for chaining
     */
    public PropertyMenu<E> fill(@Nullable E property, boolean replace) {
        if (replace) {
            for (int slot = 0; slot < this.getInventory().getSize(); slot++) {
                this.properties[slot] = property;
            }
        } else {
            for (int slot = 0; slot < this.getInventory().getSize(); slot++) {
                if (!this.getItem(slot).isPresent()) { // Java 11: .isEmpty()
                    this.properties[slot] = property;
                }
            }
        }

        return this;
    }

    /**
     * Sets a slot property object at the given inventory {@link AbstractMenu} slot(s).
     *
     * @param property {@link ISlotProperty} object
     * @param slots    Slots that these properties will belong to
     * @return This instance, useful for chaining
     * @throws IllegalArgumentException  If the array of slots is null
     * @throws IndexOutOfBoundsException If a slot in the slot array argument is out of this inventory's boundaries
     */
    public PropertyMenu<E> set(@Nullable E property, int... slots) {
        int size = this.getInventory().getSize();

        for (int slot : slots) {
            checkElement(slot, size);
            this.properties[slot] = property;
        }

        return this;
    }

    /**
     * Clears the whole inventory array of slot properties.
     *
     * @return This instance, useful for chaining
     */
    public PropertyMenu<E> clearProperties() {
        this.properties = (E[]) new Object[this.getInventory().getSize()];
        return this;
    }

    /**
     * Clears the whole inventory of it's contents and slot properties.
     *
     * @return This instance, useful for chaining
     */
    @NotNull
    @Override
    public PropertyMenu<E> clear() {
        this.clearContents();
        return this.clearProperties();
    }

    /**
     * Returns an property stored at the given slot of this inventory menu or null if it doesn't exist.
     *
     * @param slot Slot index location of the Object in the inventory
     * @return {@link Optional} of nullable Object
     * @throws IndexOutOfBoundsException If the given slot argument is out of this inventory's boundaries
     */
    @NotNull
    public Optional<E> getSlotProperty(int slot) {
        checkElement(slot, this.getInventory().getSize());
        return Optional.ofNullable(this.properties[slot]);
    }
}