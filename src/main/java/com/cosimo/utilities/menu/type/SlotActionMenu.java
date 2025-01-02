package com.cosimo.utilities.menu.type;

import lombok.NonNull;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.function.BiConsumer;

/**
 * {@link PropertyMenu} implementation with a callable {@link BiConsumer} property.
 */
public class SlotActionMenu extends PropertyMenu<BiConsumer<InventoryClickEvent, SlotActionMenu>> {

    /**
     * {@inheritDoc}
     */
    public SlotActionMenu(@NonNull Inventory inventory) {
        super(inventory);
    }

    /**
     * {@inheritDoc} Passes the {@link InventoryClickEvent} to a {@link BiConsumer} at the clicked slot, which is run if
     * it exists, and can freely change the outcome of the same event.
     */
    @Override
    public void onClick(@NonNull InventoryClickEvent event) {
        super.onClick(event);
        this.getProperty(event.getSlot()).ifPresent(property -> property.accept(event, this));
    }
}