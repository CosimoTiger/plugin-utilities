package com.cosimo.utilities.menu.menus;

import com.cosimo.utilities.menu.MenuManager;
import com.google.common.base.Preconditions;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;

import javax.annotation.Nonnull;

/**
 * Subclass that stores a {@link MenuManager} and uses it where needed to reduce the amount of method arguments.
 */
public class ManagedMenu extends AbstractMenu {

    /**
     * A {@link MenuManager} instance that is available for the control of this {@link AbstractMenu}.
     */
    private MenuManager menuManager;

    /**
     * The default constructor for all subclasses.
     *
     * @param inventory   Not null {@link Inventory} that will be wrapped and controlled by an {@link AbstractMenu}
     * @param menuManager Not null {@link MenuManager} that will be used for (un)registering this {@link AbstractMenu}
     *                    and passing events to it
     * @throws IllegalArgumentException If the {@link Inventory} or {@link MenuManager} argument is null
     */
    public ManagedMenu(@Nonnull Inventory inventory, @Nonnull MenuManager menuManager) {
        super(inventory);
        Preconditions.checkArgument(menuManager != null, "MenuManager argument can't be null");
        this.menuManager = menuManager;
    }

    @Nonnull
    public ManagedMenu open(@Nonnull Iterable<? extends HumanEntity> viewers) {
        return (ManagedMenu) this.open(this.getManager(), viewers);
    }

    @Nonnull
    public ManagedMenu open(@Nonnull HumanEntity... viewers) {
        return (ManagedMenu) this.open(this.getManager(), viewers);
    }

    /**
     * Unregisters this {@link AbstractMenu} from its previous {@link MenuManager}, sets and registers itself to the
     * new, given {@link MenuManager}.
     *
     * @param menuManager Not null {@link MenuManager} that this {@link AbstractMenu} will switch over to
     * @return Previous {@link MenuManager} of this {@link AbstractMenu}
     * @throws IllegalArgumentException If the {@link MenuManager} argument is null
     */
    @Nonnull
    public AbstractMenu setManager(@Nonnull MenuManager menuManager) {
        Preconditions.checkArgument(menuManager != null, "MenuManager argument can't be null");

        this.menuManager.unregisterMenu(this);
        this.menuManager = menuManager;
        menuManager.registerMenu(this);

        return this;
    }

    /**
     * Returns the {@link MenuManager} that is used by this {@link AbstractMenu} to (un)register itself, listen to
     * events and access other {@link AbstractMenu}s.
     *
     * @return The {@link MenuManager} this inventory is or will be registered in
     */
    @Nonnull
    public MenuManager getManager() {
        return this.menuManager;
    }
}