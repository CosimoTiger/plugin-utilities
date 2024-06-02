package com.cosimo.utilities.menu;

import com.cosimo.utilities.menu.manager.MenuManager;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

/**
 * Represents a collection of algorithms, features and event handlers upon an {@link Inventory} of any type that
 * multiple viewers can see and interact with.
 *
 * <p>A developer can subclass this class, override the methods or add them to customise the ways of processing inputs
 * for inventory menu events or modifying the inventories.
 *
 * @param <S> Menu subclass type of {@link AbstractMenu}
 * @author CosimoTiger
 * @see Menu
 * @see PropertyMenu
 */
@SuppressWarnings("unchecked")
public abstract class AbstractMenu<S extends AbstractMenu<S>> implements IMenu {

    /**
     * Backing {@link Inventory} that's wrapped and controlled by this class.
     */
    private final Inventory inventory;
    /**
     * The identifier number of a {@link BukkitTask} that's relevant to this inventory. The task is by default
     * automatically cancelled when the menu is closed with no viewers left, but this can be modified by overriding the
     * {@link #onClose(InventoryCloseEvent)} method. Example use is a constant animation that's setting new
     * {@link ItemStack}s in the menu.
     */
    protected int taskID = -1;

    /**
     * The default constructor for all subclasses.
     *
     * @param inventory Not null {@link Inventory} that will be wrapped and controlled by an {@link AbstractMenu}
     * @throws IllegalArgumentException If the {@link Inventory} argument is null
     */
    public AbstractMenu(@NotNull Inventory inventory) {
        Preconditions.checkArgument(inventory != null, "Inventory argument can't be null");
        this.inventory = inventory;
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
    public void onClose(@NotNull InventoryCloseEvent event) {
        if (this.getInventory().getViewers().size() < 2) {
            this.setBukkitTask(null);
        }
    }

    /**
     * Acts as an event handler for the inventory opening event.
     *
     * @param event {@link InventoryOpenEvent} event
     */
    @Override
    public void onOpen(@NotNull InventoryOpenEvent event) {
    }

    /**
     * Opens this {@link AbstractMenu} for the given viewers, fail-fast.
     *
     * @param viewers {@link Collection}&lt;{@link HumanEntity}&gt; of which each will see this {@link AbstractMenu}
     *                {@link Inventory}
     * @return This instance, useful for chaining
     * @throws IllegalArgumentException If the {@link Collection}&lt;{@link HumanEntity}&gt; argument is null or empty
     * @throws IllegalStateException    If this {@link AbstractMenu}'s {@link MenuManager} isn't registered for handling
     *                                  events
     * @throws NullPointerException     If a {@link HumanEntity} is null
     */
    @NotNull
    public S open(@NotNull MenuManager<AbstractMenu<?>> menuManager, @NotNull Iterable<? extends HumanEntity> viewers) {
        Preconditions.checkArgument(viewers != null,
                "Iterable<? extends HumanEntity> of viewers argument can't be null");

        menuManager.registerMenu(this);

        final long count = StreamSupport.stream(viewers.spliterator(), false)
                .filter(Objects::nonNull)
                .peek(viewer -> viewer.openInventory(this.getInventory()))
                .count();

        if (count == 0) {
            // We've been bamboozled and need to undo the menu registration, because nobody is viewing the menu now.
            menuManager.unregisterMenu(this);
            throw new IllegalArgumentException("Zero or all null menu viewers provided to open the menu");
        }

        return (S) this;
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
    @NotNull
    public S open(@NotNull MenuManager<AbstractMenu<?>> menuManager, @NotNull HumanEntity... viewers) {
        return this.open(menuManager, List.of(viewers));
    }

    /**
     * Applies a lambda function this {@link AbstractMenu}'s {@link Inventory}, such as
     * {@link Inventory#addItem(ItemStack...)}, and returns this instance to chain it.
     *
     * @param consumer Lambda function that'll modify this {@link Inventory}
     * @return This instance, useful for chaining
     */
    @NotNull
    public S change(@NotNull Consumer<Inventory> consumer) {
        consumer.accept(this.getInventory());
        return (S) this;
    }

    /**
     * Modifies an {@link ItemStack} located at a given slot with given operations to perform.
     *
     * @param consumer Lambda method that'll take an ItemStack as an argument and perform operations on it
     * @param slot     Slot at which an {@link ItemStack} that is being modified is located
     * @return This instance, useful for chaining
     * @throws IndexOutOfBoundsException If the slot argument is out of this inventory's boundaries
     * @throws IllegalArgumentException  If the {@link Consumer}&lt;{@link ItemStack}&gt; argument is null
     */
    @NotNull
    public S change(@NotNull Consumer<ItemStack> consumer, int slot) {
        this.getItem(slot).ifPresent(consumer);
        return (S) this;
    }

    /**
     * Sets a given {@link ItemStack} at every slot in given loop range if the existing {@link ItemStack} and/or slot
     * index match the given {@link BiPredicate}.
     *
     * <p>This can be used as the fill function, for example:
     * {@code menu.setIf(new ItemStack(Material.CAKE), (item, slot) -> item == null, 5, 15);}
     *
     * @param item              Nullable (air) {@link ItemStack} to set
     * @param itemSlotPredicate Previous slot {@link ItemStack} condition to match
     * @param start             Inclusive start slot index
     * @param end               Exclusive end slot index
     * @param step              Increment amount
     * @return This instance, useful for chaining
     * @throws IllegalArgumentException If the step argument is 0
     */
    public S setIf(@Nullable ItemStack item, @NotNull BiPredicate<ItemStack, Integer> itemSlotPredicate, int start,
                   int end, int step) {
        Preconditions.checkArgument(step != 0, "step argument (" + step + ") can't be 0");

        for (int slot = start; slot < end; slot += step) {
            if (itemSlotPredicate.test(this.getInventory().getItem(slot), slot)) {
                this.getInventory().setItem(slot, item);
            }
        }

        return (S) this;
    }

    @NotNull
    public S setIf(@Nullable ItemStack item, @NotNull BiPredicate<ItemStack, Integer> itemSlotPredicate, int start,
                   int end) {
        return this.setIf(item, itemSlotPredicate, start, end, 1);
    }

    @NotNull
    public S setIf(@Nullable ItemStack item, @NotNull BiPredicate<ItemStack, Integer> itemSlotPredicate, int start) {
        return this.setIf(item, itemSlotPredicate, start, this.getInventory().getSize());
    }

    @NotNull
    public S setIf(@Nullable ItemStack item, @NotNull BiPredicate<ItemStack, Integer> itemSlotPredicate) {
        return this.setIf(item, itemSlotPredicate, 0);
    }

    /**
     * Sets an {@link ItemStack} by skipping an amount of given slots from the inclusive start to the exclusive end.
     *
     * @param item  {@link ItemStack} or null
     * @param start Inclusive start slot index
     * @param end   Exclusive end slot index for this inventory's size
     * @param step  Amount of slots to be skipped until next {@link ItemStack} placement
     * @return This instance, useful for chaining
     * @throws IllegalArgumentException If step argument is lower than 1
     */
    @NotNull
    public S setRange(@Nullable ItemStack item, int start, int end, int step) {
        Preconditions.checkArgument(step != 0, "step argument (" + step + ") can't be 0");

        for (int slot = start; slot < end; slot += step) {
            this.getInventory().setItem(slot, item);
        }

        return (S) this;
    }

    @NotNull
    public S setRange(@Nullable ItemStack item, int start, int end) {
        return this.setRange(item, start, end, 1);
    }

    /**
     * Sets an {@link ItemStack} from a given inclusive starting slot index to the end of {@link Inventory#getSize()}.
     *
     * @param item  Nullable {@link ItemStack} to set in slots
     * @param start Positive inclusive starting slot index
     * @return This instance, useful for chaining
     */
    @NotNull
    public S setRange(@Nullable ItemStack item, int start) {
        return this.setRange(item, start, this.getInventory().getSize());
    }

    /**
     * Sets an {@link ItemStack} at this {@link AbstractMenu}'s given slot(s).
     *
     * @param item  {@link ItemStack} to set at given slots
     * @param slots Slots that the {@link ItemStack} will be placed at
     * @return This instance, useful for chaining
     * @throws IndexOutOfBoundsException If a slot in the slot array argument is out of this inventory's boundaries
     * @throws IllegalArgumentException  If the slot array argument is null
     */
    @NotNull
    public S set(@Nullable ItemStack item, @NotNull Iterable<Integer> slots) {
        Preconditions.checkArgument(slots != null, "Array of slots can't be null");
        slots.forEach(slot -> this.getInventory().setItem(slot, item));
        return (S) this;
    }

    /**
     * Sets an {@link ItemStack} at this {@link AbstractMenu}'s given slot(s).
     *
     * @param item  {@link ItemStack} to set at given slots
     * @param slots Slots that the {@link ItemStack} will be placed at
     * @return This instance, useful for chaining
     * @throws IndexOutOfBoundsException If a slot in the slot array argument is out of this inventory's boundaries
     * @throws IllegalArgumentException  If the slot array argument is null
     */
    @NotNull
    public S set(@Nullable ItemStack item, int @NotNull ... slots) {
        Preconditions.checkArgument(slots != null, "Array of slots can't be null");

        for (int slot : slots) {
            this.getInventory().setItem(slot, item);
        }

        return (S) this;
    }

    /**
     * Assigns a {@link BukkitTask} that will run until the inventory is closed or a new {@link BukkitTask} is set, and
     * cancels the currently assigned {@link #getBukkitTask()}.
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
    @NotNull
    public S setBukkitTask(@Nullable BukkitTask task) {
        if (this.taskID > -1) {
            Bukkit.getScheduler().cancelTask(this.taskID);
        }

        this.taskID = task == null ? -1 : task.getTaskId();
        return (S) this;
    }

    /**
     * Closes all {@link Inventory} of this instance for all viewers who are viewing it.
     *
     * <p>Remember to schedule this action for the next tick if you're running it inside an inventory event handler!
     *
     * @return This instance, useful for chaining
     */
    @NotNull
    public S close() {
        return (S) IMenu.super.close();
    }

    /**
     * Clears all of this inventory menu's contents.
     *
     * <p>Some subclasses may implement additional contents that can be cleared, such as properties.</p>
     *
     * @return This instance, useful for chaining
     * @see PropertyMenu#clearProperties()
     */
    @NotNull
    public S clear() {
        this.getInventory().clear();
        return (S) this;
    }

    /**
     * Returns an {@link Optional} of {@link ItemStack} at the given slot of this inventory menu.
     *
     * @param slot Slot index location of the item in the inventory
     * @return {@link Optional} of a nullable {@link ItemStack}
     * @throws IndexOutOfBoundsException If the given slot argument is out of the inventory's bounds
     */
    @NotNull
    public Optional<ItemStack> getItem(int slot) {
        return Optional.ofNullable(this.getInventory().getItem(slot));
    }

    /**
     * Returns the {@link Inventory} that's wrapped and controlled by this {@link AbstractMenu}.
     *
     * @return Always the same {@link Inventory}
     */
    @NotNull
    public final Inventory getInventory() {
        return this.inventory;
    }

    /**
     * Returns the identifier number of the {@link BukkitTask} that is stored in this instance and running while it's
     * open.
     *
     * @return {@link BukkitTask} identifier number or -1 if none is assigned
     */
    public int getBukkitTask() {
        return this.taskID;
    }
}