package com.cosimo.utilities.menu;

import com.cosimo.utilities.menu.manager.MenuManager;
import lombok.NonNull;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.Inventory;

import java.util.List;

/**
 * Interface which is a collection of minimum methods a {@link MenuManager} needs to work with menus.
 */
public interface IMenu extends InventoryListener {

    /**
     * {@inheritDoc}
     *
     * <p>By default, {@link InventoryAction#COLLECT_TO_CURSOR} and {@link InventoryAction#MOVE_TO_OTHER_INVENTORY}
     * and any action on the menu are cancelled, but interaction with one's own inventory is allowed.
     */
    @Override
    default void onClick(@NonNull InventoryClickEvent event, boolean external) {
        final var action = event.getAction();

        if (action == InventoryAction.COLLECT_TO_CURSOR || action == InventoryAction.MOVE_TO_OTHER_INVENTORY ||
            !external) {
            event.setCancelled(true);
        }
    }

    /**
     * Acts as an event handler for the inventory item dragging event.
     *
     * <p>By default, any item dragging in this {@link AbstractMenu} will be cancelled.
     *
     * @param event {@link InventoryDragEvent} event
     */
    @Override
    default void onDrag(@NonNull InventoryDragEvent event) {
        if (event.getRawSlots()
                .stream()
                .anyMatch(rawSlot -> this.getInventory().equals(event.getView().getInventory(rawSlot)))) {
            event.setCancelled(true);
        }
    }

    /**
     * Event handler for the {@link PluginDisableEvent} of the {@link MenuManager} this {@link IMenu} is usually
     * registered in.
     *
     * <p>During server plugin reloads or restarts, this is the last event to be passed to this {@link IMenu} by a
     * {@link MenuManager}, after which the inventory can't really be controlled, so it is suggested to close it for all
     * {@link Inventory#getViewers()}.</p>
     *
     * @param event {@link PluginDisableEvent} event
     */
    default void onDisable(@NonNull PluginDisableEvent event) {
        this.close();
    }

    /**
     * Closes all {@link Inventory} of this instance for all viewers who are viewing it.
     *
     * <p><strong>Schedule this action for the next tick if you're running it inside an inventory event
     * handler.</strong>
     *
     * @return This instance, useful for chaining
     */
    @NonNull
    default IMenu close() {
        List.copyOf(this.getInventory().getViewers()).forEach(HumanEntity::closeInventory);
        return this;
    }

    /**
     * Returns the {@link Inventory} that this instance is controlling, should always be the same.
     *
     * @return Non-null {@link Inventory}
     */
    @NonNull Inventory getInventory();
}