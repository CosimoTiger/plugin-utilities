package com.cosimo.utilities.menu;

import com.google.common.base.Preconditions;
import org.bukkit.inventory.Inventory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Implementation of {@link Menu} with an {@link Object} array of the same size as the inventory, with many methods for
 * working with these properties.
 *
 * @param <E> Single object type to be stored in the slots of this menu inventory
 * @author CosimoTiger
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
     * @param inventory Not null {@link Inventory} that will be wrapped and controlled by an {@link AbstractMenu}
     */
    public PropertyMenu(@Nonnull Inventory inventory) {
        super(inventory);
    }

    public Menu setIf(@Nullable E property, @Nonnull BiPredicate<E, Integer> propertySlotPredicate, int start, int end, int step) {
        Preconditions.checkArgument(step != 0, "step argument (" + step + ") can't be 0");

        for (int slot = start; slot < end; slot += step) {
            final int copy = slot;

            if (this.getProperty(slot)
                    .map(p -> propertySlotPredicate.test(p, copy))
                    .orElse(false)) {
                this.set(property, slot);
            }
        }

        return this;
    }

    @Nonnull
    public Menu setIf(@Nullable E property, @Nonnull BiPredicate<E, Integer> propertySlotPredicate, int start, int end) {
        return this.setIf(property, propertySlotPredicate, start, end, 1);
    }

    @Nonnull
    public Menu setIf(@Nullable E property, @Nonnull BiPredicate<E, Integer> propertySlotPredicate, int start) {
        return this.setIf(property, propertySlotPredicate, start, this.getInventory().getSize());
    }

    @Nonnull
    public Menu setIf(@Nullable E property, @Nonnull BiPredicate<E, Integer> propertySlotPredicate) {
        return this.setIf(property, propertySlotPredicate, 0);
    }

    @Nonnull
    public PropertyMenu<E> setIf(@Nullable E property, @Nonnull Predicate<E> propertyPredicate, int start, int end, int step) {
        Preconditions.checkArgument(step != 0, "step argument (" + step + ") can't be 0");

        for (int slot = start; slot < end; slot += step) {
            if (this.getProperty(slot)
                    .map(propertyPredicate::test)
                    .orElse(false)) {
                this.set(property, slot);
            }
        }

        return this;
    }

    @Nonnull
    public PropertyMenu<E> setIf(@Nullable E property, @Nonnull Predicate<E> propertyPredicate, int start, int end) {
        return this.setIf(property, propertyPredicate, start, end, 1);
    }

    @Nonnull
    public PropertyMenu<E> setIf(@Nullable E property, @Nonnull Predicate<E> propertyPredicate, int start) {
        return this.setIf(property, propertyPredicate, start, this.getInventory().getSize());
    }

    @Nonnull
    public PropertyMenu<E> setIf(@Nullable E property, @Nonnull Predicate<E> propertyPredicate) {
        return this.setIf(property, propertyPredicate, 0);
    }

    @Nonnull
    public PropertyMenu<E> setRange(@Nullable E property, int start, int end, int step) {
        Preconditions.checkArgument(step != 0, "step argument (" + step + ") can't be 0");

        for (int slot = start; slot < end; slot += step) {
            this.set(property, slot);
        }

        return this;
    }

    @Nonnull
    public PropertyMenu<E> setRange(@Nullable E property, int start, int end) {
        return this.setRange(property, start, end, 1);
    }

    @Nonnull
    public PropertyMenu<E> setRange(@Nullable E property, int start) {
        return this.setRange(property, start, this.getInventory().getSize());
    }

    /**
     * Modifies a property located at a given slot with given operations to perform.
     *
     * @param applyProperty Lambda method that'll take a slot property object as an argument and perform operations on
     *                      it
     * @param slot          Slot at which a property that is being modified is located at
     * @return This instance, useful for chaining
     * @throws IndexOutOfBoundsException If the slot argument is out of this inventory's array boundaries
     * @throws IllegalArgumentException  If the {@link Consumer} argument is null
     */
    @Nonnull
    public PropertyMenu<E> changeProperty(@Nonnull Consumer<E> applyProperty, int slot) {
        Preconditions.checkArgument(applyProperty != null, "Consumer<E> argument can't be null");
        this.getProperty(slot).ifPresent(applyProperty);
        return this;
    }

    /**
     * Sets a slot property object at the given inventory {@link AbstractMenu} slots.
     *
     * @param property Property {@link Object}
     * @param slots    Slots that these properties will belong to
     * @return This instance, useful for chaining
     * @throws IllegalArgumentException  If the array of slots is null
     * @throws IndexOutOfBoundsException If a slot in the slot array argument is out of this inventory's boundaries
     */
    @Nonnull
    public PropertyMenu<E> set(@Nullable E property, @Nonnull Iterable<Integer> slots) {
        Preconditions.checkArgument(slots != null, "Array of slots can't be null");
        slots.forEach(slot -> this.properties[slot] = property);
        return this;
    }

    /**
     * Sets a slot property object at the given inventory {@link AbstractMenu} slot(s).
     *
     * @param property Property {@link Object}
     * @param slots    Slots that these properties will belong to
     * @return This instance, useful for chaining
     * @throws IllegalArgumentException  If the array of slots is null
     * @throws IndexOutOfBoundsException If a slot in the slot array argument is out of this inventory's boundaries
     */
    @Nonnull
    public PropertyMenu<E> set(@Nullable E property, @Nonnull int... slots) {
        Preconditions.checkArgument(slots != null, "Array of slots can't be null");

        for (int slot : slots) {
            this.properties[slot] = property;
        }

        return this;
    }

    /**
     * Clears the whole inventory array of slot properties.
     *
     * @return This instance, useful for chaining
     */
    @Nonnull
    public PropertyMenu<E> clearProperties() {
        this.properties = (E[]) new Object[this.getInventory().getSize()];
        return this;
    }

    /**
     * Clears the whole inventory of its contents and slot properties.
     *
     * @return This instance, useful for chaining
     */
    @Nonnull
    @Override
    public PropertyMenu<E> clear() {
        this.clearContents();
        return this.clearProperties();
    }

    /**
     * Returns a property stored at the given slot of this inventory menu or null if it doesn't exist.
     *
     * @param slot Slot index location of the Object in the inventory
     * @return {@link Optional} of nullable Object
     * @throws IndexOutOfBoundsException If the given slot argument is out of this inventory's boundaries
     */
    @Nonnull
    public Optional<E> getProperty(int slot) {
        return Optional.ofNullable(this.properties[slot]);
    }
}