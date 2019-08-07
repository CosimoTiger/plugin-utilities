package memecat.fatcat.utilities.menu.attribute;

import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Enum constants that are used for creating menus with different sizes.
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
     * Returns a {@link Rows} enum from a given integer amount of rows, never a NULL value.
     * <p>
     * Returned {@link Rows} enum will be a row of one if the given value is lower than or equal to zero, or a row of
     * six if it's not any of them.
     *
     * @param amount Amount of rows in a chest-like inventory
     * @return {@link Rows} enum that is equal to the given amount or anything near that number
     */
    @NotNull
    public static Rows fromAmount(int amount) {
        for (Rows rows : values()) {
            if (rows.getAmount() == amount) {
                return rows;
            }
        }

        if (amount <= 0) {
            return Rows.ONE;
        }

        return Rows.SIX;
    }

    /**
     * Returns a {@link Rows} enum that belongs to an inventory type.
     * <p>
     * The returned {@link Rows} enum will most likely be NULL because the inventory types SHULKER_BOX, ENDER_CHEST, CHEST and
     * BARREL are the only ones that support rows and columns properly.
     *
     * @param type Inventory type
     * @return {@link Rows} enum or NULL
     */
    @NotNull
    public static Optional<Rows> fromType(@NotNull InventoryType type) {
        if (type != null) {
            switch (type) {
                case SHULKER_BOX:
                case ENDER_CHEST:
                case BARREL:
                case CHEST:
                    return fromSize(type.getDefaultSize());
            }
        }

        return Optional.empty();
    }

    /**
     * Returns a {@link Rows} enum that belongs to a inventory size.
     *
     * @param size Inventory slot amount
     * @return {@link Rows} enum or NULL
     */
    @NotNull
    public static Optional<Rows> fromSize(int size) {
        for (Rows rows : values()) {
            if (rows.getSize() == size) {
                return Optional.of(rows);
            }
        }

        return Optional.empty();
    }

    /**
     * Returns the integer amount of rows.
     *
     * @return Amount of rows
     */
    public int getAmount() {
        return amount;
    }

    /**
     * Returns the size of an inventory depending on the amount of rows.
     * <p>
     * Slot amount or size is calculated as rows × 9.
     *
     * @return Amount of inventory slots
     */
    public int getSize() {
        return size;
    }
}