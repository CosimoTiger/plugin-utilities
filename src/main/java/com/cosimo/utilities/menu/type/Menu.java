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
public class Menu extends AbstractMenu<Menu> {

    /**
     * {@inheritDoc} Creates a new {@link Menu} using the default constructor for {@link AbstractMenu}.
     */
    public Menu(@NonNull Inventory inventory) {
        super(inventory);
    }
}