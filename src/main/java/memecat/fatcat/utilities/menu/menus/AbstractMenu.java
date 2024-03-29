package memecat.fatcat.utilities.menu.menus;

import com.google.common.base.Preconditions;
import memecat.fatcat.utilities.menu.MenuManager;
import memecat.fatcat.utilities.menu.slot.ISlotProperty;
import memecat.fatcat.utilities.menu.slot.SlotProperty;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a collection of functional actions and features of an inventory that multiple viewers can see and interact
 * with, containing many different features that can be customised and extended by a developer.
 *
 * <p>A developer can subclass this class, override the methods or add them to customise the ways of processing inputs
 * for inventory menu events or modifying the inventories.
 *
 * @author Alan B. | FatCat
 * @see Menu
 * @see PropertyMenu
 */
public abstract class AbstractMenu {

    /**
     * Main, constant part of an {@link AbstractMenu} that identifies it.
     */
    private final Inventory inventory;

    /**
     * A {@link MenuManager} instance that is available for the control of this {@link AbstractMenu}.
     */
    private MenuManager menuManager;

    /**
     * The default constructor for all subclasses.
     *
     * @param inventory   Not null {@link Inventory} that will be wrapped and controlled by an {@link AbstractMenu}
     * @param menuManager Not null {@link MenuManager} that will be used for (un)registering this {@link AbstractMenu}
     *                    and passing events to it
     * @throws IllegalArgumentException If the {@link Inventory} or {@link MenuManager} argument is null
     */
    public AbstractMenu(@Nonnull Inventory inventory, @Nonnull MenuManager menuManager) {
        Preconditions.checkArgument(inventory != null, "Inventory argument can't be null");
        Preconditions.checkArgument(menuManager != null, "MenuManager argument can't be null");

        this.inventory = inventory;
        this.menuManager = menuManager;
    }

    /**
     * Acts as an event handler for the movement of an {@link org.bukkit.inventory.ItemStack} from source to
     * destination inventory.
     *
     * <p>This typically happens when listening to a container inventory; example of a situation is .</p>
     *
     * @param event         {@link InventoryMoveItemEvent} event
     * @param isDestination Whether this {@link org.bukkit.inventory.Inventory} is equal to the {@link
     *                      InventoryMoveItemEvent#getDestination()}
     */
    public void onItemMove(@Nonnull InventoryMoveItemEvent event, boolean isDestination) {
        event.setCancelled(true);
    }

    /**
     * Handles any {@link InventoryClickEvent} related to this inventory.
     *
     * <p>By default, the {@link InventoryAction} {@code COLLECT_TO_CURSOR} and {@code MOVE_TO_OTHER_INVENTORY} are
     * cancelled and any menu action is cancelled from interaction, but interaction with one's personal inventory is
     * allowed.
     *
     * @param event    {@link InventoryClickEvent} event
     * @param external Whether the clicked inventory is not this one, possibly not any (outside)
     * @see ISlotProperty
     * @see SlotProperty
     */
    public void onClick(@Nonnull InventoryClickEvent event, boolean external) {
        InventoryAction action = event.getAction();
        if (action == InventoryAction.COLLECT_TO_CURSOR || action == InventoryAction.MOVE_TO_OTHER_INVENTORY
                || !external) {
            event.setCancelled(true);
        }
    }

    /**
     * Handles the {@link PluginDisableEvent} of the {@link MenuManager} this {@link AbstractMenu} is registered in.
     *
     * <p>{@link AbstractMenu}s should always close themselves during this event because their reference is mostly lost -
     * {@link org.bukkit.plugin.Plugin}s are disabled on restart, their reference is lost and a totally new object is
     * created on reload. The same happens with {@link MenuManager} as they are connected to their
     * {@link org.bukkit.plugin.Plugin}s.
     *
     * @param event {@link PluginDisableEvent} event
     */
    public void onDisable(@Nonnull PluginDisableEvent event) {
        this.close();
    }

    /**
     * Handles the inventory close events of an inventory.
     *
     * <p>Can open new menus: for the given {@link InventoryCloseEvent} of a
     * {@link HumanEntity}, perform that task on the next tick. For an example, to reopen this menu: {@code
     * Bukkit.getScheduler().runTask(plugin, () -> open(event.getPlayer()))}.
     *
     * @param event {@link InventoryCloseEvent} event
     */
    public void onClose(@Nonnull InventoryCloseEvent event) {
        if (this.getInventory().getViewers().size() < 2) {
            this.getManager().unregisterMenu(this);
        }
    }

    /**
     * Acts as an event handler for the inventory item dragging event.
     *
     * <p>By default, any item dragging in this {@link AbstractMenu} will be cancelled.
     *
     * @param event {@link InventoryDragEvent} event
     */
    public void onDrag(@Nonnull InventoryDragEvent event) {
        int size = event.getView().getTopInventory().getSize();

        for (int slot : event.getRawSlots()) {
            if (slot < size) {
                event.setCancelled(true);
                break;
            }
        }
    }

    /**
     * Acts as an event handler for the inventory opening event.
     *
     * @param event {@link InventoryOpenEvent} event
     */
    public void onOpen(@Nonnull InventoryOpenEvent event) {
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
    @Nonnull
    public AbstractMenu open(@Nonnull Iterable<? extends HumanEntity> viewers) {
        Preconditions.checkState(this.menuManager.isRegistered(),
                "MenuManager has no enabled plugin registered to handle inventory menu events");
        Preconditions.checkArgument(viewers != null,
                "Collection<? extends HumanEntity> of viewers argument can't be null");

        final AtomicInteger count = new AtomicInteger();
        this.getManager().registerMenu(this);
        viewers.forEach(viewer -> {
            Preconditions.checkArgument(viewer != null, "HumanEntity in an Iterable of viewers argument can't be null");
            viewer.openInventory(this.getInventory());
            count.getAndIncrement();
        });

        if (count.get() == 0) {
            this.getManager().unregisterMenu(this);
        }

        return this;
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
    @Nonnull
    public AbstractMenu set(@Nullable ItemStack item, int... slots) {
        Preconditions.checkArgument(slots != null, "Array of slots can't be null");
        int size = this.getInventory().getSize();

        for (int slot : slots) {
            Menu.checkElement(slot, size);
            this.getInventory().setItem(slot, item);
        }

        return this;
    }

    /**
     * Unregisters this {@link AbstractMenu} from its previous {@link MenuManager}, sets and registers itself to the
     * new, given {@link MenuManager}.
     *
     * @param menuManager Not null {@link MenuManager} that this {@link AbstractMenu} will switch over to
     * @return Previous {@link MenuManager} of this {@link AbstractMenu}
     * @throws IllegalArgumentException If the {@link MenuManager} argument is null
     */
    @Nonnull
    public AbstractMenu setManager(@Nonnull MenuManager menuManager) {
        Preconditions.checkArgument(menuManager != null, "MenuManager argument can't be null");

        this.menuManager.unregisterMenu(this);
        this.menuManager = menuManager;
        menuManager.registerMenu(this);

        return this;
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
    @Nonnull
    public AbstractMenu open(@Nonnull HumanEntity... viewers) {
        return this.open(Arrays.asList(viewers));
    }

    /**
     * Closes all {@link AbstractMenu}s of this instance for all viewers who are viewing it.
     *
     * <p>Closing an {@link AbstractMenu} for a {@link HumanEntity} might not always work because their {@link
     * #onClose(InventoryCloseEvent)} can choose to open a new {@link AbstractMenu}, possibly the same one.
     * <p>Remember to schedule this action for the next tick if you're running it inside an inventory event handler!
     *
     * @return This instance, useful for chaining
     */
    @Nonnull
    public AbstractMenu close() {
        new ArrayList<>(this.getInventory().getViewers()).forEach(HumanEntity::closeInventory);
        return this;
    }

    /**
     * Clears all of this inventory menu's contents.
     *
     * @return This instance, useful for chaining
     */
    @Nonnull
    public AbstractMenu clear() {
        this.getInventory().clear();
        return this;
    }

    /**
     * Returns an ItemStack at the given slot of this inventory menu or null if it doesn't exist.
     *
     * @param slot Slot index location of the item in the inventory
     * @return {@link Optional} of a nullable {@link ItemStack}
     * @throws IndexOutOfBoundsException If the given slot argument is out of the inventory's array bounds
     */
    @Nonnull
    public Optional<ItemStack> getItem(int slot) {
        Menu.checkElement(slot, this.getInventory().getSize());
        return Optional.ofNullable(this.getInventory().getItem(slot));
    }

    /**
     * Returns the {@link Inventory} that's wrapped and controlled by this {@link AbstractMenu}.
     *
     * @return Always the same {@link Inventory}
     */
    @Nonnull
    public final Inventory getInventory() {
        return this.inventory;
    }

    /**
     * Returns the {@link MenuManager} that is used by this {@link AbstractMenu} to (un)register itself, listen to
     * events and access other {@link AbstractMenu}s.
     *
     * @return The {@link MenuManager} this inventory is or will be registered in
     */
    @Nonnull
    public MenuManager getManager() {
        return this.menuManager;
    }
}