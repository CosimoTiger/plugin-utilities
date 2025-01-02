package com.cosimo.utilities.menu;

import com.cosimo.utilities.menu.type.AbstractMenu;
import com.cosimo.utilities.menu.util.MenuUtils;
import lombok.NonNull;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.Inventory;

import java.util.List;

/**
 * A collection of minimal methods that a {@link MenuManager} needs to work with menus, along with utility methods, such
 * as for rows and columns.
 */
public interface IMenu {

    /**
     * Event handler for the {@link InventoryClickEvent}. Useful for reacting to slot button clicks and cancelling
     * such.
     *
     * <p>By default, {@link InventoryAction#COLLECT_TO_CURSOR} and {@link InventoryAction#MOVE_TO_OTHER_INVENTORY}
     * and any action on the menu are cancelled, but interaction with one's own inventory is allowed.
     */
    default void onClick(@NonNull InventoryClickEvent event) {
        event.setCancelled(MenuUtils.shouldCancelMenuClick(event));
    }

    /**
     * Event handler for the {@link InventoryDragEvent}. Useful for handling dragged items
     * {@link org.bukkit.inventory.ItemStack} input.
     *
     * <p>By default, any item dragging in this {@link AbstractMenu} will be cancelled.
     *
     * @param event {@link InventoryDragEvent} event
     */
    default void onDrag(@NonNull InventoryDragEvent event) {
        if (event.getRawSlots()
                .stream()
                .anyMatch(rawSlot -> this.getInventory().equals(event.getView().getInventory(rawSlot)))) {
            event.setCancelled(true);
        }
    }

    /**
     * Event handler for the {@link InventoryCloseEvent}.
     *
     * <p>Useful for reopening menus, cleaning up objects, tasks, finalizing and serializing player settings etc.
     *
     * @param event {@link InventoryCloseEvent}
     */
    default void onClose(@NonNull InventoryCloseEvent event) {
    }

    /**
     * Event handler for the {@link InventoryOpenEvent}.
     *
     * <p>Useful for initializing some menu components, starting tracking of viewers, starting up
     * {@link org.bukkit.scheduler.BukkitTask}s etc., generally a verification that someone began a "menu session",
     * which is a concept that can be seen in other menu libraries.
     *
     * @param event {@link InventoryOpenEvent}
     */
    default void onOpen(@NonNull InventoryOpenEvent event) {
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
     * Returns the amount of columns this {@link Inventory}'s size has according to
     * {@link MenuUtils#getColumns(Inventory)}.
     *
     * @return positive integer, at least 1
     */
    default int getColumns() {
        return MenuUtils.getColumns(this.getInventory());
    }

    /**
     * Returns the column index of this {@link Inventory} given a {@link Inventory} slot index.
     *
     * @param slot positive integer, at least 0, up to {@link Inventory#getSize()} of this menu
     * @return positive integer, at least 0
     */
    default int getColumnIndex(int slot) {
        return slot % this.getColumns();
    }

    /**
     * Returns the amount of rows this {@link Inventory}'s size has by deducing from {@link #getColumns()}.
     *
     * @return positive integer, at least 1
     */
    default int getRows() {
        return this.getRowIndex(this.getInventory().getSize());
    }

    /**
     * Returns the row index of this {@link Inventory} given a {@link Inventory} slot index.
     *
     * @param slot positive integer, at least 0, up to {@link Inventory#getSize()} of this menu
     * @return positive integer, at least 0
     */
    default int getRowIndex(int slot) {
        return slot / this.getColumns();
    }

    /**
     * Returns the {@link Inventory} that this instance is controlling, should always be the same.
     *
     * @return Non-null {@link Inventory}
     */
    @NonNull
    Inventory getInventory();
}