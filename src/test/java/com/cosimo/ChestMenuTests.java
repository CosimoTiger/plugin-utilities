package com.cosimo;

import com.cosimo.utilities.menu.Button;
import com.cosimo.utilities.menu.type.Menu;
import com.cosimo.utilities.menu.util.MenuUtils;
import lombok.NonNull;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@Execution(ExecutionMode.CONCURRENT)
public class ChestMenuTests {

    private static final int ROWS = 6;
    private static final int COLUMNS = 9;
    private static final int INVENTORY_SIZE = ROWS * COLUMNS;

    @Captor
    private ArgumentCaptor<Integer> slotArgCaptor;

    private MockedStatic<MenuUtils> mockedUtils;

    @Mock
    private Inventory mockedInventory;

    private Menu menu;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        when(this.mockedInventory.getSize()).thenReturn(INVENTORY_SIZE);

        this.mockedUtils = mockStatic(MenuUtils.class);
        this.mockedUtils.when(() -> MenuUtils.getColumns(any())).thenReturn(9);

        this.menu = spy(new Menu(this.mockedInventory));

        Mockito.doAnswer(invocation -> {
                    final int slot = invocation.getArgument(0);

                    throw new IndexOutOfBoundsException(
                            "Slot %d is out of bounds! Minimum 0, exclusive maximum %s".formatted(slot, INVENTORY_SIZE));
                })
                .when(this.mockedInventory)
                .setItem(ArgumentMatchers.intThat(slot -> slot < 0 || slot >= INVENTORY_SIZE),
                         nullable(ItemStack.class));
    }

    @AfterEach
    public void tearDown() {
        this.mockedUtils.close();
    }

    @NonNull
    private static Stream<RectangleTestCase> generateRectangleTestCases() {
        return Stream.of(new RectangleTestCase(
                                 Set.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53), 0,
                                 53), new RectangleTestCase(Set.of(10, 11, 12, 13, 14, 19, 23, 28, 32, 37, 41, 46, 47, 48, 49, 50), 10,
                                                            50), new RectangleTestCase(Set.of(0, 1, 2, 9, 11, 18, 19, 20), 0, 20),
                         new RectangleTestCase(Set.of(32, 33, 34, 35, 41, 44, 50, 51, 52, 53), 32, 53),
                         new RectangleTestCase(Set.of(5, 6, 7, 8, 14, 15, 16, 17), 5, 17),
                         new RectangleTestCase(Set.of(0, 1, 9, 10), 0, 10),
                         new RectangleTestCase(Set.of(1, 2, 10, 11), 1, 11),
                         new RectangleTestCase(Set.of(21, 22, 23), 21, 23), new RectangleTestCase(Set.of(0), 0, 0));
    }

    @ParameterizedTest
    @MethodSource("generateRectangleTestCases")
    public void shouldDrawRectangleInExactSlots(RectangleTestCase testCase) {
        this.menu.drawOutline(Button.empty(), testCase.startSlot(), testCase.endSlot());

        verify(this.mockedInventory, times(testCase.rectangleSlots().size())).setItem(this.slotArgCaptor.capture(),
                                                                                      nullable(ItemStack.class));

        assertEquals(testCase.rectangleSlots(), Set.copyOf(this.slotArgCaptor.getAllValues()));
    }

    private static Stream<ColumnTestCase> getColumnTestCases() {
        return IntStream.range(0, COLUMNS).mapToObj(column -> {
            final var columnSlots = IntStream.range(0, ROWS).map(slot -> column + slot * COLUMNS).boxed().toList();
            return new ColumnTestCase(columnSlots, column);
        });
    }

    @ParameterizedTest
    @MethodSource("getColumnTestCases")
    public void shouldDrawColumnInExactSlots(ColumnTestCase columnTestCase) {
        this.menu.fillColumn(Button.empty(), columnTestCase.column());

        verify(this.mockedInventory, times(ROWS)).setItem(this.slotArgCaptor.capture(), nullable(ItemStack.class));

        assertEquals(columnTestCase.columnSlots(), this.slotArgCaptor.getAllValues());
    }

    public record RectangleTestCase(@NonNull Set<Integer> rectangleSlots, int startSlot, int endSlot) {
    }

    public record ColumnTestCase(@NonNull List<Integer> columnSlots, int column) {
    }
}