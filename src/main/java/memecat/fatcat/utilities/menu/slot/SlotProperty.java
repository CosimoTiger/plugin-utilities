package memecat.fatcat.utilities.menu.slot;

import memecat.fatcat.utilities.menu.menus.AbstractMenu;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

/**
 * An implementation subclass of {@link ISlotProperty} that functions with lambdas (anonymous methods) as a way
 * of storing and running an action.
 */
public class SlotProperty implements ISlotProperty {

    private BiConsumer<InventoryClickEvent, AbstractMenu> eventConsumer;

    /**
     * Creates a new {@link SlotProperty} with a given event handler lambda method.
     *
     * @param event Actions that should be executed with the given InventoryClickEvent argument
     */
    public SlotProperty(@NotNull BiConsumer<InventoryClickEvent, AbstractMenu> event) {
        this.eventConsumer = event;
    }

    /**
     * Sets the event handler object to equal to a new, given event handler object.
     *
     * @param event Event handler lambda method containing actions that'll be executed with the given event
     * @return This instance, useful for chaining
     */
    @NotNull
    public SlotProperty setAction(@NotNull BiConsumer<InventoryClickEvent, AbstractMenu> event) {
        this.eventConsumer = event;
        return this;
    }

    /**
     * Returns the {@link BiConsumer}&lt;{@link InventoryClickEvent}, {@link AbstractMenu}&gt; object that contains the
     * actions that'll be run with the given InventoryClickEvent event and menu parameter.
     *
     * @return {@link BiConsumer}&lt;{@link InventoryClickEvent}, {@link AbstractMenu}&gt; runnable object
     */
    @NotNull
    public BiConsumer<InventoryClickEvent, AbstractMenu> getAction() {
        return this.eventConsumer;
    }

    /**
     * Runs the {@link #getAction()} object (if not null) with the given {@link InventoryClickEvent} and {@link
     * AbstractMenu} argument.
     *
     * @param event {@link InventoryClickEvent} inventory event
     * @param menu  {@link AbstractMenu} that the {@link InventoryClickEvent} is referring to
     * @throws IllegalArgumentException If {@link InventoryClickEvent} or {@link AbstractMenu} argument is null
     */
    public void run(@NotNull InventoryClickEvent event, @NotNull AbstractMenu menu) {
        this.getAction().accept(event, menu);
    }
}