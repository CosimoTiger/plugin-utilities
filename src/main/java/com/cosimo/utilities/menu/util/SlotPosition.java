package com.cosimo.utilities.menu.util;

import com.cosimo.utilities.menu.IMenu;
import lombok.NonNull;
import org.bukkit.inventory.Inventory;

public record SlotPosition(int row, int column, boolean isZeroIndexed) {

    public SlotPosition {
        if (row < 0 || !isZeroIndexed && row == 0) {
            throw new IllegalArgumentException(
                    "Invalid SlotPosition row given (row = %s) and (isZeroIndexed = %s)".formatted(row, isZeroIndexed));
        } else if (column < 0 || !isZeroIndexed && column == 0) {
            throw new IllegalArgumentException(
                    "Invalid SlotPosition column given (column = %s) and (isZeroIndexed = %s)".formatted(column,
                                                                                                         isZeroIndexed));
        }
    }

    public static SlotPosition atZeroIndex(int row, int column) {
        return new SlotPosition(row, column, true);
    }

    public static SlotPosition at(int row, int column) {
        return new SlotPosition(row, column, false);
    }

    public int toSlot(@NonNull IMenu menu) {
        return this.toSlot(menu.getInventory());
    }

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