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
import java.util.stream.StreamSupport;

/**
 * Represents a collection of algorithms, features, and event handlers for an {@link Inventory} of any type that
 * multiple viewers can see and interact with. All slots are zero-indexed.
 *
 * <p>This class can be subclassed to override or extend its functionality, enabling customization of inventory menu
 * event processing or inventory modification.
 *
 * @param <Self> The specific subclass type of {@link AbstractMenu}, allowing method chaining while preserving the
 *               subclass instance (leveraging the "Curiously Recurring Template Pattern")
 * @author CosimoTiger
 */
@SuppressWarnings({"unchecked", "unused"})
public abstract class AbstractMenu<Self extends AbstractMenu<Self>> implements IMenu {

    /**
     * The {@link Inventory} instance managed by this class.
     */
    private final Inventory inventory;
    private final int columns, rows;

    /**
     * The identifier of the {@link BukkitTask} associated with this inventory's lifecycle. The task is automatically
     * canceled when the menu closes with no viewers, unless {@link #onClose(InventoryCloseEvent)} is overridden. A
     * common use case is running animations that periodically update the {@link ItemStack}s in the inventory.
     */
    protected int taskId = -1;

    /**
     * Constructs a new menu, wrapping and managing the provided {@link Inventory}.
     *
     * @param inventory A non-null {@link Inventory} instance to be managed by the menu
     * @throws IllegalArgumentException If the provided {@link Inventory} is null
     */
    public AbstractMenu(@NonNull Inventory inventory) {
        this.inventory = inventory;
        this.columns = IMenu.super.getColumns();
        this.rows = IMenu.super.getRows();
    }

    /**
     * Handles inventory close events.
     *
     * <p>This method can be used to open new menus after the current one is closed. For example, to reopen
     * this menu for the player: {@code Bukkit.getScheduler().runTask(plugin, () -> open(manager, event.getPlayer()))}.
     *
     * @param event The {@link InventoryCloseEvent} associated with the close action
     */
    @Override
    public void onClose(@NonNull InventoryCloseEvent event) {
        if (MenuUtils.isAboutToBecomeDisposable(event)) {
            this.attachBukkitTask(null);
        }
    }

    /**
     * Handles inventory open events.
     *
     * @param event The {@link InventoryOpenEvent} associated with the open action
     */
    @Override
    public void onOpen(@NonNull InventoryOpenEvent event) {
    }

    /**
     * Opens this menu for the specified viewers, using the provided {@link MenuManager}.
     *
     * @param menuManager The {@link MenuManager} to register this menu to
     * @param viewers     {@link Iterable}&lt;{@link HumanEntity}&gt; of which each will see this {@link AbstractMenu}
     *                    {@link Inventory}
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
     * @param menuManager The {@link MenuManager} to register this menu to
     * @param viewers     Array of {@link HumanEntity} of which each will see this {@link AbstractMenu}
     *                    {@link Inventory}
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
     * Sets an {@link ItemStack} on the slots generated by the {@link Function} contextualized with this
     * {@link AbstractMenu} for useful parameters such as {@link #getColumns()}, {@link #getRows()},
     * {@link #getColumnIndex(int)} and {@link #getRowIndex(int)}.
     * <p>
     * Example:
     * {@snippet id = "functionExample" lang = "java":
     * new Menu(Bukkit.createInventory(null, 4 * 9, "Filled menu"))
     *      .set(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE, " ").build(),
     *          menu -> IntStream.range(0, menu.getInventory().getSize()).toArray())
     *      .open(player);
     *}
     *
     * @param item          {@link ItemStack} to set at the generated slots
     * @param slotsFunction {@link Function} that takes this menu as an input and returns an array of slots to set the
     *                      given {@link ItemStack} on
     * @return This instance, useful for chaining
     * @throws NullPointerException If the {@code slotStream} is null
     */
    public Self set(@Nullable ItemStack item, @NonNull Function<Self, int[]> slotsFunction) {
        return this.set(item, slotsFunction.apply((Self) this));
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
     * <p>Some subclasses may implement additional contents that can be cleared, such as actions.</p>
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