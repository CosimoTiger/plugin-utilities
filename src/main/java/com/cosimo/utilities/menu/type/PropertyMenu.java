package com.cosimo.utilities.menu.type;

import com.cosimo.utilities.menu.Button;
import lombok.NonNull;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * Implementation of {@link Menu} with an {@link Object} array of the same size as the inventory, with many methods for
 * working with these properties.
 * <p>
 * Example:
 * <pre>
 * {@code
 * var menu = new PropertyMenu<DyeColor>(Bukkit.createInventory(null, InventoryType.CHEST, "Color picker"));
 *
 * menu.set(DyeColor.RED, 0)
 *     .set(DyeColor.BLUE, 1);
 *
 * var iterator = menu.iterator();
 *
 * while (iterator.hasNext()) {
 *     System.out.println("Property: " + iterator.next());
 * }
 * }
 * </pre>
 *
 * @param <E> Expected object type to be stored in each slot
 * @author CosimoTiger
 * @see AbstractMenu
 */
@SuppressWarnings("unchecked")
public class PropertyMenu<E> extends AbstractMenu<PropertyMenu<E>, E> implements Iterable<E> {

    private final E[] properties = (E[]) new Object[this.getInventory().getSize()];

    /**
     * Creates a new {@link PropertyMenu} using the default constructor for {@link Menu}, with an array of this
     * instance's generic type with default null values.
     *
     * @param inventory Not null {@link Inventory} that will be wrapped and controlled by an {@link AbstractMenu}
     */
    public PropertyMenu(@NonNull Inventory inventory) {
        super(inventory);
    }

    /**
     * @param property Property {@link Object}
     * @param slot     Slot that this properties will belong to
     * @return This instance, useful for chaining
     * @throws IllegalArgumentException  If the array of slots is null
     * @throws IndexOutOfBoundsException If a slot in the slot array argument is out of this inventory's boundaries
     */
    @NonNull
    public PropertyMenu<E> set(E property, int slot) {
        this.properties[slot] = property;
        return this;
    }

    /**
     * Decomposes a {@link Button} into an {@link org.bukkit.inventory.ItemStack} and slot property object and sets them
     * in the given inventory {@link AbstractMenu} slot.
     *
     * @param button {@link Button} that has a nullable {@link org.bukkit.inventory.ItemStack} and nullable property
     * @param slot   Slot to place the {@link Button}'s {@link org.bukkit.inventory.ItemStack} in the actual
     *               {@link Inventory} and property in this {@link PropertyMenu}
     * @return This instance, useful for chaining
     * @throws NullPointerException      If the {@link Button} is null
     * @throws IndexOutOfBoundsException If the slot is out of this inventory's boundaries
     */
    @Override
    @NonNull
    public PropertyMenu<E> set(@NotNull Button<E> button, int slot) {
        this.getInventory().setItem(slot, button.item());
        return this.set(button.property(), slot);
    }

    /**
     * Modifies a property located at a given slot with given operations to perform.
     *
     * @param applyProperty Method that'll take a slot property object as an argument and perform operations on it
     * @param slot          Slot at which a property that is being modified is located at
     * @return This instance, useful for chaining
     * @throws IndexOutOfBoundsException If the slot argument is out of this inventory's array boundaries
     * @throws IllegalArgumentException  If the {@link Consumer} argument is null
     */
    @NonNull
    public PropertyMenu<E> changeProperty(@NonNull Consumer<E> applyProperty, int slot) {
        this.getProperty(slot).ifPresent(applyProperty);
        return this;
    }

    /**
     * Clears the whole inventory array of slot properties.
     *
     * @return This instance, useful for chaining
     */
    @NonNull
    public PropertyMenu<E> clearProperties() {
        Arrays.fill(this.properties, null);
        return this;
    }

    /**
     * Clears the whole inventory of its contents and slot properties.
     *
     * @return This instance, useful for chaining
     */
    @NonNull
    @Override
    public PropertyMenu<E> clear() {
        super.clear();
        return this.clearProperties();
    }

    /**
     * Returns a property stored at the given slot of this inventory menu or null if it doesn't exist.
     *
     * @param slot Slot index location of the Object in the inventory
     * @return {@link Optional} of nullable Object
     * @throws IndexOutOfBoundsException If the given slot argument is out of this inventory's boundaries
     */
    @NonNull
    public Optional<E> getProperty(int slot) {
        return Optional.ofNullable(this.properties[slot]);
    }

    /**
     * Returns an iterator over the properties in this menu.
     * <p>
     * The iterator provides sequential access to the properties stored in the menu. It allows for the removal of
     * elements during iteration but does not support addition.
     *
     * @return A {@link PropertyIterator} instance for this menu
     */
    @NonNull
    @Override
    public Iterator<E> iterator() {
        return new PropertyIterator();
    }

    /**
     * Returns a {@link Spliterator} over the properties in this menu.
     *
     * @return A {@link Spliterator} for the properties in this menu
     */
    @Override
    public Spliterator<E> spliterator() {
        return Arrays.spliterator(this.properties);
    }

    /**
     * A bidirectional iterator for the properties stored in the {@link PropertyMenu}.
     * <p>
     * This iterator provides sequential and reverse access to the properties of the menu and allows elements to be
     * modified or removed during iteration. Adding new elements isn't supported, but setting them is.
     */
    class PropertyIterator implements ListIterator<E> {

        private int cursor = -1;

        /**
         * Checks if there are more properties to iterate over in the forward direction.
         *
         * @return {@code true} if there are more properties, {@code false} otherwise
         */
        @Override
        @Contract(pure = true)
        public boolean hasNext() {
            return this.cursor + 1 < PropertyMenu.this.properties.length;
        }

        /**
         * Returns the next property in the iteration.
         *
         * @return The next property
         * @throws NoSuchElementException If no more properties exist in the menu
         */
        @Override
        public E next() {
            if (!this.hasNext()) {
                throw new NoSuchElementException("End of menu property iterator reached at index " + (this.cursor + 1));
            }

            return PropertyMenu.this.properties[++this.cursor];
        }

        /**
         * Checks if there are more properties to iterate over in the reverse direction.
         *
         * @return {@code true} if there are more properties in reverse, {@code false} otherwise
         */
        @Override
        @Contract(pure = true)
        public boolean hasPrevious() {
            return this.cursor >= 0;
        }

        /**
         * Returns the previous property in the iteration.
         *
         * @return The previous property
         * @throws NoSuchElementException If no more properties exist in reverse
         */
        @Override
        public E previous() {
            if (!this.hasPrevious()) {
                throw new NoSuchElementException("Start of menu property reached");
            }

            return PropertyMenu.this.properties[this.cursor--];
        }

        /**
         * Returns the index of the next property in the iteration.
         *
         * @return The index of the next property, or the size of the menu if at the end
         */
        @Override
        @Contract(pure = true)
        public int nextIndex() {
            return this.cursor + 1;
        }

        /**
         * Returns the index of the previous property in the iteration.
         *
         * @return The index of the previous property, or -1 if at the start.
         */
        @Override
        @Contract(pure = true)
        public int previousIndex() {
            return this.cursor;
        }

        /**
         * Removes the last property returned by this iterator from the menu.
         * <p>
         * The property at the last returned index will be set to {@code null}.
         *
         * @throws IllegalStateException If {@link #next()} or {@link #previous()} has not been called, or if this has
         *                               already been called after the last retrieval
         */
        @Override
        public void remove() {
            if (this.cursor < 0 || PropertyMenu.this.properties[this.cursor] == null) {
                throw new IllegalStateException(
                        "Cannot remove: either next() or previous() has not been called or the element is already null");
            }

            PropertyMenu.this.properties[this.cursor] = null;
        }

        /**
         * Replaces the last property returned by this iterator with the specified element.
         *
         * @param element The element to replace the last returned property with
         * @throws IllegalStateException If {@link #next()} or {@link #previous()} has not been called
         */
        @Override
        public void set(E element) {
            if (this.cursor < 0 || this.cursor >= PropertyMenu.this.properties.length) {
                throw new IllegalStateException("Cannot set element because next() or previous() has not been called");
            }

            PropertyMenu.this.properties[this.cursor] = element;
        }

        /**
         * Adds a new property to the menu at the current cursor position.
         *
         * @param element The element to be added
         * @throws UnsupportedOperationException Always, as adding new elements is not supported
         */
        @Override
        @Contract("_ -> fail")
        public void add(E element) {
            throw new UnsupportedOperationException("Shifting menu properties isn't supported");
        }
    }
}