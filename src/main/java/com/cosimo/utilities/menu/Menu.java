package com.cosimo.utilities.menu;

import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

/**
 * The default implementation of the {@link AbstractMenu} class.
 *
 * @author CosimoTiger
 * @see PropertyMenu
 */
public class Menu extends AbstractMenu {

    /**
     * The identifier number of a {@link BukkitTask} that's relevant to this inventory. The task is by default
     * automatically cancelled when the menu is closed with no viewers left, but this can be modified by overriding the
     * {@link #onClose(InventoryCloseEvent)} method. Example use is a constant animation that's setting new
     * {@link ItemStack}s in the menu.
     */
    protected int taskID = -1;

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

    /**
     * Sets an {@link ItemStack} from a given inclusive starting slot index to the end of {@link Inventory#getSize()}.
     *
     * @param item  Nullable {@link ItemStack} to set in slots
     * @param start Positive inclusive starting slot index
     * @return This instance, useful for chaining
     */
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
    @Nonnull
    public Menu setBukkitTask(@Nullable BukkitTask task) {
        if (this.taskID > -1) {
            Bukkit.getScheduler().cancelTask(this.taskID);
        }

        this.taskID = task == null ? -1 : task.getTaskId();
        return this;
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