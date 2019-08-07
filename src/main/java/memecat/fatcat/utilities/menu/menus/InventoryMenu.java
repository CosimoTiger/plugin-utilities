package memecat.fatcat.utilities.menu.menus;

import com.google.common.base.Preconditions;
import memecat.fatcat.utilities.menu.attribute.Rows;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Implementation of the {@link AbstractMenu} class with many useful functions, it can be easily instantiated,
 * subclassed and modified.
 *
 * @author Alan B.
 * @see PropertyMenu
 */
public abstract class InventoryMenu extends AbstractMenu {

    /**
     * Main, constant part of an {@link InventoryMenu} that identifies it
     */
    protected final Inventory inventory;

    /**
     * Amount of rows that exist in this chest inventory, or null if the inventory isn't a chest
     */
    protected Rows rows;

    /**
     * The identifier (ID) of a BukkitTask task that's relevant to this inventory. The task is automatically
     * cancelled when the menu is closed with no viewers left, but this can be modified by overriding the
     * {@link #onClose(InventoryCloseEvent)} method.
     */
    protected int taskId = -1;

    /**
     * Creates a new {@link InventoryMenu} from the given inventory type, holder and display name (title).
     *
     * @param type   Type of an inventory
     * @param holder Object that this inventory belongs to (chest, player, horse..)
     * @param title  Display name of this inventory
     */
    public InventoryMenu(@NotNull InventoryType type, @Nullable InventoryHolder holder, @Nullable String title) {
        inventory = Bukkit.createInventory(holder == null ? this : holder, type, title == null ? type.getDefaultTitle() : title);
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
        inventory = Bukkit.createInventory(holder == null ? this : holder, (this.rows = rows).getSize(), title == null ?
                InventoryType.CHEST.getDefaultTitle() : title);
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
     */
    public InventoryMenu(@NotNull Inventory inventory) {
        if ((this.inventory = inventory) == null) {
            throw new IllegalArgumentException("Inventory argument should never be null");
        }
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
     * This method places an item in the first slot and keeps on adding the skipForSlots amount until the
     * current slot is bigger than toSlot.
     *
     * @param item         Item stack or null
     * @param fromSlot     Beginning index of a slot in an inventory
     * @param toSlot       Ending index of a slot in an inventory
     * @param skipForSlots Amount of slots to be skipped till next item placement
     * @return This instance, useful for chaining
     */
    @NotNull
    public InventoryMenu fillSkip(@Nullable ItemStack item, int fromSlot, int toSlot, int skipForSlots) {
        if (fromSlot > toSlot) {
            throw new IllegalArgumentException("From-slot index argument " + fromSlot + " is greater than to-slot index " + toSlot + " argument.");
        } else if (toSlot > getSize()) {
            toSlot = getSize();
        }

        if (skipForSlots <= 1) {
            for (int i = fromSlot; i < toSlot; i++) {
                setAndUpdate(item, i);
            }
        } else {
            for (int i = fromSlot; i < toSlot; i += skipForSlots) {
                setAndUpdate(item, i);
            }
        }

        return this;
    }

    /**
     * Fills inventory slots with an item from a given beginning slot to a given ending slot (interval).
     * <p>
     * An "interval" in the case of this method can be defined as a set of whole numbers ranging from the given beginning
     * slot index (inclusive) to the given slot index (exclusive). This is referenced to mathematical intervals, or
     * simply shown with symbols: [fromSlot, toSlot&gt; or firstSlot = fromSlot, endSlot = (toSlot - 1)
     *
     * @param item     Item stack or null
     * @param fromSlot Start index location of a slot in an inventory
     * @param toSlot   End index location of a slot in an inventory
     * @return This instance, useful for chaining
     */
    @NotNull
    public InventoryMenu fillInterval(@Nullable ItemStack item, int fromSlot, int toSlot) {
        if (fromSlot > toSlot) {
            throw new IllegalArgumentException("From-slot index argument " + fromSlot + " is greater than to-slot index " + toSlot + " argument.");
        } else if (fromSlot < 0) {
            fromSlot = 0;
        }

        int size = getSize();

        if (toSlot > size) {
            toSlot = size;
        }

        for (int i = fromSlot; i < toSlot; i++) {
            setAndUpdate(item, i);
        }

        return this;
    }

    /**
     * Modifies an ItemStack located at a given slot with given operations to perform.
     *
     * @param applyItem Lambda method that'll take an ItemStack as an argument and perform operations on it
     * @param slot      Slot at which an ItemStack that is being modified is located at
     * @return This instance, useful for chaining
     */
    @NotNull
    public InventoryMenu changeItem(@NotNull Consumer<ItemStack> applyItem, int slot) {
        Preconditions.checkArgument(isSlot(slot), "Consumer<AbstractSlotProperty> argument shouldn't be null");
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
    public InventoryMenu fillAll(@Nullable ItemStack item, boolean replace) {
        if (replace) {
            for (int i = 0; i < getSize(); i++) {
                setAndUpdate(item, i);
            }
        } else {
            for (int i = 0; i < getSize(); i++) {
                if (!getItem(i).isPresent()) {
                    setAndUpdate(item, i);
                }
            }
        }

        return this;
    }

    /**
     * Sets an item stack in a slot in an inventory.
     *
     * @param item  Item stack or null
     * @param slots Index numbers of inventory slots
     * @return This instance, useful for chaining
     */
    @NotNull
    @Override
    public InventoryMenu set(@Nullable ItemStack item, int... slots) {
        for (int i : slots) {
            if (!isSlot(i)) {
                continue;
            }

            setAndUpdate(item, i);
        }

        return this;
    }

    /**
     * Directly sets an inventory item at the appropriate (expected) given slot index.
     *
     * @param item Inventory item stack object
     * @param slot Inventory slot index
     */
    protected void setAndUpdate(@Nullable ItemStack item, int slot) {
        inventory.setItem(slot, item);
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

        if (task == null) {
            taskId = -1;
        } else {
            taskId = task.getTaskId();
        }

        return this;
    }


    /**
     * Clears the whole inventory of it's contents (array of item stacks).
     *
     * @return This instance, useful for chaining
     */
    @NotNull
    public InventoryMenu clearContents() {
        inventory.setStorageContents(new ItemStack[0]);
        return this;
    }

    @NotNull
    @Override
    public InventoryMenu clear() {
        return clearContents();
    }

    /**
     * Returns the existing item stack at the given slot of this inventory menu or else null.
     *
     * @param slot Slot index location of the item in the inventory
     * @return Item stack or null
     */
    @NotNull
    @Override
    public Optional<ItemStack> getItem(int slot) {
        return Optional.ofNullable(isSlot(slot) ? inventory.getItem(slot) : null);
    }

    /**
     * Returns the amount of rows that this {@link InventoryMenu} has.
     *
     * @return {@link Rows} enum containing the amount
     */
    @NotNull
    public final Optional<Rows> getRows() {
        return Optional.ofNullable(rows);
    }

    /**
     * Returns a boolean value indicating whether a given slot index is within this inventory's array bounds.
     *
     * @param slot An integer number
     * @return Whether the given slot number is bigger than -1 and smaller than the size of this inventory
     */
    public final boolean isSlot(int slot) {
        return slot > -1 && slot < getSize();
    }

    @NotNull
    @Override
    public List<HumanEntity> getViewers() {
        return inventory.getViewers();
    }

    @NotNull
    public final InventoryType getType() {
        return inventory.getType();
    }

    @NotNull
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Returns the identifier number of the {@link BukkitTask} that is stored in this instance and running while it's open.
     *
     * @return {@link BukkitTask} identifier number
     */
    public int getBukkitTask() {
        return taskId;
    }

    /**
     * Returns the slot amount that this {@link InventoryMenu} has.
     *
     * @return Amount of slots of this inventory {@link InventoryMenu}
     */
    public final int getSize() {
        return inventory.getSize();
    }
}