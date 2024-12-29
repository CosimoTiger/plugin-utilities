package com.cosimo.utilities.menu;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a {@link Button} in a menu, associating an optional {@link ItemStack} with an optional property of type
 * {@code E}, as used in the parameterized {@link com.cosimo.utilities.menu.type.PropertyMenu}.
 *
 * <p>It's a composition of the two, whose constituents are separated and the instance is typically left unused in the
 * {@link com.cosimo.utilities.menu.type.AbstractMenu} setters. Its main goal is to reduce the need for an
 * implementation for every slot type.</p>
 *
 * @param <E> the type of the associated property, such as an action or slot metadata
 */
public record Button<E>(@Nullable ItemStack item, @Nullable E property) {

    /**
     * Creates a {@link Button} with an item and no associated property.
     *
     * @param item the {@link ItemStack} associated with the {@link Button}, or {@code null} if no item
     */
    public Button(@Nullable ItemStack item) {
        this(item, null);
    }

    /**
     * Creates a {@link Button} with a property and no associated item.
     *
     * @param property the property associated with the {@link Button}, or {@code null} if no property
     */
    public Button(@Nullable E property) {
        this(null, property);
    }

    /**
     * Creates a {@link Button} with both an item and a property.
     *
     * @param item     the {@link ItemStack} associated with the {@link Button}, or {@code null} if no item
     * @param property the property associated with the {@link Button}, or {@code null} if no property
     * @param <E>      the type of the property
     * @return a new {@link Button} instance
     */
    @Contract("_, _ -> new")
    public static <E> Button<E> of(@Nullable ItemStack item, @Nullable E property) {
        return new Button<>(item, property);
    }

    /**
     * Creates a {@link Button} with an item and no associated property.
     *
     * @param item the {@link ItemStack} associated with the {@link Button}, or {@code null} if no item
     * @param <E>  the type of the property
     * @return a new {@link Button} instance
     */
    @Contract("_ -> new")
    public static <E> Button<E> of(@Nullable ItemStack item) {
        return new Button<>(item);
    }

    /**
     * Creates a {@link Button} with a property and no associated item.
     *
     * @param property the property associated with the {@link Button}, or {@code null} if no property
     * @param <E>      the type of the property
     * @return a new {@link Button} instance
     */
    @Contract("_ -> new")
    public static <E> Button<E> of(@Nullable E property) {
        return new Button<>(property);
    }

    /**
     * Creates an empty {@link Button} with no item and no property.
     *
     * @param <E> the type of the property
     * @return a new empty {@link Button} instance
     */
    @Contract("-> new")
    public static <E> Button<E> empty() {
        return new Button<>(null, null);
    }
}
