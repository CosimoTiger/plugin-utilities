package memecat.fatcat.utilities.menu.slot;

import org.jetbrains.annotations.NotNull;
import memecat.fatcat.utilities.menu.menus.AbstractMenu;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * Stores slot property data and runs actions of an inventory slot in {@link AbstractMenu}s on each slot interaction.
 *
 * @see SlotProperty
 */
public abstract class AbstractSlotProperty {

    /**
     * Runs an action with a given event and menu argument.
     *
     * @param event InventoryClickEvent event
     * @param menu  Inventory menu that is related to this event
     */
    public abstract void run(@NotNull InventoryClickEvent event, @NotNull AbstractMenu menu);
}