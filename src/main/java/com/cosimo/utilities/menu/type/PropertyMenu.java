package com.cosimo.utilities.menu.type;

import com.cosimo.utilities.menu.AbstractMenu;
import com.cosimo.utilities.menu.Button;
import lombok.NonNull;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * Implementation of {@link Menu} with an {@link Object} array of the same size as the inventory, with many methods for
 * working with these properties.
 * <p>
 * Example:
 * {@code var menu = new PropertyMenu<DyeColor>(Bukkit.createInventory(null, InventoryType.CHEST, "Color picker"));}
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

    @NonNull
    @Override
    public Iterator<E> iterator() {
        return Arrays.stream(this.properties).iterator();
    }
}