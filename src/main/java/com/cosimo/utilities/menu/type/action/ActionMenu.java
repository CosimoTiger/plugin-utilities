package com.cosimo.utilities.menu.type.action;

import com.cosimo.utilities.menu.type.AbstractMenu;
import com.cosimo.utilities.menu.type.Menu;
import lombok.NonNull;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * Implementation of {@link Menu} with an {@link Object} array of the same size as the inventory, with methods for
 * working with these actions.
 *
 * @author CosimoTiger
 * @see AbstractMenu
 */
@SuppressWarnings("unused")
public class ActionMenu extends AbstractMenu<ActionMenu> implements Iterable<MenuAction> {

    private final MenuAction[] actions = new MenuAction[this.getInventory().getSize()];

    /**
     * Creates a new {@link ActionMenu} using the default constructor for {@link Menu}, with an array of this instance's
     * generic type with default null values.
     *
     * @param inventory Not null {@link Inventory} that will be wrapped and controlled by an {@link AbstractMenu}
     */
    public ActionMenu(@NonNull Inventory inventory) {
        super(inventory);
    }

    /**
     * {@inheritDoc} Passes the {@link InventoryClickEvent} to a {@link BiConsumer} at the clicked slot, which is run if
     * it exists, and can freely change the outcome of the same event.
     */
    @Override
    public void onClick(@NonNull InventoryClickEvent event) {
        super.onClick(event);
        this.getAction(event.getSlot()).ifPresent(action -> action.accept(event, this));
    }

    /**
     * Returns an action stored at the given slot of this inventory menu or null if it doesn't exist.
     *
     * @param slot Slot index location of the {@link Object} in the inventory
     * @return {@link Optional} of nullable {@link Object}
     * @throws IndexOutOfBoundsException If the given slot argument is out of this inventory's boundaries
     */
    @NonNull
    @Contract(pure = true)
    public Optional<MenuAction> getAction(int slot) {
        return Optional.ofNullable(this.actions[slot]);
    }

    /**
     * Returns an iterator over the actions in this menu.
     * <p>
     * The iterator provides sequential access to the actions stored in the menu. It allows for the removal of elements
     * during iteration but does not support addition.
     *
     * @return A {@link ActionMenuIterator} instance for this menu
     */
    @NonNull
    @Override
    public Iterator<MenuAction> iterator() {
        return new ActionMenuIterator();
    }

    /**
     * Returns a {@link Spliterator} over the actions in this menu.
     *
     * @return A {@link Spliterator} for the actions in this menu
     */
    @NonNull
    @Override
    public Spliterator<MenuAction> spliterator() {
        return Arrays.spliterator(this.actions);
    }

    @NonNull
    @Contract(mutates = "this")
    public ActionMenu set(@Nullable ItemStack item, @Nullable MenuAction action,
                          @NonNull Function<ActionMenu, IntStream> slotStream) {
        this.set(item, slotStream);
        return this.set(action, slotStream);
    }

    @NonNull
    @Contract(mutates = "this")
    public ActionMenu set(@Nullable ItemStack item, @Nullable MenuAction action,
                          @NonNull Iterable<@NonNull Integer> slots) {
        this.set(item, slots);
        return this.set(action, slots);
    }

    @NonNull
    @Contract(mutates = "this")
    public ActionMenu set(@Nullable ItemStack item, @Nullable MenuAction action, int @NonNull ... slots) {
        this.set(item, slots);
        return this.set(action, slots);
    }

    @NonNull
    @Contract(mutates = "this")
    public ActionMenu set(@Nullable ItemStack item, @Nullable MenuAction action, int slot) {
        this.getInventory().setItem(slot, item);
        return this.set(action, slot);
    }

    public ActionMenu set(@Nullable MenuAction action, @NonNull Function<ActionMenu, IntStream> slotStream) {
        slotStream.apply(this).forEach(slot -> this.set(action, slot));
        return this;
    }

    /**
     * Sets the given action at the given numeric slot in this {@link ActionMenu}.
     *
     * @param action {@link MenuAction}
     * @param slots  Slots that this action will be set in
     * @return This instance, useful for chaining
     * @throws IllegalArgumentException  If the array of slots is null
     * @throws IndexOutOfBoundsException If a slot in the slot array argument is out of this inventory's boundaries
     */
    @NonNull
    @Contract(mutates = "this")
    public ActionMenu set(@Nullable MenuAction action, @NonNull Iterable<@NonNull Integer> slots) {
        slots.forEach(slot -> this.set(action, slot));
        return this;
    }

    /**
     * Sets the given action at the given numeric slot in this {@link ActionMenu}.
     *
     * @param action {@link MenuAction}
     * @param slots  Slots that this action will be set in
     * @return This instance, useful for chaining
     * @throws IllegalArgumentException  If the array of slots is null
     * @throws IndexOutOfBoundsException If a slot in the slot array argument is out of this inventory's boundaries
     */
    @NonNull
    @Contract(mutates = "this")
    public ActionMenu set(@Nullable MenuAction action, int @NonNull ... slots) {
        for (int slot : slots) {
            this.set(action, slot);
        }

        return this;
    }

    /**
     * Sets the given action at the given numeric slot in this {@link ActionMenu}.
     *
     * @param action {@link MenuAction}
     * @param slot   Slot that this actions will belong to
     * @return This instance, useful for chaining
     * @throws IllegalArgumentException  If the array of slots is null
     * @throws IndexOutOfBoundsException If a slot in the slot array argument is out of this inventory's boundaries
     */
    @NonNull
    @Contract(mutates = "this")
    public ActionMenu set(@Nullable MenuAction action, int slot) {
        this.actions[slot] = action;
        return this;
    }

    /**
     * Clears the whole inventory array of slot actions.
     *
     * @return This instance, useful for chaining
     */
    @NonNull
    @Contract(mutates = "this")
    public ActionMenu clearActions() {
        Arrays.fill(this.actions, null);
        return this;
    }

    /**
     * Clears the whole inventory of its contents and slot actions.
     *
     * @return This instance, useful for chaining
     */
    @NonNull
    @Override
    @Contract(mutates = "this")
    public ActionMenu clear() {
        super.clear();
        return this.clearActions();
    }

    /**
     * A bidirectional iterator for the actions stored in the {@link ActionMenu}.
     * <p>
     * This iterator provides sequential and reverse access to the actions of the menu and allows elements to be
     * modified or removed during iteration. Adding new elements isn't supported, but setting them is.
     */
    class ActionMenuIterator implements ListIterator<MenuAction> {

        private int cursor = -1;

        /**
         * Checks if there are more actions to iterate over in the forward direction.
         *
         * @return {@code true} if there are more actions, {@code false} otherwise
         */
        @Override
        @Contract(pure = true)
        public boolean hasNext() {
            return this.cursor + 1 < ActionMenu.this.actions.length;
        }

        /**
         * Returns the next action in the iteration.
         *
         * @return The next action
         * @throws NoSuchElementException If no more actions exist in the menu
         */
        @Nullable
        @Override
        public MenuAction next() {
            if (!this.hasNext()) {
                throw new NoSuchElementException("End of menu action iterator reached at index " + (this.cursor + 1));
            }

            return ActionMenu.this.actions[++this.cursor];
        }

        /**
         * Checks if there are more actions to iterate over in the reverse direction.
         *
         * @return {@code true} if there are more actions in reverse, {@code false} otherwise
         */
        @Override
        @Contract(pure = true)
        public boolean hasPrevious() {
            return this.cursor >= 0;
        }

        /**
         * Returns the previous action in the iteration.
         *
         * @return The previous action
         * @throws NoSuchElementException If no more actions exist in reverse
         */
        @Nullable
        @Override
        public MenuAction previous() {
            if (!this.hasPrevious()) {
                throw new NoSuchElementException("Start of menu action reached");
            }

            return ActionMenu.this.actions[this.cursor--];
        }

        /**
         * Returns the index of the next action in the iteration.
         *
         * @return The index of the next action, or the size of the menu if at the end
         */
        @Override
        @Contract(pure = true)
        public int nextIndex() {
            return this.cursor + 1;
        }

        /**
         * Returns the index of the previous action in the iteration.
         *
         * @return The index of the previous action, or -1 if at the start.
         */
        @Override
        @Contract(pure = true)
        public int previousIndex() {
            return this.cursor;
        }

        /**
         * Replaces the last action returned by this iterator with the specified element.
         *
         * @param element The element to replace the last returned action with
         * @throws IllegalStateException If {@link #next()} or {@link #previous()} has not been called
         */
        @Override
        public void set(@Nullable MenuAction element) {
            if (this.cursor < 0 || this.cursor >= ActionMenu.this.actions.length) {
                throw new IllegalStateException("Cannot set element because next() or previous() has not been called");
            }

            ActionMenu.this.actions[this.cursor] = element;
        }

        /**
         * Adds a new action to the menu at the last cursor position.
         *
         * @param element The element to be added
         * @throws UnsupportedOperationException Always, as adding new actions is not supported
         */
        @Override
        @Contract("_ -> fail")
        public void add(@Nullable MenuAction element) {
            throw new UnsupportedOperationException("Can't change the size of an inventory!");
        }

        /**
         * Removes an action from the menu at the last cursor position.
         *
         * @throws UnsupportedOperationException Always, as removing actions is not supported
         */
        @Override
        @Contract("-> fail")
        public void remove() {
            throw new UnsupportedOperationException("Can't change the size of an inventory!");
        }
    }
}