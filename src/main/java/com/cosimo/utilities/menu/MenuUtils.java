package com.cosimo.utilities.menu;

import lombok.NonNull;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

/**
 * Contains common {@link IMenu} and {@link AbstractMenu} logic.
 */
public class MenuUtils {

    public static boolean isAboutToBecomeDisposable(@NonNull InventoryCloseEvent event) {
        return event.getViewers().size() < 2;
    }

    public static boolean isInventoryViewItemMixingAction(@NonNull InventoryAction action) {
        return action == InventoryAction.COLLECT_TO_CURSOR || action == InventoryAction.MOVE_TO_OTHER_INVENTORY;
    }

    public static boolean isClickInsideInventory(@NonNull InventoryClickEvent event) {
        return event.getInventory().equals(event.getClickedInventory());
    }

    public static boolean shouldCancelMenuClick(@NonNull InventoryClickEvent event) {
        return isInventoryViewItemMixingAction(event.getAction()) || isClickInsideInventory(event);
    }

    public static int getColumns(@NonNull Inventory inventory) {
        return switch (inventory.getType()) {
            case CHEST, ENDER_CHEST, BARREL, PLAYER, CREATIVE, SHULKER_BOX -> 9;
            case HOPPER -> 5;
            case DROPPER, DISPENSER, CRAFTER -> 3;
            case SMITHING -> 2;
            default -> inventory.getSize();
        };
    }
}