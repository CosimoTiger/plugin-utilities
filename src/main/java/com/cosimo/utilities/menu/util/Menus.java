package com.cosimo.utilities.menu.util;

import com.cosimo.utilities.menu.IMenu;
import com.cosimo.utilities.menu.type.AbstractMenu;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;

/**
 * Contains utility methods and common logic for working with {@link IMenu} and {@link AbstractMenu}, handling
 * menu-related events, determining inventory states, and performing inventory-related calculations.
 */
@NullMarked
public class Menus {

    public static final int MAX_CHEST_ROWS = 6;
    public static final int CHEST_COLUMNS = 9;

    /**
     * Determines if the inventory in the given {@link InventoryCloseEvent} handler is about to become disposable.
     *
     * <p>An inventory is considered disposable if it has fewer than two viewers remaining.</p>
     *
     * @param event the {@link InventoryCloseEvent} to check
     * @return {@code true} if the inventory is about to become disposable, {@code false} otherwise
     */
    @Contract(pure = true)
    @ApiStatus.Internal
    public static boolean isAboutToBecomeDisposable(InventoryCloseEvent event) {
        return event.getViewers().size() < 2;
    }

    /**
     * Checks if the given {@link InventoryAction} involves mixing items between inventories in an inventory view.
     *
     * <p>Examples of such actions include collecting items to the cursor or moving items to another inventory.</p>
     *
     * @param action the {@link InventoryAction} to check
     * @return {@code true} if the action involves item mixing, {@code false} otherwise
     */
    @Contract(pure = true)
    public static boolean isInventoryViewItemMixingAction(InventoryAction action) {
        return action == InventoryAction.COLLECT_TO_CURSOR || action == InventoryAction.MOVE_TO_OTHER_INVENTORY;
    }

    /**
     * Determines if the click event occurred inside the inventory.
     *
     * @param event the {@link InventoryClickEvent} to check
     * @return {@code true} if the click was inside the inventory, {@code false} otherwise
     */
    @Contract(pure = true)
    public static boolean isClickInsideInventory(InventoryClickEvent event) {
        return event.getInventory().equals(event.getClickedInventory());
    }

    /**
     * Determines if the given {@link InventoryClickEvent} should result in the menu click being canceled.
     *
     * <p>Clicks are typically canceled if they involve item-mixing actions or occur inside the inventory.</p>
     *
     * @param event the {@link InventoryClickEvent} to evaluate
     * @return {@code true} if the click should be canceled, {@code false} otherwise
     */
    @Contract(pure = true)
    @ApiStatus.Internal
    public static boolean shouldCancelMenuClick(InventoryClickEvent event) {
        return isInventoryViewItemMixingAction(event.getAction()) || isClickInsideInventory(event);
    }

    /**
     * Retrieves the number of columns in the specified {@link Inventory}, based on its type.
     *
     * <p>Standard chest-like inventories typically have {@code 9} columns, hoppers {@code 5}, dropper-like
     * inventories {@code 3}, and most others are invalid, though they default to their size.</p>
     *
     * @param inventory the {@link Inventory} to analyze
     * @return the number of columns in the inventory
     */
    @Contract(pure = true)
    public static int getColumns(Inventory inventory) {
        return switch (inventory.getType()) {
            case CHEST, ENDER_CHEST, BARREL, PLAYER, SHULKER_BOX -> 9;
            case DROPPER, DISPENSER, CRAFTER -> 3;
            case SMITHING -> 2;
            default -> inventory.getSize();
        };
    }

    /**
     * Calculates the number of rows required to fit a specified number of items in a {@link Inventory} with a limited
     * number of columns, e.g., a hopper or a chest with left or right padding.
     *
     * @param count   the number of items to fit into the chest
     * @param columns the number of columns in the chest
     * @return the number of rows required, clamped between 1 and {@link #MAX_CHEST_ROWS}
     */
    @Contract(pure = true)
    public static int getChestRowsForCount(int count, int columns) {
        return Math.clamp((int) Math.ceil((double) count / columns), 1, MAX_CHEST_ROWS);
    }

    /**
     * Calculates the number of rows required to fit a specified number of items in a chest-like {@link Inventory}.
     *
     * @param count the number of items to fit into the chest
     * @return the number of rows required, clamped between 1 and {@link #MAX_CHEST_ROWS}
     */
    @Contract(pure = true)
    public static int getChestRowsForCount(int count) {
        return getChestRowsForCount(count, CHEST_COLUMNS);
    }

    /**
     * Computes the total {@link Inventory} size (in slots) required for a specified number of items in a chest-like
     * {@link Inventory}.
     *
     * @param count the number of items to fit into the chest
     * @return the total number of slots required
     */
    @Contract(pure = true)
    public static int getChestSizeForCount(int count) {
        return getChestRowsForCount(count) * CHEST_COLUMNS;
    }
}