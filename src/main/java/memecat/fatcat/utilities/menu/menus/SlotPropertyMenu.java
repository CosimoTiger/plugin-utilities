package memecat.fatcat.utilities.menu.menus;

import com.google.common.base.Preconditions;
import memecat.fatcat.utilities.menu.MenuManager;
import memecat.fatcat.utilities.menu.slot.ISlotProperty;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * {@link PropertyMenu} implementation with runnable {@link ISlotProperty}.
 *
 * @param <E> Specific ISlotProperty subclass, or empty ({@link ISlotProperty}), which should allow multiple subclasses
 *           to populate this {@link PropertyMenu}
 */
public class SlotPropertyMenu<E extends ISlotProperty> extends PropertyMenu<ISlotProperty> {

    /**
     * {@inheritDoc}
     */
    public SlotPropertyMenu(@NotNull Inventory inventory, @NotNull MenuManager menuManager) {
        super(inventory, menuManager);
    }

    /**
     * {@inheritDoc} Runs an existing {@link ISlotProperty} and cancels the {@link InventoryClickEvent} if a
     * property doesn't exist. Every existing {@link ISlotProperty} should decide whether to cancel the {@link
     * org.bukkit.event.inventory.InventoryClickEvent} through
     * {@link org.bukkit.event.Cancellable#setCancelled(boolean)}.
     */
    @Override
    public void onClick(@NotNull InventoryClickEvent event, boolean external) {
        super.onClick(event, external);

        // If only those stubborn servers migrated to Java 9 at least so we could simplify it to:
        // getSlotProperty(event.getSlot()).ifPresentOrElse(property -> property.run(event, this), event.setCancelled(true));
        Optional<ISlotProperty> property = getSlotProperty(event.getSlot());

        if (property.isPresent()) {
            property.get().run(event, this);
        } else {
            event.setCancelled(true);
        }
    }

    public SlotPropertyMenu<E> changeProperty(Consumer<ISlotProperty> applyProperty, int slot, Class<E> type) {
        Preconditions.checkArgument(applyProperty != null, "Consumer<ISlotProperty> argument can't be null");
        Preconditions.checkArgument(type != null, "Class<ISlotProperty> argument can't be null");

        getSlotProperty(slot).ifPresent(property -> {
            if (property.getClass().equals(type)) {
                applyProperty.accept(property);
            }
        });

        return this;
    }
}