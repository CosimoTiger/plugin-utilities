package memecat.fatcat.utilities.menu.menus;

import com.google.common.base.Preconditions;
import memecat.fatcat.utilities.menu.MenuManager;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Instantiable implementation of the {@link AbstractMenu} class.
 *
 * @author Alan B.
 * @see PropertyMenu
 */
public class InventoryMenu extends AbstractMenu {

    /**
     * The identifier number of a BukkitTask task that's relevant to this inventory. The task is by default
     * automatically cancelled when the menu is closed with no viewers left, but this can be modified by overriding the
     * {@link #onClose(InventoryCloseEvent)} method.
     */
    protected int taskId = -1;

    /**
     * {@inheritDoc} Creates a new {@link InventoryMenu} using the default constructor for {@link AbstractMenu}.
     */
    public InventoryMenu(@NotNull Inventory inventory, @NotNull MenuManager menuManager) {
        super(inventory, menuManager);
    }

    @Override
    public void onClose(@NotNull InventoryCloseEvent event) {
        if (getInventory().getViewers().size() < 2) {
            setBukkitTask(null);
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
     * @throws IndexOutOfBoundsException If the from-slot or to-slot argument isn't within the inventory's boundaries
     * @throws IllegalArgumentException  If the from-slot is greater than the to-slot argument or the skipForSlots
     *                                   argument is lower than 1
     */
    @NotNull
    public InventoryMenu fillSkip(@Nullable ItemStack item, int fromSlot, int toSlot, int skipForSlots) {
        checkRange(fromSlot, toSlot, getInventory().getSize());
        Preconditions.checkArgument(skipForSlots > 0, "Skip-for-slots argument (" + skipForSlots + ") can't be smaller than 1");

        for (int slot = fromSlot; slot < toSlot; slot += skipForSlots) {
            getInventory().setItem(slot, item);
        }

        return this;
    }

    /**
     * Fills inventory slots with an {@link ItemStack} from a given beginning slot to a given ending slot (interval).
     * <p>
     * An "interval" in the case of this method can be defined as a set of whole numbers ranging from the given
     * beginning slot index (inclusive) to the given slot index (exclusive). This is referenced to mathematical
     * intervals, or simply shown with symbols: [fromSlot, toSlot&gt; or firstSlot = fromSlot, endSlot = (toSlot - 1)
     *
     * @param item     {@link ItemStack} or null
     * @param fromSlot Start index location of a slot in an inventory
     * @param toSlot   End index location of a slot in an inventory
     * @return This instance, useful for chaining
     * @throws IndexOutOfBoundsException If the from-slot or to-slot argument isn't within the inventory's boundaries
     * @throws IllegalArgumentException  If the from-slot is greater than the to-slot argument
     */
    @NotNull
    public InventoryMenu fillInterval(@Nullable ItemStack item, int fromSlot, int toSlot) {
        return fillSkip(item, fromSlot, toSlot, 1);
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
    @NotNull
    public InventoryMenu changeItem(@NotNull Consumer<ItemStack> applyItem, int slot) {
        Preconditions.checkArgument(applyItem != null, "Consumer<ItemStack> argument can't be null");
        getItem(slot).ifPresent(applyItem);
        return this;
    }

    /**
     * Sets all or only empty inventory slots to equal to the given {@link ItemStack}.
     *
     * @param item    {@link ItemStack} that will be in all inventory slots
     * @param replace Whether existing {@link ItemStack}s should be replaced with a new one
     * @return This instance, useful for chaining
     */
    @NotNull
    public InventoryMenu fill(@Nullable ItemStack item, boolean replace) {
        if (replace) {
            for (int slot = 0; slot < getInventory().getSize(); slot++) {
                getInventory().setItem(slot, item);
            }
        } else {
            for (int slot = 0; slot < getInventory().getSize(); slot++) {
                if (getInventory().getItem(slot) == null) {
                    getInventory().setItem(slot, item);
                }
            }
        }

        return this;
    }

    /**
     * Sets a {@link BukkitTask} that will run until the inventory is closed or a new {@link BukkitTask} is set.
     * <p>
     * The task should be scheduled first and then set, as an example:
     * <pre>
     *     {@code
     * BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> player.sendMessage("This menu is currently open!"), 0, 40);
     *
     * menu.setBukkitTask(task);
     * }
     * </pre>
     *
     * @param task The created {@link BukkitTask} that is returned by scheduling it
     * @return This instance, useful for chaining
     */
    @NotNull
    public InventoryMenu setBukkitTask(@Nullable BukkitTask task) {
        if (taskId > -1) {
            Bukkit.getScheduler().cancelTask(taskId);
        }

        taskId = task == null ? -1 : task.getTaskId();

        return this;
    }

    public static void checkRange(int from, int to, int size) {
        if (from > to) {
            throw new IllegalArgumentException("From-slot argument (" + from + ") can't be greater than to-slot argument (" + to + ")");
        } else if (from < 0) {
            throw new IndexOutOfBoundsException("From-slot argument (" + from + ") can't be smaller than 0");
        } else if (from > size) {
            throw new IndexOutOfBoundsException("From-slot argument (" + from + ") can't be greater than the inventory size (" + size + ")");
        } else if (to > size) {
            throw new IndexOutOfBoundsException("To-slot argument (" + from + ") can't be greater than the inventory size (" + size + ")");
        }
    }

    public static void checkElement(int index, int size) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("Slot index argument (" + index + ") can't be smaller than 0");
        } else if (index >= size) {
            throw new IndexOutOfBoundsException("Slot index argument (" + index + ") can't be greater or equal to the size");
        }
    }

    /**
     * Clears the whole inventory of it's {@link ItemStack} contents.
     *
     * @return This instance, useful for chaining
     */
    @NotNull
    public InventoryMenu clearContents() {
        getInventory().setStorageContents(new ItemStack[0]);
        return this;
    }

    @NotNull
    @Override
    public InventoryMenu clear() {
        return clearContents();
    }

    /**
     * Returns the identifier number of the {@link BukkitTask} that is stored in this instance and running while it's
     * open.
     *
     * @return {@link BukkitTask} identifier number
     */
    public int getBukkitTask() {
        return taskId;
    }
}