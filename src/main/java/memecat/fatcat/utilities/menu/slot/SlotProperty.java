package memecat.fatcat.utilities.menu.slot;

import memecat.fatcat.utilities.menu.menus.AbstractMenu;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

/**
 * An implementation subclass of {@link AbstractSlotProperty} that functions with lambdas (anonymous methods) as a way
 * of storing and running an action.
 */
public class SlotProperty extends AbstractSlotProperty {

    private BiConsumer<InventoryClickEvent, AbstractMenu> eventConsumer;

    /**
     * Creates a new {@link SlotProperty} with a given event handler lambda method.
     *
     * @param event Actions that should be executed with the given InventoryClickEvent argument
     */
    public SlotProperty(@Nullable BiConsumer<InventoryClickEvent, AbstractMenu> event) {
        this.eventConsumer = event;
    }

    /**
     * Sets the event handler object to equal to a new, given event handler object.
     *
     * @param event Event handler lambda method containing actions that'll be executed with the given event
     * @return This instance, useful for chaining
     */
    @NotNull
    public SlotProperty setAction(@Nullable BiConsumer<InventoryClickEvent, AbstractMenu> event) {
        this.eventConsumer = event;
        return this;
    }

    /**
     * Returns the BiConsumer&lt;InventoryClickEvent, AbstractMenu&gt; object that contains the actions that'll be run
     * with the given InventoryClickEvent event and menu parameter.
     *
     * @return BiConsumer&lt;InventoryClickEvent, AbstractMenu&gt; runnable object
     */
    @Nullable
    public BiConsumer<InventoryClickEvent, AbstractMenu> getAction() {
        return eventConsumer;
    }

    /**
     * Runs the BiConsumer&lt;InventoryClickEvent, AbstractMenu&gt; object with the given event and menu parameter, if
     * arguments and event handler object aren't NULL.
     *
     * @param event InventoryClickEvent inventory event
     */
    public void run(@Nullable InventoryClickEvent event, @Nullable AbstractMenu menu) {
        if (getAction() != null && event != null && menu != null) {
            getAction().accept(event, menu);
        }
    }
}