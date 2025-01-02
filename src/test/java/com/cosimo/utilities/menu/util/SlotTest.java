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

import static com.cosimo.utilities.menu.util.MenuUtils.CHEST_COLUMNS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class SlotTest {

    private static final int ROWS = 4;

    private MockedStatic<MenuUtils> mockedUtils;

    @Mock
    private Inventory mockedInventory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        this.mockedUtils = mockStatic(MenuUtils.class);
        this.mockedUtils.when(() -> MenuUtils.getColumns(this.mockedInventory)).thenReturn(CHEST_COLUMNS);

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

    private static Stream<Slot> getValidZeroIndexedSlots() {
        return IntStream.range(0, ROWS)
                .boxed()
                .flatMap(row -> IntStream.range(0, CHEST_COLUMNS).mapToObj(column -> Slot.of0th(row, column)));
    }

    private static Stream<SlotPositionTestCase> getZeroIndexedTestCases() {
        final var slots = getValidZeroIndexedSlots().toList();
        return getInventorySlots().mapToObj(index -> new SlotPositionTestCase(slots.get(index), index));
    }

    @ParameterizedTest
    @MethodSource("getZeroIndexedTestCases")
    void testToSlotZeroIndexed(SlotPositionTestCase testCase) {
        final int slot = testCase.slot().toSlot(this.mockedInventory);
        assertEquals(testCase.expectedSlot(), slot);
    }

    private static Stream<Slot> getValidSlots() {
        return IntStream.range(1, ROWS + 1)
                .boxed()
                .flatMap(row -> IntStream.range(1, CHEST_COLUMNS + 1).mapToObj(column -> Slot.of1st(row, column)));
    }

    private static Stream<SlotPositionTestCase> getOneIndexedTestCases() {
        final var slots = getValidSlots().toList();
        return getInventorySlots().mapToObj(index -> new SlotPositionTestCase(slots.get(index), index));
    }

    @ParameterizedTest
    @MethodSource("getOneIndexedTestCases")
    void testToSlotOneIndexed(SlotPositionTestCase testCase) {
        final int slot = testCase.slot().toSlot(this.mockedInventory);
        assertEquals(testCase.expectedSlot(), slot);
    }

    @ParameterizedTest
    @ValueSource(ints = {-99, -5, -2, -1})
    void testInvalidRowAndColumnIndex(int value) {
        assertThrows(IllegalArgumentException.class, () -> Slot.of0th(value, 1));
        assertThrows(IllegalArgumentException.class, () -> Slot.of0th(1, value));
    }

    private static Stream<Slot> getExceedingZeroIndexedSlots() {
        return Stream.of(Slot.of0th(5, 0), Slot.of0th(4, 10), Slot.of0th(4, 11), Slot.of0th(0, 37));
    }

    @ParameterizedTest
    @MethodSource("getExceedingZeroIndexedSlots")
    void testZeroIndexedExceedsInventory(Slot position) {
        assertThrows(IllegalArgumentException.class, () -> position.toSlot(this.mockedInventory));
    }

    private static Stream<Slot> getExceedingOneIndexedSlots() {
        return Stream.of(Slot.of1st(4, 10), Slot.of1st(5, 1), Slot.of1st(5, 8), Slot.of1st(5, 9), Slot.of1st(4, 10));
    }

    @ParameterizedTest
    @MethodSource("getExceedingOneIndexedSlots")
    void testOneIndexedExceedsInventory(Slot position) {
        assertThrows(IllegalArgumentException.class, () -> position.toSlot(this.mockedInventory));
    }

    private record SlotPositionTestCase(Slot slot, int expectedSlot) {
    }
}