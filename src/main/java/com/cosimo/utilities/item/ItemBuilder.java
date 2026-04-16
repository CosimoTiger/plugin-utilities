package com.cosimo.utilities.item;

import lombok.NonNull;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A proxy class (rather than a true builder) that wraps around an {@link ItemStack}, allowing easier, more fluent and
 * chained modifications of the item. Supports Kyori Adventure API {@link Component} for titles and lore instead of
 * plain Strings, allowing for rich text formatting with colors, hover events, click events, and other modern Minecraft
 * text features.
 */
@NullMarked
@SuppressWarnings("unused")
public class ItemBuilder implements Cloneable {

    /**
     * Mutable {@link ItemStack}.
     */
    private ItemStack itemStack;

    /**
     * Creates a new {@link ItemBuilder} from an item stored in a given configuration file's path or a default provided
     * one in case of non-existence of path or object at the path.
     *
     * @param configuration Typically a YAML configuration file in which the item is stored
     * @param path          Path at which the item is stored at
     * @param failure       {@link ItemStack} object that will be used instead if the configuration value is
     *                      non-existent
     */
    public ItemBuilder(@NonNull ConfigurationSection configuration, @NonNull String path,
                       @NonNull Supplier<ItemStack> failure) {
        this(Objects.requireNonNull(Objects.requireNonNullElseGet(configuration.getItemStack(path), failure),
                                    "FileConfiguration or the resulting ItemStack can't be null"));
    }

    /**
     * Creates a new {@link ItemBuilder} from an item stored in a given configuration file's path or a default provided
     * one in case of non-existence of path or object at the path.
     *
     * @param configuration Typically a YAML configuration file in which the item is stored
     * @param path          Path at which the item is stored at
     * @param failure       {@link ItemStack} object that will be used instead if the configuration value is
     *                      non-existent
     */
    public ItemBuilder(@NonNull ConfigurationSection configuration, @NonNull String path, @Nullable ItemStack failure) {
        this(Objects.requireNonNull(configuration.getItemStack(path, failure),
                                    "FileConfiguration or the resulting ItemStack can't be null"));
    }

    /**
     * Creates a new {@link ItemBuilder} from an item stored in a given configuration file's path.
     *
     * @param configuration Typically a YAML configuration file in which the item is stored
     * @param path          Path at which the item is stored at
     */
    public ItemBuilder(@NonNull ConfigurationSection configuration, @NonNull String path) {
        this(configuration, path, new ItemStack(Material.AIR));
    }

    /**
     * Creates a new {@link ItemBuilder} from the given material, amount, and title of a new item being created.
     *
     * @param material Type of item
     * @param amount   Number of items in a stack
     * @param title    Custom name or visible title of an item using Adventure Component
     */
    public ItemBuilder(Material material, int amount, @Nullable Component title) {
        this(ItemStack.of(material, amount), title);
    }

    /**
     * Creates a new {@link ItemBuilder} from the given item, renaming it to a given title.
     *
     * @param itemStack {@link ItemStack} that's being wrapped by this class for further modification
     * @param title     Custom name or visible title of an item using Adventure Component
     * @throws IllegalArgumentException If the ItemStack argument is null
     */
    public ItemBuilder(ItemStack itemStack, @Nullable Component title) {
        this(itemStack);
        this.customName(title);
    }

    /**
     * Creates a new {@link ItemBuilder} from the given material and title of a new item being created, with an amount
     * of 1.
     *
     * @param material Type of item
     * @param title    Custom name or visible title of an item using Adventure Component
     */
    public ItemBuilder(Material material, @Nullable Component title) {
        this(material, 1, title);
    }

    /**
     * Creates a new {@link ItemBuilder} from the given material and amount of a new item being created, with a default
     * material title.
     *
     * @param material Type of item
     * @param amount   Amount of items in a stack
     */
    public ItemBuilder(Material material, int amount) {
        this(material, amount, null);
    }

    /**
     * Creates a new {@link ItemBuilder} from the given material, with an amount of 1 and default material title.
     *
     * @param material Type of item
     */
    public ItemBuilder(Material material) {
        this(material, 1, null);
    }

    /**
     * Creates a new {@link ItemBuilder} from a given {@link ItemStack}.
     *
     * @param itemStack {@link ItemStack} that's being wrapped by this class for further modification
     * @throws IllegalArgumentException If the ItemStack argument is null or of {@link Material#AIR} type
     */
    public ItemBuilder(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    /**
     * Modifies the {@link ItemStack}'s subtype of {@link ItemMeta} with given operations, possibly specific for that
     * subclass.
     *
     * @param metaClass    Class that belongs to the {@link ItemMeta} subtype
     * @param metaConsumer Consumer or anonymous function that'll take this instance's item's specific {@link ItemMeta}
     *                     as an argument
     * @param <T>          {@link ItemMeta} subclass type
     * @return This instance, useful for chaining
     * @throws IllegalArgumentException If the {@link Class}&lt;T&gt; or {@link Consumer}&lt;T&gt; argument is null
     */
    public <T extends ItemMeta> ItemBuilder withMeta(Class<T> metaClass, Consumer<T> metaConsumer) {
        this.itemStack.editMeta(metaClass, metaConsumer);
        return this;
    }

    /**
     * Removes all enchantments that are listed in a given list from the {@link ItemStack}.
     *
     * @param enchantments Enchantment(s) that will be removed from the {@link ItemStack}
     * @return This instance, useful for chaining
     * @throws IllegalArgumentException If the {@link Collection}&lt;{@link Enchantment}&gt; argument is null
     */
    public ItemBuilder removeEnchantments(Collection<Enchantment> enchantments) {
        enchantments.forEach(this.itemStack::removeEnchantment);
        return this;
    }

    /**
     * Removes all enchantments that are listed in a given array from the {@link ItemStack}.
     *
     * @param enchantments Enchantment(s) that will be removed from the {@link ItemStack}
     * @return This instance, useful for chaining
     * @throws IllegalArgumentException If the {@link Enchantment} array argument is null
     */
    public ItemBuilder removeEnchantments(Enchantment... enchantments) {
        Arrays.stream(enchantments).forEach(this.itemStack::removeEnchantment);
        return this;
    }

    /**
     * Adds an enchantment glint (glow) to the {@link ItemStack} without applying any particular enchantment.
     *
     * @return This instance, useful for chaining
     */
    public ItemBuilder glint() {
        return this.withMeta(meta -> meta.setEnchantmentGlintOverride(true));
    }

    /**
     * Sets/repeats a new given line of lore at given <strong>existing</strong> indices of the {@link ItemStack}'s
     * lore.
     * <p>
     * If there is no existing lore, this method will create a new list with the initial size of the biggest given index
     * plus 1 and proceed to set the line at given indices.
     *
     * @param indices Indexes at which the specified lore line will be set
     * @param line    New line of lore as Component
     * @return This instance, useful for chaining
     * @throws NullPointerException If the integer array of indices is null
     */
    public ItemBuilder loreAt(@Nullable Component line, final int... indices) {
        final var lore = this.getLore();

        for (int index : indices) {
            while (index >= lore.size()) {
                lore.add(null);
            }

            lore.set(index, line);
        }

        return this.lore(lore);
    }

    /**
     * Modifies the {@link ItemStack} with given operations.
     *
     * @param itemConsumer Consumer or anonymous function that'll take this instance's item as an argument
     * @return This instance, useful for chaining
     * @throws IllegalArgumentException If the {@link Consumer}&lt;{@link ItemStack}&gt; argument is null
     */
    public ItemBuilder with(Consumer<ItemStack> itemConsumer) {
        itemConsumer.accept(this.build());
        return this;
    }

    public ItemBuilder withNew(Function<ItemStack, ItemStack> itemConsumer) {
        this.itemStack = itemConsumer.apply(this.build());
        return this;
    }

    /**
     * Modifies the {@link ItemStack}'s {@link ItemMeta} with given operations.
     *
     * @param metaConsumer Consumer or anonymous function that'll take this instance's item's {@link ItemMeta} as an
     *                     argument
     * @return This instance, useful for chaining
     * @throws IllegalArgumentException If the {@link Consumer}&lt;{@link ItemMeta}&gt; argument is null
     */
    public ItemBuilder withMeta(Consumer<ItemMeta> metaConsumer) {
        itemStack.editMeta(metaConsumer);
        return this;
    }

    /**
     * Appends new lines of lore at the given index of the {@link ItemStack}'s lore from a given list.
     *
     * @param index Index at which the specified new lines of lore will be added
     * @param lines List of lore lines that'll be added
     * @return This instance, useful for chaining
     * @throws IllegalArgumentException If the {@link Collection}&lt;{@link Component}&gt; argument is null
     */
    public ItemBuilder addLoreAt(int index, Collection<Component> lines) {
        final var lore = this.getLore();

        lore.addAll(index, lines);

        return this.lore(lore);
    }

    /**
     * Appends new lines of lore at the given index of the {@link ItemStack}'s lore from a given array.
     *
     * @param index Index at which the specified new lines of lore will be added
     * @param lines Array of lore lines that'll be added
     * @return This instance, useful for chaining
     * @throws IllegalArgumentException If the {@link Component} array of lore lines is null
     */
    public ItemBuilder addLoreAt(int index, Component... lines) {
        return this.addLoreAt(index, Arrays.asList(lines));
    }

    /**
     * Removes given ItemFlags from the {@link ItemStack}.
     *
     * @param flags ItemFlags that'll be removed from the {@link ItemStack}
     * @return This instance, useful for chaining
     * @throws IllegalArgumentException If the {@link ItemFlag} array is null
     */
    public ItemBuilder removeFlags(ItemFlag... flags) {
        return this.withMeta(meta -> meta.removeItemFlags(flags));
    }

    /**
     * Removes a line of lore at each given list index of the {@link ItemStack}'s lore.
     * <p>
     * This method loops through the given list of indexes and removes a line at each given index. Removing an element
     * at an index will cause the {@link ArrayList} to shift in size and move its elements towards the removed element
     * each time. Repeat the same index to remove a line and lines after it.
     *
     * @param indices Array of indexes at which each lore line should be removed
     * @return This instance, useful for chaining
     * @throws IllegalArgumentException If the integer array of indexes is null
     */
    public ItemBuilder removeLoreAt(int... indices) {
        final var lore = this.getLore();

        for (int index : indices) {
            lore.remove(index);
        }

        return this.lore(lore);
    }

    /**
     * Sets a new type of material of item for the {@link ItemStack}.
     *
     * @param material New type of material that the {@link ItemStack} will be
     * @return This instance, useful for chaining
     * @throws IllegalArgumentException If the {@link Material} argument is null
     */
    public ItemBuilder material(Material material) {
        return this.withNew(item -> item.withType(material));
    }

    /**
     * Alias of {@link #material(Material)}.
     *
     * @param material New type of material that the {@link ItemStack} will be
     * @return This instance, useful for chaining
     * @throws IllegalArgumentException If the {@link Material} argument is null
     * @see #material(Material)
     */
    public ItemBuilder type(Material material) {
        return this.material(material);
    }

    /**
     * Sets new lines of lore of the {@link ItemStack}'s lore from a given list.
     *
     * @param lines List of lore lines, lore will be removed if it's null
     * @return This instance, useful for chaining
     */
    public ItemBuilder lore(@Nullable List<? extends @Nullable Component> lines) {
        return this.withMeta(meta -> meta.lore(lines));
    }

    /**
     * Sets new lines of lore of the {@link ItemStack}'s lore from a given array.
     *
     * @param lines Array of lore lines, lore will be removed if it's null
     * @return This instance, useful for chaining
     */
    public ItemBuilder lore(@Nullable Component @Nullable ... lines) {
        return this.lore(lines == null ? null : Arrays.asList(lines));
    }

    /**
     * Appends new lines of lore at the end of the {@link ItemStack}'s lore from a given list.
     *
     * @param lines List of lore lines that'll be added
     * @return This instance, useful for chaining
     * @throws IllegalArgumentException If the {@link Collection}&lt;{@link Component}&gt; argument is null
     */
    public ItemBuilder addLore(Collection<Component> lines) {
        final var lore = this.getLore();

        lore.addAll(lines);

        return this.lore(lore);
    }

    /**
     * Appends new lines of lore at the end of the {@link ItemStack}'s lore from a given array.
     *
     * @param lines Array of lore lines that'll be added
     * @return This instance, useful for chaining
     * @throws IllegalArgumentException If the {@link Component} array of lore lines is null
     */
    public ItemBuilder addLore(Component... lines) {
        return this.addLore(Arrays.asList(lines));
    }

    /**
     * Sets a new {@link ItemMeta} for the {@link ItemStack}.
     *
     * @param meta New {@link ItemMeta}, will be removed if it's null
     * @return This instance, useful for chaining
     */
    public ItemBuilder itemMeta(@Nullable ItemMeta meta) {
        return this.with(item -> item.setItemMeta(meta));
    }

    /**
     * Sets a new custom name for the {@link ItemStack} using an Adventure {@link Component}.
     *
     * @param title New custom name of this {@link ItemStack}
     * @return This instance, useful for chaining
     */
    @SuppressWarnings("UnusedReturnValue")
    public ItemBuilder customName(@Nullable Component title) {
        return this.withMeta(meta -> meta.customName(title));
    }

    /**
     * Sets whether the {@link ItemStack} can lose its durability through use.
     *
     * @param breakable Whether the {@link ItemStack} can lose its durability through use
     * @return This instance, useful for chaining
     */
    public ItemBuilder breakable(boolean breakable) {
        return this.withMeta(meta -> meta.setUnbreakable(!breakable));
    }

    /**
     * Sets a given number of items of the {@link ItemStack}.
     *
     * @param amount Number of items in an {@link ItemStack}
     * @return This instance, useful for chaining
     */
    public ItemBuilder amount(int amount) {
        return this.with(item -> item.setAmount(amount));
    }

    /**
     * Removes all lines of lore from the {@link ItemStack}'s lore.
     *
     * @return This instance, useful for chaining
     */
    public ItemBuilder clearLore() {
        return this.lore((List<Component>) null);
    }

    /**
     * Returns the {@link ItemMeta} of the {@link ItemStack}.
     *
     * @return {@link ItemMeta} of the {@link ItemStack}
     */
    public Optional<ItemMeta> getItemMeta() {
        return Optional.of(this.itemStack.getItemMeta());
    }

    /**
     * Returns a {@link List} of lore lines of the {@link ItemStack}.
     *
     * @return Not null list of lore lines of the {@link ItemStack}, possibly empty
     */
    public List<@Nullable Component> getLore() {
        return Optional.ofNullable(itemStack.lore()).orElseGet(ArrayList::new);
    }

    /**
     * Returns whether the {@link ItemStack} can lose its durability through use.
     *
     * @return Whether the {@link ItemStack} can lose its durability through use
     */
    public boolean isBreakable() {
        return this.getItemMeta().map(meta -> !meta.isUnbreakable()).orElse(true);
    }

    /**
     * Simply returns the {@link ItemStack} that's being modified by this {@link ItemBuilder}.
     *
     * @return {@link ItemStack} that's being modified
     */
    public ItemStack build() {
        return this.itemStack;
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public ItemBuilder clone() {
        return new ItemBuilder(this.build().clone());
    }
}