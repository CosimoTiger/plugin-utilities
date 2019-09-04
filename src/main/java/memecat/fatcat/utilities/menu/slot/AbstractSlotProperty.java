package memecat.fatcat.utilities.menu.slot;

import org.jetbrains.annotations.NotNull;
import memecat.fatcat.utilities.menu.menus.AbstractMenu;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * This type is used by {@link memecat.fatcat.utilities.menu.menus.PropertyMenu} to store an action or even a value at
 * specific slots and react to an {@link InventoryClickEvent} at the slot. Of course, developers are able to implement
 * this for their own way and usage.
 *
 * @see SlotProperty
 */
public interface AbstractSlotProperty {

    /**
     * Runs an action with a given {@link InventoryClickEvent} and {@link AbstractMenu} argument.
     *
     * @param event {@link InventoryClickEvent} inventory event
     * @param menu  {@link AbstractMenu} that the {@link InventoryClickEvent} is referring to
     */
    void run(@NotNull InventoryClickEvent event, @NotNull AbstractMenu menu);
}