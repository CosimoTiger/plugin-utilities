package com.cosimo.utilities.menu;

import com.cosimo.utilities.menu.type.action.ActionMenu;
import com.cosimo.utilities.menu.type.action.MenuAction;
import com.cosimo.utilities.menu.util.MenuUtils;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;

import java.util.stream.IntStream;

import static com.cosimo.utilities.menu.util.MenuUtils.CHEST_COLUMNS;
import static com.cosimo.utilities.menu.util.MenuUtils.MAX_CHEST_ROWS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ChestMenuTests {

    private static final int INVENTORY_SIZE = MAX_CHEST_ROWS * CHEST_COLUMNS;

    private MockedStatic<MenuUtils> mockedUtils;

    @Mock
    private BukkitTask mockedTask;

    @Mock
    private Inventory mockedInventory;

    @Mock
    private ItemStack mockedItem;

    private ActionMenu menu;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        when(this.mockedInventory.getSize()).thenReturn(INVENTORY_SIZE);

        this.mockedUtils = mockStatic(MenuUtils.class, InvocationOnMock::callRealMethod);
        this.mockedUtils.when(() -> MenuUtils.getColumns(any())).thenReturn(9);

        this.menu = spy(new ActionMenu(this.mockedInventory));

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
        }).when(this.menu)
          .set(nullable(MenuAction.class), ArgumentMatchers.intThat(slot -> slot < 0 || slot >= INVENTORY_SIZE));
    }

    @AfterEach
    public void tearDown() {
        this.mockedUtils.close();
    }

    @Test
    public void shouldSetItemExactTimes() {
        final var slots = IntStream.range(0, this.menu.getInventory().getSize()).toArray();

        this.menu.set(this.mockedItem, slots);

        verify(this.menu, times(slots.length)).set(eq(this.mockedItem), anyInt());
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

    @Test
    public void testGetChestRowsForCount() {
        assertEquals(1, MenuUtils.getChestRowsForCount(5));
        assertEquals(3, MenuUtils.getChestRowsForCount(20));
        assertEquals(MenuUtils.MAX_CHEST_ROWS, MenuUtils.getChestRowsForCount(100));
        assertEquals(1, MenuUtils.getChestRowsForCount(0));
    }

    @Test
    public void testGetChestSizeForCount() {
        assertEquals(9, MenuUtils.getChestSizeForCount(5));
        assertEquals(27, MenuUtils.getChestSizeForCount(20));
        assertEquals(MenuUtils.MAX_CHEST_ROWS * MenuUtils.CHEST_COLUMNS, MenuUtils.getChestSizeForCount(100));
        assertEquals(9, MenuUtils.getChestSizeForCount(0));
    }
}