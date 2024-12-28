package com.cosimo.utilities.menu;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public record Button<E>(@Nullable ItemStack item, @Nullable E property) {

    public Button(@Nullable ItemStack item) {
        this(item, null);
    }

    public Button(@Nullable E action) {
        this(null, action);
    }

    public static <E> Button<E> of(@Nullable ItemStack item, @Nullable E property) {
        return new Button<>(item, property);
    }

    public static <E> Button<E> of(@Nullable ItemStack item) {
        return new Button<>(item);
    }

    public static <E> Button<E> of(@Nullable E property) {
        return new Button<>(property);
    }

    public static <E> Button<E> empty() {
        return new Button<>(null, null);
    }
}