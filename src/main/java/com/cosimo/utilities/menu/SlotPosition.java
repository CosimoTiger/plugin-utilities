package com.cosimo.utilities.menu;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.inventory.Inventory;

@Getter
public class SlotPosition {

    private final int row, column;
    private final boolean isZeroIndexed;

    private SlotPosition(int row, int column, boolean isZeroIndexed) {
        if (row < 0 || !isZeroIndexed && row == 0) {
            throw new IllegalArgumentException(
                    "Invalid SlotPosition row given (row = %s) and (isZeroIndexed = %s)".formatted(row, isZeroIndexed));
        } else if (column < 0 || !isZeroIndexed && column == 0) {
            throw new IllegalArgumentException(
                    "Invalid SlotPosition column given (column = %s) and (isZeroIndexed = %s)".formatted(column,
                                                                                                         isZeroIndexed));
        }

        this.row = row;
        this.column = column;
        this.isZeroIndexed = isZeroIndexed;
    }

    public static SlotPosition of(int row, int column, boolean isZeroIndexed) {
        return new SlotPosition(row, column, isZeroIndexed);
    }

    public static SlotPosition of(int row, int column) {
        return of(row, column, true);
    }

    public int getSlotIndex(@NonNull Inventory inventory) {
        final int rows = inventory.getSize() / MenuUtils.getInventoryTypeColumns(inventory);
        final int slot;

        if (!this.isZeroIndexed) {
            slot = this.row * rows + this.column;
        } else {
            slot = (this.row - 1) * rows + this.column - 1;
        }

        if (slot >= inventory.getSize()) {
            throw new IllegalArgumentException(
                    "SlotPosition exceeded inventory size of %s slots for args (row = %s, column = %s, isZeroIndexed = %s)".formatted(
                            inventory.getSize(), this.row, this.column, this.isZeroIndexed));
        }

        return slot;
    }
}