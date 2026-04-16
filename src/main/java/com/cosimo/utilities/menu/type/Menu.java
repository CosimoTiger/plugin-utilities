package com.cosimo.utilities.menu.type;

import com.cosimo.utilities.menu.type.action.ActionMenu;
import org.bukkit.inventory.Inventory;
import org.jspecify.annotations.NullMarked;

/**
 * The default instantiable class of the {@link AbstractMenu} class without the hassle of using generics.
 *
 * @author CosimoTiger
 * @see ActionMenu
 */
@NullMarked
public class Menu extends AbstractMenu<Menu> {

    /**
     * Creates a new {@link Menu} using the default constructor for {@link AbstractMenu}.
     *
     * @param inventory Non-null {@link Inventory} to wrap around and control as a menu
     */
    public Menu(Inventory inventory) {
        super(inventory);
    }
}