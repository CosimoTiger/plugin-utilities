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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class SlotPositionTest {

    private static final int COLUMNS = 9;
    private static final int ROWS = 4;

    private static int getInventorySize() {
        return COLUMNS * ROWS;
    }

    private MockedStatic<MenuUtils> mockedUtils;

    @Mock
    private Inventory mockedInventory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        this.mockedUtils = mockStatic(MenuUtils.class);
        this.mockedUtils.when(() -> MenuUtils.getColumns(this.mockedInventory)).thenReturn(COLUMNS);

        when(this.mockedInventory.getSize()).thenReturn(getInventorySize());
    }

    @AfterEach
    void tearDown() {
        this.mockedUtils.close();
    }

    private static IntStream getInventorySlots() {
        return IntStream.range(0, getInventorySize());
    }

    private static Stream<SlotPosition> getValidZeroIndexedPositions() {
        return IntStream.range(0, ROWS)
                .boxed()
                .flatMap(row -> IntStream.range(0, COLUMNS).mapToObj(column -> SlotPosition.atZeroIndex(row, column)));
    }

    private static Stream<SlotPositionTestCase> getZeroIndexedTestCases() {
        final var slots = getValidZeroIndexedPositions().toList();
        return getInventorySlots().mapToObj(index -> new SlotPositionTestCase(slots.get(index), index));
    }

    @ParameterizedTest
    @MethodSource("getZeroIndexedTestCases")
    void testToSlotZeroIndexed(SlotPositionTestCase testCase) {
        final int slot = testCase.slotPosition().toSlot(this.mockedInventory);
        assertEquals(testCase.expectedSlot(), slot);
    }

    private static Stream<SlotPosition> getValidPositions() {
        return IntStream.range(1, ROWS + 1)
                .boxed()
                .flatMap(row -> IntStream.range(1, COLUMNS + 1).mapToObj(column -> SlotPosition.at(row, column)));
    }

    private static Stream<SlotPositionTestCase> getOneIndexedTestCases() {
        final var slots = getValidPositions().toList();
        return getInventorySlots().mapToObj(index -> new SlotPositionTestCase(slots.get(index), index));
    }

    @ParameterizedTest
    @MethodSource("getOneIndexedTestCases")
    void testToSlotOneIndexed(SlotPositionTestCase testCase) {
        final int slot = testCase.slotPosition().toSlot(this.mockedInventory);
        assertEquals(testCase.expectedSlot(), slot);
    }

    @ParameterizedTest
    @ValueSource(ints = {-99, -5, -2, -1})
    void testInvalidRowAndColumnIndex(int value) {
        assertThrows(IllegalArgumentException.class, () -> SlotPosition.atZeroIndex(value, 1));
        assertThrows(IllegalArgumentException.class, () -> SlotPosition.atZeroIndex(1, value));
    }

    private static Stream<SlotPosition> getExceedingZeroIndexedPositions() {
        return Stream.of(SlotPosition.atZeroIndex(5, 0),
                         SlotPosition.atZeroIndex(4, 10),
                         SlotPosition.atZeroIndex(4, 11),
                         SlotPosition.atZeroIndex(0, 37));
    }

    @ParameterizedTest
    @MethodSource("getExceedingZeroIndexedPositions")
    void testZeroIndexedExceedsInventory(SlotPosition position) {
        assertThrows(IllegalArgumentException.class, () -> position.toSlot(this.mockedInventory));
    }

    private static Stream<SlotPosition> getExceedingOneIndexedPositions() {
        return Stream.of(SlotPosition.at(4, 10),
                         SlotPosition.at(5, 1),
                         SlotPosition.at(5, 8),
                         SlotPosition.at(5, 9),
                         SlotPosition.at(4, 10));
    }

    @ParameterizedTest
    @MethodSource("getExceedingOneIndexedPositions")
    void testOneIndexedExceedsInventory(SlotPosition position) {
        assertThrows(IllegalArgumentException.class, () -> position.toSlot(this.mockedInventory));
    }

    private record SlotPositionTestCase(SlotPosition slotPosition, int expectedSlot) {
    }
}