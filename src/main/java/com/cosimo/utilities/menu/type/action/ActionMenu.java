package com.cosimo.utilities.menu.type.action;

import com.cosimo.utilities.menu.type.AbstractMenu;
import com.cosimo.utilities.menu.type.Menu;
import com.cosimo.utilities.menu.util.Menus;
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
import java.util.function.Function;

/**
 * Implementation of {@link Menu} with an {@link MenuAction} array of the same size as the {@link Inventory}, providing
 * methods for associating and managing actions with {@link Inventory} slots.
 *
 * @see MenuAction
 */
@SuppressWarnings("unused")
public class ActionMenu extends AbstractMenu<ActionMenu> implements Iterable<MenuAction> {

    private final MenuAction[] actions = new MenuAction[this.getInventory().getSize()];

    /**
     * Creates a new {@link ActionMenu}, initializing an action array of the same size as the {@link Inventory}.
     *
     * @param inventory A non-null {@link Inventory} to be controlled by this {@link ActionMenu}
     */
    public ActionMenu(@NonNull Inventory inventory) {
        super(inventory);
    }

    /**
     * {@inheritDoc} If a {@link MenuAction} is set for the clicked slot, it will be invoked with the
     * {@link InventoryClickEvent}.
     *
     * @param event The {@link InventoryClickEvent} triggering the action
     */
    @Override
    public void onClick(@NonNull InventoryClickEvent event) {
        super.onClick(event);

        if (Menus.isClickInsideInventory(event)) {
            this.getAction(event.getSlot()).ifPresent(action -> action.accept(event, this));
        }
    }

    /**
     * Retrieves the action associated with a specific slot in this menu.
     *
     * @param slot The index of the slot in the {@link Inventory}
     * @return An {@link Optional} containing the {@link MenuAction} if one is set, or empty if no action exists
     * @throws IndexOutOfBoundsException If the slot index is out of the {@link Inventory}'s boundaries
     */
    @NonNull
    @Contract(pure = true)
    public Optional<MenuAction> getAction(int slot) {
        return Optional.ofNullable(this.actions[slot]);
    }

    /**
     * Returns an iterator over all {@link MenuAction}s in this menu, including null values.
     *
     * @return An iterator over the actions in this menu
     */
    @NonNull
    @Override
    public Iterator<MenuAction> iterator() {
        return new ActionMenuIterator();
    }

    /**
     * Returns a {@link Spliterator} for traversing the {@link MenuAction}s in this menu.
     *
     * @return A {@link Spliterator} for the actions in this menu
     */
    @NonNull
    @Override
    public Spliterator<MenuAction> spliterator() {
        return Arrays.spliterator(this.actions);
    }

    /**
     * Sets an item and action for multiple slots defined by a slot array function.
     *
     * @param item          The {@link ItemStack} to set in the slots
     * @param action        The {@link MenuAction} to associate with the slots
     * @param slotsFunction A function that generates an array of slot indices
     * @return This menu instance, useful for chaining
     * @throws NullPointerException      If the {@code slots} argument is {@code null}
     * @throws IndexOutOfBoundsException If any slot is out of the {@link Inventory}'s boundaries
     */
    @NonNull
    @Contract(mutates = "this")
    public ActionMenu set(@Nullable ItemStack item, @Nullable MenuAction action,
                          @NonNull Function<ActionMenu, int[]> slotsFunction) {
        return this.set(item, action, slotsFunction.apply(this));
    }

    /**
     * Sets an item and action for multiple slots defined by an iterable of slot indices.
     *
     * @param item   The {@link ItemStack} to set in the slots
     * @param action The {@link MenuAction} to associate with the slots
     * @param slots  An iterable of slot indices
     * @return This menu instance, useful for chaining
     * @throws NullPointerException      If the {@code slots} argument is {@code null}
     * @throws IndexOutOfBoundsException If any slot in the iterable is out of the {@link Inventory}'s boundaries
     */
    @NonNull
    @Contract(mutates = "this")
    public ActionMenu set(@Nullable ItemStack item, @Nullable MenuAction action,
                          @NonNull Iterable<@NonNull Integer> slots) {
        return this.set(item, slots).set(action, slots);
    }

    /**
     * Sets an item and action for specific slot indices.
     *
     * @param item   The {@link ItemStack} to set in the slots
     * @param action The {@link MenuAction} to associate with the slots
     * @param slots  The slot indices where the item and action will be set
     * @return This menu instance, useful for chaining
     * @throws NullPointerException If the {@code slots} argument is {@code null}
     */
    @NonNull
    @Contract(mutates = "this")
    public ActionMenu set(@Nullable ItemStack item, @Nullable MenuAction action, int @NonNull ... slots) {
        return this.set(item, slots).set(action, slots);
    }

    /**
     * Sets both an {@link ItemStack} and a {@link MenuAction} at a specific slot in this {@link ActionMenu}.
     *
     * @param item   The {@link ItemStack} to be placed in the slot, or {@code null} to clear the item
     * @param action The {@link MenuAction} to associate with the slot, or {@code null} to remove any existing action
     * @param slot   The slot index where the item and action will be set
     * @return This menu instance, useful for chaining
     * @throws IndexOutOfBoundsException If the slot index is out of the {@link Inventory}'s boundaries
     */
    @NonNull
    @Contract(mutates = "this")
    public ActionMenu set(@Nullable ItemStack item, @Nullable MenuAction action, int slot) {
        this.getInventory().setItem(slot, item);
        return this.set(action, slot);
    }

    /**
     * Sets a {@link MenuAction} for all slots determined by a {@link Function} that produces an array of slot indices.
     *
     * @param action        The {@link MenuAction} to associate with the slots, or {@code null} to clear existing
     *                      actions
     * @param slotsFunction A {@link Function} that generates an array of slot indices for this menu
     * @return This menu instance, useful for chaining
     * @throws NullPointerException      If the {@code slots} argument is {@code null}
     * @throws IndexOutOfBoundsException If any slot is out of the {@link Inventory}'s boundaries
     */
    public ActionMenu set(@Nullable MenuAction action, @NonNull Function<ActionMenu, int[]> slotsFunction) {
        return this.set(action, slotsFunction.apply(this));
    }

    /**
     * Sets a {@link MenuAction} for multiple slots specified by an {@link Iterable} of slot indices.
     *
     * @param action The {@link MenuAction} to associate with the slots, or {@code null} to clear existing actions
     * @param slots  An {@link Iterable} of slot indices where the action will be set
     * @return This menu instance, useful for chaining
     * @throws NullPointerException      If the {@code slots} argument is {@code null}
     * @throws IndexOutOfBoundsException If any slot in the {@link Iterable} is out of the {@link Inventory}'s
     *                                   boundaries
     */
    @NonNull
    @Contract(mutates = "this")
    public ActionMenu set(@Nullable MenuAction action, @NonNull Iterable<@NonNull Integer> slots) {
        slots.forEach(slot -> this.set(action, slot));
        return this;
    }

    /**
     * Sets a {@link MenuAction} for multiple slots specified by an array of slot indices.
     *
     * @param action The {@link MenuAction} to associate with the slots, or {@code null} to clear existing actions
     * @param slots  An array of slot indices where the action will be set
     * @return This menu instance, useful for chaining
     * @throws NullPointerException      If the {@code slots} argument is {@code null}
     * @throws IndexOutOfBoundsException If any slot in the array is out of the {@link Inventory}'s boundaries
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
     * Sets a {@link MenuAction} at the given slot index.
     *
     * @param action The {@link MenuAction} to associate with the slot, or {@code null} to remove any existing action
     * @param slot   The slot where the {@link MenuAction} will be set
     * @return This instance, useful for chaining
     * @throws IndexOutOfBoundsException If the slot index is out of the {@link Inventory}'s boundaries
     */
    @NonNull
    @Contract(mutates = "this")
    public ActionMenu set(@Nullable MenuAction action, int slot) {
        this.actions[slot] = action;
        return this;
    }

    /**
     * Clears all slot actions in the menu, leaving the {@link Inventory} contents unchanged
     *
     * @return This menu instance, useful for chaining
     */
    @NonNull
    @Contract(mutates = "this")
    public ActionMenu clearActions() {
        Arrays.fill(this.actions, null);
        return this;
    }

    /**
     * Clears all slot actions and items in the menu.
     *
     * @return This menu instance, useful for chaining
     */
    @NonNull
    @Override
    @Contract(mutates = "this")
    public ActionMenu clear() {
        return super.clear().clearActions();
    }

    /**
     * A bidirectional iterator for the actions stored in the {@link ActionMenu}.
     * <p>
     * This iterator provides sequential and reverse access to the actions of the menu and allows elements to be
     * modified or removed during iteration. Adding or removing elements isn't supported, but setting them is.
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