package com.cosimo.utilities.menu;

import lombok.NonNull;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

/**
 * Extracted interface that holds all the common {@link org.bukkit.event.inventory.InventoryEvent}
 * {@link org.bukkit.event.Listener} handler methods.
 */
public interface InventoryListener {
    /**
     * Event handler for the {@link InventoryClickEvent}. Useful for reacting to slot button clicks and cancelling
     * such.
     *
     * @param event {@link InventoryClickEvent}
     */
    void onClick(@NonNull InventoryClickEvent event);

    /**
     * Event handler for the {@link InventoryDragEvent}. Useful for handling dragged items
     * {@link org.bukkit.inventory.ItemStack} input.
     *
     * @param event {@link InventoryDragEvent}
     */
    void onDrag(@NonNull InventoryDragEvent event);

    /**
     * Event handler for the {@link InventoryCloseEvent}. Useful for deleting objects, reopening menus etc.
     *
     * @param event {@link InventoryCloseEvent}
     */
    void onClose(@NonNull InventoryCloseEvent event);

    /**
     * Event handler for the {@link InventoryOpenEvent}. Useful for initializing some menu components, starting tracking
     * of viewers, starting up {@link org.bukkit.scheduler.BukkitTask}s etc., generally a verification that someone
     * began a "menu session" (a concept that can be seen in other menu libraries).
     *
     * @param event {@link InventoryOpenEvent}
     */
    void onOpen(@NonNull InventoryOpenEvent event);
}