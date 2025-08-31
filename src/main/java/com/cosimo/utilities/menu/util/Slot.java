package com.cosimo.utilities.menu.util;

import com.cosimo.utilities.menu.IMenu;
import lombok.NonNull;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Contract;

/**
 * Utility class for handling slot conversions in {@link Inventory} or {@link IMenu} grids. Provides separate utilities
 * for zero-based and one-based indexing through static inner classes for better convenience.
 */
public final class Slot {

    private Slot() {
    }

    /**
     * Utilities for zero-based indexing.
     */
    public static final class Zero {
        private Zero() {
        }

        /**
         * Converts a row-column position to a slot index with zero-based indexing with default columns for chest
         * inventories using {@link Menus#CHEST_COLUMNS}.
         *
         * @param row    The row of the slot position (starting from 1)
         * @param column The column of the slot position (starting from 1)
         * @return The calculated slot index
         */
        @Contract(pure = true)
        public static int of(int row, int column) {
            return of(row, column, Menus.CHEST_COLUMNS);
        }

        /**
         * Converts a row-column position to a slot index with zero-based indexing.
         *
         * @param row     The row of the slot position (starting from 0)
         * @param column  The column of the slot position (starting from 0)
         * @param columns The number of columns in the grid
         * @return The calculated slot index
         */
        @Contract(pure = true)
        public static int of(int row, int column, int columns) {
            validateIndices(row, column, columns);
            return row * columns + column;
        }

        /**
         * Converts a row-column position to a slot index within a specific {@link Inventory}.
         *
         * @param row       The row of the slot position (starting from 0)
         * @param column    The column of the slot position (starting from 0)
         * @param inventory The {@link Inventory} instance
         * @return The calculated slot index
         * @throws IllegalArgumentException if the slot is out of bounds
         */
        @Contract(pure = true)
        public static int of(int row, int column, @NonNull Inventory inventory) {
            final int slot = of(row, column, Menus.getColumns(inventory));

            if (slot >= inventory.getSize()) {
                throw new IllegalArgumentException(
                        "Slot index exceeds inventory size of %s (row = %s, column = %s)".formatted(inventory.getSize(),
                                                                                                    row, column));
            }

            return slot;
        }

        /**
         * Validates row and column indices based on the indexing scheme.
         *
         * @param row     The row index
         * @param column  The column index
         * @param columns The number of columns in the grid
         * @throws IllegalArgumentException if indices are invalid
         */
        @Contract(pure = true)
        public static void validateIndices(int row, int column, int columns) {
            if (columns <= 0) {
                throw new IllegalArgumentException("Columns can't be zero or negative");
            } else if (row < 0 || column < 0 || column >= columns) {
                throw new IllegalArgumentException(
                        "Row and column must be >= 0, and column < %d for zero-based indexing (row = %d, column = %d)".formatted(
                                columns, row, column));
            }
        }
    }

    /**
     * Utilities for one-based indexing.
     */
    public static final class One {
        private One() {
        }

        /**
         * Converts a row-column position to a slot index with one-based indexing with default columns for chest
         * inventories using {@link Menus#CHEST_COLUMNS}.
         *
         * @param row    The row of the slot position (starting from 1)
         * @param column The column of the slot position (starting from 1)
         * @return The calculated slot index
         */
        @Contract(pure = true)
        public static int of(int row, int column) {
            return of(row, column, Menus.CHEST_COLUMNS);
        }

        /**
         * Converts a row-column position to a slot index with one-based indexing.
         *
         * @param row     The row of the slot position (starting from 1)
         * @param column  The column of the slot position (starting from 1)
         * @param columns The number of columns in the grid
         * @return The calculated slot index
         */
        @Contract(pure = true)
        public static int of(int row, int column, int columns) {
            validateIndices(row, column, columns);
            return (row - 1) * columns + column - 1;
        }

        /**
         * Converts a row-column position to a slot index within a specific {@link Inventory}.
         *
         * @param row       The row of the slot position (starting from 1)
         * @param column    The column of the slot position (starting from 1)
         * @param inventory The {@link Inventory} instance
         * @return The calculated slot index
         * @throws IllegalArgumentException if the slot is out of bounds
         */
        @Contract(pure = true)
        public static int of(int row, int column, @NonNull Inventory inventory) {
            final int slot = of(row, column, Menus.getColumns(inventory));

            if (slot >= inventory.getSize()) {
                throw new IllegalArgumentException(
                        "Slot index exceeds inventory size of %s (row = %s, column = %s)".formatted(inventory.getSize(),
                                                                                                    row, column));
            }

            return slot;
        }

        /**
         * Validates row and column indices based on the indexing scheme.
         *
         * @param row     The row index
         * @param column  The column index
         * @param columns The number of columns in the grid
         * @throws IllegalArgumentException if indices are invalid
         */
        @Contract(pure = true)
        public static void validateIndices(int row, int column, int columns) {
            if (columns <= 0) {
                throw new IllegalArgumentException("Columns can't be zero or negative");
            } else if (row < 1 || column < 1 || column > columns) {
                throw new IllegalArgumentException(
                        "Row and column must be >= 1, and column <= %d for one-based indexing (row = %d, column = %d)".formatted(
                                columns, row, column));
            }
        }
    }
}