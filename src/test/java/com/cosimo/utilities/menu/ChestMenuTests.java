package com.cosimo.utilities.menu;

import com.cosimo.utilities.menu.type.PropertyMenu;
import com.cosimo.utilities.menu.util.MenuUtils;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ChestMenuTests {

    private static final int ROWS = 6;
    private static final int COLUMNS = 9;
    private static final int INVENTORY_SIZE = ROWS * COLUMNS;

    @Captor
    private ArgumentCaptor<Integer> itemSlotArgCaptor;
    @Captor
    private ArgumentCaptor<Integer> propertySlotArgCaptor;

    private MockedStatic<MenuUtils> mockedUtils;

    @Mock
    private BukkitTask mockedTask;

    @Mock
    private Inventory mockedInventory;

    private PropertyMenu<Object> menu;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        when(this.mockedInventory.getSize()).thenReturn(INVENTORY_SIZE);

        this.mockedUtils = mockStatic(MenuUtils.class);
        this.mockedUtils.when(() -> MenuUtils.getColumns(any())).thenReturn(9);

        this.menu = spy(new PropertyMenu<>(this.mockedInventory));

        doAnswer(invocation -> {
            final int slot = invocation.getArgument(0);

            throw new IndexOutOfBoundsException(
                    "Slot %d is out of bounds! Minimum 0, exclusive maximum %s".formatted(slot, INVENTORY_SIZE));
        }).when(this.mockedInventory)
                .setItem(ArgumentMatchers.intThat(slot -> slot < 0 || slot >= INVENTORY_SIZE),
                         nullable(ItemStack.class));

        doAnswer(invocation -> {
            final int slot = invocation.getArgument(1);

            throw new IndexOutOfBoundsException(
                    "Slot %d is out of bounds! Minimum 0, exclusive maximum %s".formatted(slot, INVENTORY_SIZE));
        }).when(this.menu).set(nullable(Object.class),
                               ArgumentMatchers.intThat(slot -> slot < 0 || slot >= INVENTORY_SIZE));
    }

    @AfterEach
    public void tearDown() {
        this.mockedUtils.close();
    }

    private static Stream<FillAreaTestCase> generateFillAreaTestCases() {
        return Stream.of(new FillAreaTestCase(Set.of(0, 1, 9, 10), 0, 10),
                         new FillAreaTestCase(Set.of(4, 5, 6, 13, 14, 15), 4, 15),
                         new FillAreaTestCase(Set.of(0, 1, 2, 3, 4, 9, 10, 11, 12, 13, 18, 19, 20, 21, 22), 0, 22),
                         new FillAreaTestCase(Set.of(5), 5, 5),
                         new FillAreaTestCase(Set.of(31, 32, 40, 41), 31, 41));
    }

    @ParameterizedTest
    @MethodSource("generateFillAreaTestCases")
    public void shouldFillRectangularAreaCorrectly(FillAreaTestCase testCase) {
        this.menu.fillArea(Button.empty(), testCase.startSlot(), testCase.endSlot());

        verify(this.mockedInventory, times(testCase.expectedSlots().size())).setItem(this.itemSlotArgCaptor.capture(),
                                                                                     nullable(ItemStack.class));
        verify(this.menu, times(testCase.expectedSlots().size())).set(nullable(Objects.class),
                                                                      this.propertySlotArgCaptor.capture());

        assertEquals(testCase.expectedSlots(), Set.copyOf(this.itemSlotArgCaptor.getAllValues()));
    }

    @NonNull
    private static Stream<OutlineTestCase> generateOutlineTestCases() {
        return Stream.of(new OutlineTestCase(
                                 Set.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53), 0,
                                 53),
                         new OutlineTestCase(Set.of(10, 11, 12, 13, 14, 19, 23, 28, 32, 37, 41, 46, 47, 48, 49, 50), 10,
                                             50),
                         new OutlineTestCase(Set.of(0, 1, 2, 9, 11, 18, 19, 20), 0, 20),
                         new OutlineTestCase(Set.of(32, 33, 34, 35, 41, 44, 50, 51, 52, 53), 32, 53),
                         new OutlineTestCase(Set.of(5, 6, 7, 8, 14, 15, 16, 17), 5, 17),
                         new OutlineTestCase(Set.of(0, 1, 9, 10), 0, 10),
                         new OutlineTestCase(Set.of(1, 2, 10, 11), 1, 11),
                         new OutlineTestCase(Set.of(21, 22, 23), 21, 23),
                         new OutlineTestCase(Set.of(0), 0, 0));
    }

    @ParameterizedTest
    @MethodSource("generateOutlineTestCases")
    public void shouldDrawOutlineInExactSlots(OutlineTestCase testCase) {
        this.menu.drawOutline(Button.empty(), testCase.startSlot(), testCase.endSlot());

        verify(this.mockedInventory, times(testCase.outlineSlots().size())).setItem(this.itemSlotArgCaptor.capture(),
                                                                                    nullable(ItemStack.class));
        verify(this.menu, times(testCase.outlineSlots().size())).set(nullable(Objects.class),
                                                                     this.propertySlotArgCaptor.capture());

        assertEquals(testCase.outlineSlots(), Set.copyOf(this.itemSlotArgCaptor.getAllValues()));
    }

    private static Stream<LineTestCase> getColumnTestCases() {
        return IntStream.range(0, COLUMNS).mapToObj(column -> {
            final var columnSlots = IntStream.range(0, ROWS).map(slot -> column + slot * COLUMNS).boxed().toList();
            return new LineTestCase(columnSlots, column);
        });
    }

    @ParameterizedTest
    @MethodSource("getColumnTestCases")
    public void shouldDrawColumnInExactSlots(LineTestCase columnTestCase) {
        this.menu.fillColumn(Button.empty(), columnTestCase.column());

        verify(this.mockedInventory, times(ROWS)).setItem(this.itemSlotArgCaptor.capture(), nullable(ItemStack.class));
        verify(this.menu, times(ROWS)).set(nullable(Objects.class), this.propertySlotArgCaptor.capture());

        assertEquals(columnTestCase.lineSlots(), this.itemSlotArgCaptor.getAllValues());
    }

    private static Stream<LineTestCase> getRowTestCases() {
        return IntStream.range(0, ROWS).mapToObj(row -> {
            final var rowSlots = IntStream.range(0, COLUMNS).map(slot -> row * COLUMNS + slot).boxed().toList();
            return new LineTestCase(rowSlots, row);
        });
    }

    @ParameterizedTest
    @MethodSource("getRowTestCases")
    public void shouldDrawRowInExactSlots(LineTestCase rowTestCase) {
        this.menu.fillRow(Button.empty(), rowTestCase.column());

        verify(this.mockedInventory, times(COLUMNS)).setItem(this.itemSlotArgCaptor.capture(),
                                                             nullable(ItemStack.class));
        verify(this.menu, times(COLUMNS)).set(nullable(Objects.class), this.propertySlotArgCaptor.capture());

        assertEquals(rowTestCase.lineSlots(), this.itemSlotArgCaptor.getAllValues());
    }

    @Test
    public void shouldIdentifyWhenTaskIsAssigned() {
        assertFalse(this.menu.hasBukkitTask());

        when(this.mockedTask.getTaskId()).thenReturn(42);
        this.menu.attachBukkitTask(this.mockedTask);

        assertTrue(this.menu.hasBukkitTask());
        assertEquals(42, this.menu.getBukkitTaskId());
    }

    @Test
    public void shouldCancelPreviousTaskWhenAssigningNewTask() {
        final BukkitScheduler mockedScheduler = mock(BukkitScheduler.class);

        try (var mockedBukkit = mockStatic(Bukkit.class)) {
            mockedBukkit.when(Bukkit::getScheduler).thenReturn(mockedScheduler);

            when(this.mockedTask.getTaskId()).thenReturn(42);
            this.menu.attachBukkitTask(this.mockedTask);

            final BukkitTask newMockedTask = mock(BukkitTask.class);

            when(newMockedTask.getTaskId()).thenReturn(84);
            this.menu.attachBukkitTask(newMockedTask);

            verify(mockedScheduler).cancelTask(42);
        }

        assertEquals(84, this.menu.getBukkitTaskId());
    }

    @Test
    public void shouldHandleNullTaskGracefully() {
        final BukkitScheduler mockedScheduler = mock(BukkitScheduler.class);

        try (var mockedBukkit = mockStatic(Bukkit.class)) {
            mockedBukkit.when(Bukkit::getScheduler).thenReturn(mockedScheduler);

            when(this.mockedTask.getTaskId()).thenReturn(42);

            this.menu.attachBukkitTask(this.mockedTask);
            this.menu.attachBukkitTask(null);

            verify(mockedScheduler).cancelTask(42);
        }

        assertEquals(-1, this.menu.getBukkitTaskId());
        assertFalse(this.menu.hasBukkitTask());
    }

    public record FillAreaTestCase(@NonNull Set<Integer> expectedSlots, int startSlot, int endSlot) {
    }

    public record OutlineTestCase(@NonNull Set<Integer> outlineSlots, int startSlot, int endSlot) {
    }

    public record LineTestCase(@NonNull List<Integer> lineSlots, int column) {
    }
}