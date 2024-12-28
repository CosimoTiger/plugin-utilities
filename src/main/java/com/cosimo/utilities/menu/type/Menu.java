package com.cosimo.utilities.menu.type;

import com.cosimo.utilities.menu.AbstractMenu;
import lombok.NonNull;
import org.bukkit.inventory.Inventory;

/**
 * The default instantiable class of the {@link AbstractMenu} class without the hassle of using generics.
 *
 * @author CosimoTiger
 * @see PropertyMenu
 */
public class Menu extends AbstractMenu<Menu, Object> {

    /**
     * Creates a new {@link Menu} using the default constructor for {@link AbstractMenu}.
     *
     * @param inventory Non-null {@link Inventory} to wrap around and control as a menu
     */
    public Menu(@NonNull Inventory inventory) {
        super(inventory);
    }
}