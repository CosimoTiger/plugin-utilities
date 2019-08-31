package memecat.fatcat.utilities.menu.menus;

import com.google.common.base.Preconditions;
import memecat.fatcat.utilities.menu.attribute.Rows;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Implementation of the {@link AbstractMenu} class.
 *
 * @author Alan B.
 * @see PropertyMenu
 */
public class InventoryMenu extends AbstractMenu {

    /**
     * Amount of rows that exist in this chest inventory, or null if the inventory isn't a chest
     */
    private final Rows rows;

    /**
     * The identifier (ID) of a BukkitTask task that's relevant to this inventory. The task is automatically cancelled
     * when the menu is closed with no viewers left, but this can be modified by overriding the {@link
     * #onClose(InventoryCloseEvent)} method.
     */
    private int taskId = -1;

    /**
     * Creates a new {@link InventoryMenu} from the given inventory type, holder and display name (title).
     *
     * @param type   Type of an inventory
     * @param holder Object that this inventory belongs to (chest, player, horse..)
     * @param title  Display name of this inventory
     */
    public InventoryMenu(@NotNull InventoryType type, @Nullable InventoryHolder holder, @Nullable String title) {
        super(Bukkit.createInventory(holder, type, title == null ? type.getDefaultTitle() : title));
        rows = null;
    }

    /**
     * Creates a new {@link InventoryMenu} from the given inventory type and holder.
     *
     * @param type   Type of an inventory
     * @param holder Object that this inventory belongs to (chest, player, horse..)
     */
    public InventoryMenu(@NotNull InventoryType type, @Nullable InventoryHolder holder) {
        this(type, holder, null);
    }

    /**
     * Creates a new {@link InventoryMenu} from the given inventory type and display name (title).
     *
     * @param type  Type of an inventory
     * @param title Display name of this inventory
     */
    public InventoryMenu(@NotNull InventoryType type, @Nullable String title) {
        this(type, null, title);
    }

    /**
     * Creates a new {@link InventoryMenu} from the given inventory type.
     *
     * @param type Type of an inventory
     */
    public InventoryMenu(@NotNull InventoryType type) {
        this(type, null, null);
    }

    /**
     * Creates a new chest {@link InventoryMenu} from the given amount of rows, it's holder and display name (title).
     *
     * @param rows   {@link Rows} enum, amount of rows in this chest inventory
     * @param holder Object that this inventory belongs to (chest, player, horse..)
     * @param title  Display name of this inventory
     */
    public InventoryMenu(@NotNull Rows rows, @Nullable InventoryHolder holder, @Nullable String title) {
        super(Bukkit.createInventory(holder, rows.getSize(), title == null ?
                InventoryType.CHEST.getDefaultTitle() : title));
        this.rows = rows;
    }

    /**
     * Creates a new chest {@link InventoryMenu} from the given amount of rows and it's holder.
     *
     * @param rows   {@link Rows} enum, amount of rows in this chest inventory
     * @param holder Object that this inventory belongs to (chest, player, horse..)
     */
    public InventoryMenu(@NotNull Rows rows, @Nullable InventoryHolder holder) {
        this(rows, holder, null);
    }

    /**
     * Creates a new chest {@link InventoryMenu} from the given amount of rows and display name (title).
     *
     * @param rows  {@link Rows} enum, amount of rows in this chest inventory
     * @param title Display name of this inventory
     */
    public InventoryMenu(@NotNull Rows rows, @Nullable String title) {
        this(rows, null, title);
    }

    /**
     * Creates a new chest {@link InventoryMenu} from the given amount of rows.
     *
     * @param rows {@link Rows} enum, amount of rows in this chest inventory
     */
    public InventoryMenu(@NotNull Rows rows) {
        this(rows, null, null);
    }

    /**
     * Creates a new {@link InventoryMenu} with the given inventory and it's attributes equal to it.
     *
     * @param inventory Inventory that'll function as a menu
     * @throws IllegalArgumentException If the inventory argument is null or already has an {@link AbstractMenu} as it's
     *                                  {@link InventoryHolder}
     */
    public InventoryMenu(@NotNull Inventory inventory) {
        super(inventory);
        rows = Rows.fromInventory(getInventory()).orElse(null);
    }

    @Override
    public void onClose(@NotNull InventoryCloseEvent event) {
        if (getViewers().size() < 2) {
            setBukkitTask(null);
        }
    }

    /**
     * Fills inventory slots with an item by skipping an amount of given slots from a start to the end.
     * <p>
     * This method places an item in the first slot and keeps on adding the skipForSlots amount until the current slot
     * is bigger than toSlot.
     *
     * @param item         Item stack or null
     * @param fromSlot     Beginning index of a slot in an inventory
     * @param toSlot       Ending index of a slot in an inventory
     * @param skipForSlots Amount of slots to be skipped till next item placement
     * @return This instance, useful for chaining
     */
    @NotNull
    public InventoryMenu fillSkip(@Nullable ItemStack item, int fromSlot, int toSlot, int skipForSlots) {
        checkRange(fromSlot, toSlot, getSize());
        Preconditions.checkArgument(skipForSlots > 0, "Skip-for-slots argument can't be smaller than 1");

        for (int slot = fromSlot; slot < toSlot; slot += skipForSlots) {
            getInventory().setItem(slot, item);
        }

        return this;
    }

    /**
     * Fills inventory slots with an item from a given beginning slot to a given ending slot (interval).
     * <p>
     * An "interval" in the case of this method can be defined as a set of whole numbers ranging from the given
     * beginning slot index (inclusive) to the given slot index (exclusive). This is referenced to mathematical
     * intervals, or simply shown with symbols: [fromSlot, toSlot&gt; or firstSlot = fromSlot, endSlot = (toSlot - 1)
     *
     * @param item     Item stack or null
     * @param fromSlot Start index location of a slot in an inventory
     * @param toSlot   End index location of a slot in an inventory
     * @return This instance, useful for chaining
     */
    @NotNull
    public InventoryMenu fillInterval(@Nullable ItemStack item, int fromSlot, int toSlot) {
        checkRange(fromSlot, toSlot, getSize());

        for (int slot = fromSlot; slot < toSlot; slot++) {
            getInventory().setItem(slot, item);
        }

        return this;
    }

    /**
     * Modifies an ItemStack located at a given slot with given operations to perform.
     *
     * @param applyItem Lambda method that'll take an ItemStack as an argument and perform operations on it
     * @param slot      Slot at which an ItemStack that is being modified is located at
     * @return This instance, useful for chaining
     * @throws IndexOutOfBoundsException If the slot argument is out of this inventory's array boundaries
     * @throws IllegalArgumentException  If the Consumer&lt;ItemStack&gt; argument is null
     */
    @NotNull
    public InventoryMenu changeItem(@NotNull Consumer<ItemStack> applyItem, int slot) {
        Preconditions.checkArgument(applyItem != null, "Consumer<ItemStack> argument can't be null");
        getItem(slot).ifPresent(applyItem);
        return this;
    }

    /**
     * Sets all or only empty inventory slots to equal to the given item.
     *
     * @param item    Item stack that will be in all inventory slots
     * @param replace Whether existing items should be replaced with a new one
     * @return This instance, useful for chaining
     */
    @NotNull
    public InventoryMenu fill(@Nullable ItemStack item, boolean replace) {
        if (replace) {
            for (int slot = 0; slot < getSize(); slot++) {
                getInventory().setItem(slot, item);
            }
        } else {
            for (int slot = 0; slot < getSize(); slot++) {
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

    protected static void checkRange(int from, int to, int size) {
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

    protected static void checkElement(int index, int size) {
        if (index < 0) {
            throw new IllegalArgumentException("Slot index argument (" + index + ") can't be smaller 0");
        } else if (index >= size) {
            throw new IllegalArgumentException("Slot index argument (" + index + ") can't be greater or equal to the size");
        }
    }

    /**
     * Clears the whole inventory of it's contents (array of item stacks).
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
     * Returns the amount of rows that this {@link AbstractMenu} has.
     *
     * @return {@link Optional} of nullable {@link Rows} enum
     */
    @NotNull
    public Optional<Rows> getRows() {
        return Optional.ofNullable(rows);
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