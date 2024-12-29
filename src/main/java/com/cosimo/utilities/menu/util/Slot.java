package com.cosimo.utilities.menu.util;

import com.cosimo.utilities.menu.IMenu;
import lombok.NonNull;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Contract;

/**
 * Represents the position of a slot in an {@link Inventory} or {@link IMenu} grid, as a 2D coordinate, a pair of a row
 * and column. A {@link Slot} can either be zero-indexed or one-indexed, and is used to convert the row-column position
 * format into a slot index within a given {@link Inventory} as a context.
 */
public record Slot(int row, int column, boolean isZeroIndexed) {

    /**
     * Constructor that validates the row and column indices based on the indexing scheme.
     *
     * @param row           The row of the slot position
     * @param column        The column of the slot position
     * @param isZeroIndexed Flag indicating whether the position is zero-indexed (true) or one-indexed (false)
     * @throws IllegalArgumentException if row or column are invalid for the specified indexing scheme
     */
    public Slot {
        if (row < 0 || !isZeroIndexed && row == 0) {
            throw new IllegalArgumentException(
                    "Invalid SlotPosition row given (row = %s) and (isZeroIndexed = %s)".formatted(row, isZeroIndexed));
        } else if (column < 0 || !isZeroIndexed && column == 0) {
            throw new IllegalArgumentException(
                    "Invalid SlotPosition column given (column = %s) and (isZeroIndexed = %s)".formatted(column,
                                                                                                         isZeroIndexed));
        }
    }

    /**
     * Creates a new {@link Slot} that is zero-indexed.
     *
     * @param row    The row of the slot position (starting from 0)
     * @param column The column of the slot position (starting from 0)
     * @return A new {@link Slot} instance with zero indexing
     */
    @Contract("_, _ -> new")
    public static Slot atZeroIndex(int row, int column) {
        return new Slot(row, column, true);
    }

    /**
     * Creates a new {@link Slot} that is one-indexed.
     *
     * @param row    The row of the slot position (starting from 1)
     * @param column The column of the slot position (starting from 1)
     * @return A new {@link Slot} instance with one indexing
     */
    @Contract("_, _ -> new")
    public static Slot at(int row, int column) {
        return new Slot(row, column, false);
    }

    /**
     * Converts the {@link Slot} to a corresponding slot index within the provided IMenu. The conversion depends on
     * whether the position is zero-indexed or one-indexed.
     *
     * @param menu The IMenu instance, which provides access to the inventory
     * @return The slot index corresponding to the {@link Slot} in the provided inventory
     * @throws IllegalArgumentException if the {@link Slot} exceeds the size of the inventory
     */
    @Contract(pure = true)
    public int toSlot(@NonNull IMenu menu) {
        return this.toSlot(menu.getInventory());
    }

    /**
     * Converts the {@link Slot} to a corresponding slot index within the provided Inventory. The conversion depends on
     * whether the position was created as zero-indexed or one-indexed.
     *
     * @param inventory The Inventory instance that represents the available slots
     * @return The slot index corresponding to the {@link Slot} in the provided inventory
     * @throws IllegalArgumentException if the {@link Slot} exceeds the size of the inventory
     */
    @Contract(pure = true)
    public int toSlot(@NonNull Inventory inventory) {
        final int columns = MenuUtils.getColumns(inventory);
        int slot = this.row * columns + this.column;

        if (!this.isZeroIndexed) {
            slot -= columns + 1;
        }

        if (slot >= inventory.getSize()) {
            throw new IllegalArgumentException(
                    "SlotPosition exceeded inventory size of %s slots for args (row = %s, column = %s, isZeroIndexed = %s)".formatted(
                            inventory.getSize(), this.row, this.column, this.isZeroIndexed));
        }

        return slot;
    }
}