package memecat.fatcat.utilities.menu.attribute;

import com.google.common.base.Preconditions;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Enum constants that are used for creating chest inventories that follow the pattern of having rows of 9 item slots.
 * <p>
 * Maximal row amount is 6 because at bigger inventory sizes the inventory rendering becomes glitched.
 */
public enum Rows {

    ONE(1), TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6);

    private final int amount;
    private final int size;

    Rows(int amount) {
        this.amount = amount;
        this.size = amount * 9;
    }

    /**
     * Returns the {@link Optional} of a nullable {@link Rows} that matches a given {@link InventoryType} and {@link
     * Inventory} size as an {@link Inventory} configuration.
     *
     * @param type Not null {@link InventoryType}
     * @param size Amount of inventory slots
     * @return {@link Optional} of nullable {@link Rows}
     * @throws IllegalArgumentException If {@link InventoryType} argument is null
     * @see #isChestLike(InventoryType)
     */
    @NotNull
    public static Optional<Rows> fromInventory(@NotNull InventoryType type, int size) {
        if (isChestLike(type)) {
            for (Rows rows : values()) {
                if (rows.getSize() == size) {
                    return Optional.of(rows);
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Returns the {@link Optional} of a nullable {@link Rows} that matches a given {@link Inventory}'s {@link
     * InventoryType} and {@link Inventory} size.
     *
     * @param inventory Not null {@link Inventory}
     * @return {@link Optional} of nullable {@link Rows}
     * @see #fromInventory(InventoryType, int)
     * @see #isChestLike(InventoryType)
     */
    @NotNull
    public static Optional<Rows> fromInventory(@NotNull Inventory inventory) {
        Preconditions.checkArgument(inventory != null, "Inventory argument can't be null");
        return fromInventory(inventory.getType(), inventory.getSize());
    }

    /**
     * Returns whether the given {@link InventoryType} is chest-like ({@link InventoryType}s {@link
     * InventoryType#CHEST}, {@link InventoryType#BARREL}, {@link InventoryType#ENDER_CHEST}, {@link
     * InventoryType#SHULKER_BOX}).
     *
     * @param type Not null {@link InventoryType} input
     * @return Whether the given {@link InventoryType} is chest-like
     * @throws IllegalArgumentException If {@link InventoryType} argument is null
     */
    public static boolean isChestLike(@NotNull InventoryType type) {
        Preconditions.checkArgument(type != null, "InventoryType argument can't be null");

        switch (type) {
            case CHEST:
            case BARREL:
            case ENDER_CHEST:
            case SHULKER_BOX:
                return true;
            default:
                return false;
        }
    }

    /**
     * Returns the integer amount of chest-like rows.
     *
     * @return Amount of rows
     */
    public int getAmount() {
        return amount;
    }

    /**
     * Returns the size of a chest-like inventory depending on the amount of rows.
     * <p>
     * Slot amount or size is calculated as {@link #getAmount()} × 9.
     *
     * @return Amount of inventory slots
     */
    public int getSize() {
        return size;
    }
}