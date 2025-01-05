package com.cosimo.utilities.menu.type.action;

import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.function.BiConsumer;

public interface MenuAction extends BiConsumer<InventoryClickEvent, ActionMenu> {
}