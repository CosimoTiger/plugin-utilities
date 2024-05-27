package com.cosimo.utilities.menu;

import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * The default implementation of the {@link AbstractMenu} class.
 *
 * @author CosimoTiger
 * @see PropertyMenu
 */
public class Menu extends AbstractMenu {

    /**
     * The identifier number of a BukkitTask task that's relevant to this inventory. The task is by default
     * automatically cancelled when the menu is closed with no viewers left, but this can be modified by overriding the
     * {@link #onClose(InventoryCloseEvent)} method.
     */
    protected int taskId = -1;

    /**
     * {@inheritDoc} Creates a new {@link Menu} using the default constructor for {@link AbstractMenu}.
     */
    public Menu(@Nonnull Inventory inventory) {
        super(inventory);
    }

    @Override
    public void onClose(@Nonnull InventoryCloseEvent event) {
        if (this.getInventory().getViewers().size() < 2) {
            this.setBukkitTask(null);
        }
    }

    public Menu setIf(@Nullable ItemStack item, @Nonnull BiPredicate<ItemStack, Integer> itemSlotPredicate, int start, int end, int step) {
        Preconditions.checkArgument(step != 0, "step argument (" + step + ") can't be 0");

        for (int slot = start; slot < end; slot += step) {
            if (itemSlotPredicate.test(this.getInventory().getItem(slot), slot)) {
                this.getInventory().setItem(slot, item);
            }
        }

        return this;
    }

    @Nonnull
    public Menu setIf(@Nullable ItemStack item, @Nonnull BiPredicate<ItemStack, Integer> itemSlotPredicate, int start, int end) {
        return this.setIf(item, itemSlotPredicate, start, end, 1);
    }

    @Nonnull
    public Menu setIf(@Nullable ItemStack item, @Nonnull BiPredicate<ItemStack, Integer> itemSlotPredicate, int start) {
        return this.setIf(item, itemSlotPredicate, start, this.getInventory().getSize());
    }

    @Nonnull
    public Menu setIf(@Nullable ItemStack item, @Nonnull BiPredicate<ItemStack, Integer> itemSlotPredicate) {
        return this.setIf(item, itemSlotPredicate, 0);
    }

    /**
     * Sets a given {@link ItemStack} at every slot in given loop range if the existing {@link ItemStack} matches the
     * given {@link Predicate}.
     *
     * @param item          Nullable (air) {@link ItemStack} to set
     * @param itemPredicate Previous slot {@link ItemStack} condition to match
     * @param start         Inclusive start slot index
     * @param end           Exclusive end slot index
     * @param step          Increment amount
     * @return This instance, useful for chaining
     * @throws IllegalArgumentException If the step argument is 0
     */
    @Nonnull
    public Menu setIf(@Nullable ItemStack item, @Nonnull Predicate<ItemStack> itemPredicate, int start, int end, int step) {
        Preconditions.checkArgument(step != 0, "step argument (" + step + ") can't be 0");

        for (int slot = start; slot < end; slot += step) {
            if (itemPredicate.test(this.getInventory().getItem(slot))) {
                this.getInventory().setItem(slot, item);
            }
        }

        return this;
    }

    @Nonnull
    public Menu setIf(@Nullable ItemStack item, @Nonnull Predicate<ItemStack> itemPredicate, int start, int end) {
        return this.setIf(item, itemPredicate, start, end, 1);
    }

    @Nonnull
    public Menu setIf(@Nullable ItemStack item, @Nonnull Predicate<ItemStack> itemPredicate, int start) {
        return this.setIf(item, itemPredicate, start, this.getInventory().getSize());
    }

    @Nonnull
    public Menu setIf(@Nullable ItemStack item, @Nonnull Predicate<ItemStack> itemPredicate) {
        return this.setIf(item, itemPredicate, 0);
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
    @Nonnull
    public Menu setRange(@Nullable ItemStack item, int start, int end, int step) {
        Preconditions.checkArgument(step != 0, "step argument (" + step + ") can't be 0");

        for (int slot = start; slot < end; slot += step) {
            this.getInventory().setItem(slot, item);
        }

        return this;
    }

    @Nonnull
    public Menu setRange(@Nullable ItemStack item, int start, int end) {
        return this.setRange(item, start, end, 1);
    }

    @Nonnull
    public Menu setRange(@Nullable ItemStack item, int start) {
        return this.setRange(item, start, this.getInventory().getSize());
    }

    /**
     * Modifies an {@link ItemStack} located at a given slot with given operations to perform.
     *
     * @param applyItem Lambda method that'll take an ItemStack as an argument and perform operations on it
     * @param slot      Slot at which an {@link ItemStack} that is being modified is located
     * @return This instance, useful for chaining
     * @throws IndexOutOfBoundsException If the slot argument is out of this inventory's boundaries
     * @throws IllegalArgumentException  If the {@link Consumer}&lt;{@link ItemStack}&gt; argument is null
     */
    @Nonnull
    public Menu changeItem(@Nonnull Consumer<ItemStack> applyItem, int slot) {
        Preconditions.checkArgument(applyItem != null, "Consumer<ItemStack> argument can't be null");
        this.getItem(slot).ifPresent(applyItem);
        return this;
    }

    /**
     * Sets a {@link BukkitTask} that will run until the inventory is closed or a new {@link BukkitTask} is set.
     *
     * <p>The task should be scheduled first and then set, for an example:
     * <pre>{@code
     * BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> player.sendMessage("This menu is currently open!"), 0, 40);
     *
     * menu.setBukkitTask(task);
     * }</pre>
     *
     * @param task The created {@link BukkitTask} that is returned by scheduling it
     * @return This instance, useful for chaining
     */
    @Nonnull
    public Menu setBukkitTask(@Nullable BukkitTask task) {
        if (this.taskId > -1) {
            Bukkit.getScheduler().cancelTask(this.taskId);
        }

        this.taskId = task == null ? -1 : task.getTaskId();
        return this;
    }

    /**
     * Clears the whole inventory of it's {@link ItemStack} contents.
     *
     * @return This instance, useful for chaining
     */
    @Nonnull
    public Menu clearContents() {
        this.getInventory().setStorageContents(new ItemStack[0]);
        return this;
    }

    /**
     * Generally clears the inventory menu of its contents and attributes.
     *
     * @return This instance, useful for chaining
     */
    @Nonnull
    @Override
    public Menu clear() {
        return this.clearContents();
    }

    @Nonnull
    @Override
    public Menu set(@Nullable ItemStack item, @Nonnull int... slots) {
        return (Menu) super.set(item, slots);
    }

    @Nonnull
    @Override
    public Menu open(@Nonnull MenuManager menuManager, @Nonnull Iterable<? extends HumanEntity> viewers) {
        return (Menu) super.open(menuManager, viewers);
    }

    @Nonnull
    @Override
    public Menu open(@Nonnull MenuManager menuManager, @Nonnull HumanEntity... viewers) {
        return (Menu) super.open(menuManager, viewers);
    }

    /**
     * Returns the identifier number of the {@link BukkitTask} that is stored in this instance and running while it's
     * open.
     *
     * @return {@link BukkitTask} identifier number
     */
    public int getBukkitTask() {
        return this.taskId;
    }
}