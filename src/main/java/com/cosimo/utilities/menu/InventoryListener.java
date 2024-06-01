package com.cosimo.utilities.menu;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

import javax.annotation.Nonnull;

/**
 * Extracted interface that holds all the common {@link org.bukkit.event.inventory.InventoryEvent}
 * {@link org.bukkit.event.Listener} handler methods.
 */
public interface InventoryListener {

    void onClick(@Nonnull InventoryClickEvent event, boolean external);

    void onDrag(@Nonnull InventoryDragEvent event);

    void onClose(@Nonnull InventoryCloseEvent event);

    void onOpen(@Nonnull InventoryOpenEvent event);
}