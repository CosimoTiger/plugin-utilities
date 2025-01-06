package com.cosimo.utilities.menu.type.action;

import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.function.BiConsumer;

/**
 * A {@link MenuAction} is a {@link BiConsumer} that takes an {@link InventoryClickEvent} and this menu, allowing for
 * dynamic behavior when slots are clicked.
 */
public interface MenuAction extends BiConsumer<InventoryClickEvent, ActionMenu> {
}