package com.cosimo.utilities.menu.type;

import com.cosimo.utilities.menu.IMenu;
import com.cosimo.utilities.menu.MenuManager;
import com.cosimo.utilities.menu.type.action.ActionMenu;
import com.cosimo.utilities.menu.util.MenuUtils;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

/**
 * Represents a collection of algorithms, features and event handlers upon an {@link Inventory} of any type that
 * multiple viewers can see and interact with. All slots are zero-indexed.
 *
 * <p>A developer can subclass this class, override the methods or add them to customise the ways of processing inputs
 * for inventory menu events or modifying the inventories.
 *
 * @param <Self> Menu subclass type of {@link AbstractMenu}, which allows method chaining while retaining the same
 *               subclass instance (see "Curiously Recurring Template Pattern")
 * @author CosimoTiger
 * @see Menu
 * @see ActionMenu
 */
@SuppressWarnings({"unchecked", "unused"})
public abstract class AbstractMenu<Self extends AbstractMenu<Self>> implements IMenu {

    /**
     * Backing {@link Inventory} that's wrapped and controlled by this class.
     */
    private final Inventory inventory;
    private final int columns, rows;

    /**
     * The identifier number of a {@link BukkitTask} that's attached to this inventory's lifecycle. The task is by
     * default automatically cancelled when the menu is closed with no viewers left, but this can be modified by
     * overriding the {@link #onClose(InventoryCloseEvent)} method. Example use is a constant animation that's setting
     * new {@link ItemStack}s in the menu.
     */
    protected int taskId = -1;

    /**
     * The default constructor for all subclasses.
     *
     * @param inventory Not null {@link Inventory} that will be wrapped and controlled by an {@link AbstractMenu}
     * @throws IllegalArgumentException If the {@link Inventory} argument is null
     */
    public AbstractMenu(@NonNull Inventory inventory) {
        this.inventory = inventory;
        this.columns = IMenu.super.getColumns();
        this.rows = IMenu.super.getRows();
    }

    /**
     * Handles the inventory close events of an inventory.
     *
     * <p>Can open new menus: for the given {@link InventoryCloseEvent} of a
     * {@link HumanEntity}, perform that task on the next tick. For an example, to reopen this menu:
     * {@code Bukkit.getScheduler().runTask(plugin, () -> open(manager, event.getPlayer()))}.
     *
     * @param event {@link InventoryCloseEvent} event
     */
    @Override
    public void onClose(@NonNull InventoryCloseEvent event) {
        if (MenuUtils.isAboutToBecomeDisposable(event)) {
            this.attachBukkitTask(null);
        }
    }

    /**
     * Acts as an event handler for the inventory opening event.
     *
     * @param event {@link InventoryOpenEvent} event
     */
    @Override
    public void onOpen(@NonNull InventoryOpenEvent event) {
    }

    /**
     * Opens this {@link AbstractMenu} for the given viewers, fail-fast.
     *
     * @param viewers {@link Iterable}&lt;{@link HumanEntity}&gt; of which each will see this {@link AbstractMenu}
     *                {@link Inventory}
     * @return This instance, useful for chaining
     * @throws IllegalArgumentException If the {@link Iterable}&lt;{@link HumanEntity}&gt; argument is null or empty
     * @throws IllegalStateException    If this {@link AbstractMenu}'s {@link MenuManager} isn't registered for handling
     *                                  events
     * @throws NullPointerException     If a {@link HumanEntity} is null
     */
    @NonNull
    @Contract(value = "_, _ -> this", mutates = "this")
    public Self open(@NonNull MenuManager menuManager, @NonNull Iterable<@NonNull ? extends HumanEntity> viewers) {
        viewers.forEach(viewer -> Objects.requireNonNull(viewer, "Menu viewer can't be null"));

        menuManager.registerMenu(this);

        StreamSupport.stream(viewers.spliterator(), false).forEach(viewer -> viewer.openInventory(this.getInventory()));

        return (Self) this;
    }

    /**
     * Opens this {@link AbstractMenu} for the given viewers, fail-fast.
     *
     * @param viewers Array of {@link HumanEntity} of which each will see this {@link AbstractMenu} {@link Inventory}
     * @return This instance, useful for chaining
     * @throws IllegalArgumentException If the {@link HumanEntity} array argument is null or empty
     * @throws IllegalStateException    If this {@link AbstractMenu}'s {@link MenuManager} isn't registered for handling
     *                                  events
     * @throws NullPointerException     If a {@link HumanEntity} is null
     */
    @NonNull
    @Contract(value = "_, _ -> this", mutates = "this")
    public Self open(@NonNull MenuManager menuManager, @NonNull HumanEntity @NonNull ... viewers) {
        return this.open(menuManager, List.of(viewers));
    }

    /**
     * Opens this {@link AbstractMenu} for the given viewers using the {@link MenuManager} singleton instance,
     * fail-fast.
     *
     * @param viewers Array of {@link HumanEntity} of which each will see this {@link AbstractMenu} {@link Inventory}
     * @return This instance, useful for chaining
     * @throws IllegalArgumentException If the {@link HumanEntity} array argument is null or empty
     * @throws IllegalStateException    If this {@link AbstractMenu}'s {@link MenuManager} isn't registered for handling
     *                                  events
     * @throws NullPointerException     If a {@link HumanEntity} is null
     */
    @NonNull
    @Contract(value = "_ -> this", mutates = "this")
    public Self open(@NonNull HumanEntity @NonNull ... viewers) {
        return this.open(MenuManager.getInstance(), viewers);
    }

    /**
     * Opens this {@link AbstractMenu} for the given viewers using the {@link MenuManager} singleton instance.
     *
     * @param viewers {@link Iterable}&lt;{@link HumanEntity}&gt; of which each will see this {@link AbstractMenu}
     *                {@link Inventory}
     * @return This instance, useful for chaining
     * @throws IllegalArgumentException If the {@link Iterable}&lt;{@link HumanEntity}&gt; argument is null or empty
     * @throws IllegalStateException    If this {@link AbstractMenu}'s {@link MenuManager} isn't registered for handling
     *                                  events
     * @throws NullPointerException     If a {@link HumanEntity} is null
     */
    @NonNull
    @Contract(value = "_ -> this", mutates = "this")
    public Self open(@NonNull Iterable<@NonNull ? extends HumanEntity> viewers) {
        return this.open(MenuManager.getInstance(), viewers);
    }

    /**
     * Applies changes to this {@link AbstractMenu}'s {@link Inventory}, such as
     * {@link Inventory#addItem(ItemStack...)}, and returns this instance to chain it.
     *
     * @param consumer Method that'll modify this {@link Inventory}
     * @return This instance, useful for chaining
     */
    @NonNull
    @Contract(value = "_ -> this", mutates = "this")
    public Self apply(@NonNull Consumer<Inventory> consumer) {
        consumer.accept(this.getInventory());
        return (Self) this;
    }

    /**
     * Sets an {@link ItemStack} on the slots generated by the {@link IntStream} {@link Function} contextualized with
     * this {@link AbstractMenu} for parameters such as {@link #getColumns()}, {@link #getRows()},
     * {@link #getColumnIndex(int)} and {@link #getRowIndex(int)}.
     * <p>
     * Example:
     * <pre>
     * {@code new Menu(Bukkit.createInventory(null, 4 * 9, "Filled menu"))
     *      .set(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE, " ").build(),
     *          menu -> IntStream.range(0, menu.getInventory().getSize()))
     *      .open(player);
     * }
     * </pre>
     *
     * @param item       {@link ItemStack} to set at the generated slots
     * @param slotStream {@link Function} that takes this menu as an input and returns an {@link IntStream} of slots to
     *                   set the given {@link ItemStack} on
     * @return This instance, useful for chaining
     * @throws NullPointerException If the {@code slotStream} is null
     */
    public Self set(@Nullable ItemStack item, @NonNull Function<Self, IntStream> slotStream) {
        slotStream.apply((Self) this).forEach(slot -> this.set(item, slot));
        return (Self) this;
    }

    /**
     * Sets an {@link ItemStack} at this {@link AbstractMenu}'s given slot(s).
     *
     * @param item  {@link ItemStack} to set at given slots
     * @param slots Slots that the {@link ItemStack} will be placed at
     * @return This instance, useful for chaining
     * @throws IndexOutOfBoundsException If a slot in the slot array argument is out of this inventory's boundaries
     * @throws NullPointerException      If the {@code slots} {@link Iterable} or their elements are null
     */
    @NonNull
    @Contract(value = "_, _ -> this", mutates = "this")
    public Self set(@Nullable ItemStack item, @NonNull Iterable<Integer> slots) {
        slots.forEach(slot -> this.set(item, slot));
        return (Self) this;
    }

    /**
     * Sets an {@link ItemStack} at this {@link AbstractMenu}'s given slot(s).
     *
     * @param item  {@link ItemStack} to set at given slots
     * @param slots Slots that the {@link ItemStack} will be placed at
     * @return This instance, useful for chaining
     * @throws IndexOutOfBoundsException If a slot in the slot array argument is out of this inventory's boundaries
     * @throws NullPointerException      If the slot array argument is null
     */
    @NonNull
    @Contract(value = "_, _ -> this", mutates = "this")
    public Self set(@Nullable ItemStack item, int @NonNull ... slots) {
        for (int slot : slots) {
            this.set(item, slot);
        }

        return (Self) this;
    }

    /**
     * Assigns a {@link ItemStack} to a specific slot in the inventory.
     *
     * @param item The {@link ItemStack} to assign
     * @param slot The slot index
     * @return This instance, useful for chaining
     * @throws IndexOutOfBoundsException If the slot index is out of bounds
     */
    @NonNull
    @Contract(value = "_, _ -> this", mutates = "this")
    public Self set(@Nullable ItemStack item, int slot) {
        this.getInventory().setItem(slot, item);
        return (Self) this;
    }

    /**
     * Assigns a {@link BukkitTask} that will run until the inventory is closed or a new {@link BukkitTask} is set, and
     * cancels the currently assigned {@link #getBukkitTaskId()}.
     *
     * <p>The task should be scheduled first and then set, for an example:
     * <pre>{@code
     * BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> player.sendMessage("This menu is currently open!"), 0, 40);
     * menu.setBukkitTask(task);
     * }</pre>
     *
     * @param task Nullable {@link BukkitTask} to assign
     * @return This instance, useful for chaining
     */
    @NonNull
    @Contract(value = "_ -> this", mutates = "this")
    public Self attachBukkitTask(@Nullable BukkitTask task) {
        if (this.hasBukkitTask()) {
            Bukkit.getScheduler().cancelTask(this.getBukkitTaskId());
        }

        this.taskId = task == null ? -1 : task.getTaskId();
        return (Self) this;
    }

    /**
     * Closes all {@link Inventory} of this instance for all viewers who are viewing it.
     *
     * <p>Remember to schedule this action for the next tick if you're running it inside an inventory event handler!
     *
     * @return This instance, useful for chaining
     */
    @NonNull
    @Contract(value = "-> this", mutates = "this")
    public Self close() {
        return (Self) IMenu.super.close();
    }

    /**
     * Clears all of this inventory menu's contents.
     *
     * <p>Some subclasses may implement additional contents that can be cleared, such as properties.</p>
     *
     * @return This instance, useful for chaining
     * @see ActionMenu#clearActions()
     */
    @NonNull
    @Contract(value = "-> this", mutates = "this")
    public Self clear() {
        this.getInventory().clear();
        return (Self) this;
    }

    /**
     * Returns an {@link Optional} of {@link ItemStack} at the given slot of this inventory menu.
     *
     * @param slot Slot index location of the item in the inventory
     * @return {@link Optional} of a nullable {@link ItemStack}
     * @throws IndexOutOfBoundsException If the given slot argument is out of the inventory's bounds
     */
    @NonNull
    @Contract(pure = true)
    public Optional<ItemStack> getItem(int slot) {
        return Optional.ofNullable(this.getInventory().getItem(slot));
    }

    @Override
    @Contract(pure = true)
    public int getColumns() {
        return this.columns;
    }

    @Override
    @Contract(pure = true)
    public int getRows() {
        return this.rows;
    }

    /**
     * Returns the {@link Inventory} that's wrapped and controlled by this {@link AbstractMenu}.
     *
     * @return Always the same {@link Inventory}
     */
    @NonNull
    @Contract(pure = true)
    public final Inventory getInventory() {
        return this.inventory;
    }

    /**
     * Checks if this menu has an associated Bukkit task ID stored.
     *
     * @return True if a Bukkit task ID is stored, otherwise false
     */
    @Contract(pure = true)
    public boolean hasBukkitTask() {
        return this.taskId > -1;
    }

    /**
     * Returns the identifier number of the {@link BukkitTask} that is stored in this instance and running while it's
     * open.
     *
     * @return {@link BukkitTask} identifier number or -1 if none is assigned
     */
    @Contract(pure = true)
    public int getBukkitTaskId() {
        return this.taskId;
    }
}