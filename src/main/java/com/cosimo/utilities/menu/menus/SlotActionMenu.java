package com.cosimo.utilities.menu.menus;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import javax.annotation.Nonnull;
import java.util.function.BiConsumer;

/**
 * {@link PropertyMenu} implementation with callable {@link BiConsumer} action.
 */
public class SlotActionMenu extends PropertyMenu<BiConsumer<InventoryClickEvent, SlotActionMenu>> {

    public SlotActionMenu(@Nonnull Inventory inventory) {
        super(inventory);
    }

    /**
     * {@inheritDoc} Passes the {@link InventoryClickEvent} to a {@link BiConsumer} at the clicked slot, which is run if
     * it exists, and can freely change the outcome of the same event.
     */
    @Override
    public void onClick(@Nonnull InventoryClickEvent event, boolean external) {
        super.onClick(event, external);
        this.getSlotProperty(event.getSlot()).ifPresent(property -> property.accept(event, this));
    }
}