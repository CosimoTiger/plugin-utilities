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
import java.util.function.Consumer;

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

    /**
     * Fills inventory slots with an {@link ItemStack} by skipping an amount of given slots from a start to the end.
     * <p>
     * This method places an {@link ItemStack} in the first slot and keeps on adding the skipForSlots amount until the
     * current slot is bigger than toSlot.
     *
     * @param item         {@link ItemStack} or null
     * @param fromSlot     Beginning index of a slot in an inventory
     * @param toSlot       Ending index of a slot in an inventory
     * @param skipForSlots Amount of slots to be skipped until next {@link ItemStack} placement
     * @return This instance, useful for chaining
     * @throws IndexOutOfBoundsException If the fromSlot or toSlot argument isn't within the inventory's boundaries
     * @throws IllegalArgumentException  If the fromSlot is greater than the toSlot argument or the skipForSlots
     *                                   argument is lower than 1
     */
    @Nonnull
    public Menu fillSkip(@Nullable ItemStack item, int fromSlot, int toSlot, int skipForSlots) {
        Preconditions.checkArgument(skipForSlots > 0, "skipForSlots argument (" + skipForSlots + ") can't be smaller than 1");
        fromSlot = Math.max(0, fromSlot);
        toSlot = Math.min(this.getInventory().getSize() - 1, toSlot);

        for (int slot = fromSlot; slot < toSlot; slot += skipForSlots) {
            this.getInventory().setItem(slot, item);
        }

        return this;
    }

    /**
     * Fills inventory slots with an {@link ItemStack} from a given beginning slot to a given ending slot (interval).
     *
     * <p>An "interval" in the case of this method can be defined as a set of whole numbers ranging from the given
     * beginning slot index (inclusive) to the given slot index (exclusive). This is referenced to mathematical
     * intervals, or simply shown with symbols: [fromSlot, toSlot&gt; or firstSlot = fromSlot, endSlot = (toSlot - 1)
     *
     * @param item     {@link ItemStack} or null
     * @param fromSlot Start index location of a slot in an inventory
     * @param toSlot   End index location of a slot in an inventory
     * @return This instance, useful for chaining
     * @throws IndexOutOfBoundsException If the fromSlot or toSlot argument isn't within the inventory's boundaries
     * @throws IllegalArgumentException  If the fromSlot is greater than the toSlot argument
     */
    @Nonnull
    public Menu fillInterval(@Nullable ItemStack item, int fromSlot, int toSlot) {
        return this.fillSkip(item, fromSlot, toSlot, 1);
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
     * Sets all or only empty inventory slots to equal to the given {@link ItemStack}.
     *
     * @param item    {@link ItemStack} that will be in all inventory slots
     * @param replace Whether existing {@link ItemStack}s should be replaced with a new one
     * @return This instance, useful for chaining
     */
    @Nonnull
    public Menu fill(@Nullable ItemStack item, boolean replace) {
        if (replace) {
            for (int slot = 0; slot < this.getInventory().getSize(); slot++) {
                this.getInventory().setItem(slot, item);
            }
        } else {
            for (int slot = 0; slot < this.getInventory().getSize(); slot++) {
                if (this.getInventory().getItem(slot) == null) {
                    this.getInventory().setItem(slot, item);
                }
            }
        }

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
     * Package-protected methods for reuse.
     */
    static void checkElement(int index, int size) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("Slot index argument (" + index + ") can't be smaller than 0");
        } else if (index >= size) {
            throw new IndexOutOfBoundsException("Slot index argument (" + index + ") can't be greater than or equal to the size");
        }
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
    public Menu set(@Nullable ItemStack item, int... slots) {
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