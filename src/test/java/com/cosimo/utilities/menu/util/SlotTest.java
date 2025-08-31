package com.cosimo.utilities.menu.util;

import org.bukkit.inventory.Inventory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.cosimo.utilities.menu.util.Menus.CHEST_COLUMNS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class SlotTest {

    private static final int ROWS = 4;

    private MockedStatic<Menus> mockedUtils;

    @Mock
    private Inventory mockedInventory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        this.mockedUtils = mockStatic(Menus.class);
        this.mockedUtils.when(() -> Menus.getColumns(this.mockedInventory)).thenReturn(CHEST_COLUMNS);

        when(this.mockedInventory.getSize()).thenReturn(getInventorySize());
    }

    @AfterEach
    void tearDown() {
        this.mockedUtils.close();
    }

    private static int getInventorySize() {
        return CHEST_COLUMNS * ROWS;
    }

    private static IntStream getInventorySlots() {
        return IntStream.range(0, getInventorySize());
    }

    private static Stream<SlotPositionTestCase> getZeroIndexedTestCases() {
        return getInventorySlots().mapToObj(index -> {
            final int row = index / CHEST_COLUMNS;
            final int column = index % CHEST_COLUMNS;
            return new SlotPositionTestCase(row, column, index);
        });
    }

    @ParameterizedTest
    @MethodSource("getZeroIndexedTestCases")
    void correctZeroIndexedSlotConversion(SlotPositionTestCase testCase) {
        final int slot = Slot.Zero.of(testCase.row(), testCase.column(), this.mockedInventory);
        assertEquals(testCase.expectedSlot(), slot);
    }

    private static Stream<SlotPositionTestCase> getOneIndexedTestCases() {
        return getInventorySlots().mapToObj(index -> {
            final int row = index / CHEST_COLUMNS + 1;
            final int column = index % CHEST_COLUMNS + 1;

            return new SlotPositionTestCase(row, column, index);
        });
    }

    @ParameterizedTest
    @MethodSource("getOneIndexedTestCases")
    void correctOneIndexedSlotConversion(SlotPositionTestCase testCase) {
        final int slot = Slot.One.of(testCase.row(), testCase.column(), this.mockedInventory);
        assertEquals(testCase.expectedSlot(), slot);
    }

    @ParameterizedTest
    @ValueSource(ints = {-99, -5, -2, -1, CHEST_COLUMNS, CHEST_COLUMNS + 1})
    void shouldThrowForInvalidZeroIndexedColumn(int value) {
        assertThrows(IllegalArgumentException.class, () -> Slot.Zero.of(1, value, this.mockedInventory));
    }

    @ParameterizedTest
    @ValueSource(ints = {-99, -5, -2, -1})
    void shouldThrowForInvalidZeroIndexedRow(int value) {
        assertThrows(IllegalArgumentException.class, () -> Slot.Zero.of(value, 1, this.mockedInventory));
    }

    @ParameterizedTest
    @ValueSource(ints = {-2, -1, 0, CHEST_COLUMNS + 1})
    void shouldThrowForInvalidOneIndexedColumn(int value) {
        assertThrows(IllegalArgumentException.class, () -> Slot.One.of(1, value, this.mockedInventory));
    }

    @ParameterizedTest
    @ValueSource(ints = {-2, -1, 0})
    void shouldThrowForInvalidOneIndexedRow(int row) {
        assertThrows(IllegalArgumentException.class, () -> Slot.One.of(row, 1, this.mockedInventory));
    }

    private record SlotPositionTestCase(int row, int column, int expectedSlot) {
    }
}