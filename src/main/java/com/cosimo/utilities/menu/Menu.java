package com.cosimo.utilities.menu;

import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

/**
 * The default instantiable class of the {@link AbstractMenu} class without the hassle of using generics.
 *
 * @author CosimoTiger
 * @see PropertyMenu
 */
public class Menu extends AbstractMenu<Menu> {

    /**
     * {@inheritDoc} Creates a new {@link Menu} using the default constructor for {@link AbstractMenu}.
     */
    public Menu(@NotNull Inventory inventory) {
        super(inventory);
    }
}