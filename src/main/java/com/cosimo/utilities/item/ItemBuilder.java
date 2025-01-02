package com.cosimo.utilities.item;

import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A proxy class (rather than a true builder) that wraps around an {@link ItemStack}, allowing easier, more fluent and
 * chained modifications of the item.
 */
@SuppressWarnings("unused")
public class ItemBuilder implements Cloneable {

    /**
     * Mutable {@link ItemStack}.
     */
    private final ItemStack itemStack;
    private ItemMeta itemMeta;

    /**
     * Creates a new {@link ItemBuilder} from an item stored in a given configuration file's path or a default provided
     * one in case of non-existence of path or object at path.
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
     * one in case of non-existence of path or object at path.
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
     * Creates a new {@link ItemBuilder} from the given material, amount and title of a new item being created.
     *
     * @param material Type of item
     * @param amount   Amount of items in a stack
     * @param title    Display name or visible title of an item
     */
    public ItemBuilder(@NonNull Material material, int amount, @Nullable String title) {
        this(new ItemStack(material, amount), title);
    }

    /**
     * Creates a new {@link ItemBuilder} from the given item, renaming it to a given title.
     *
     * @param itemStack {@link ItemStack} that's being wrapped by this class for further modification
     * @param title     Display name or visible title of an item
     * @throws IllegalArgumentException If the ItemStack argument is null
     */
    public ItemBuilder(@NonNull ItemStack itemStack, @Nullable String title) {
        this(itemStack);
        this.title(title);
    }

    /**
     * Creates a new {@link ItemBuilder} from the given material and title of a new item being created, with an amount
     * of 1.
     *
     * @param material Type of item
     * @param title    Display name or visible title of an item
     */
    public ItemBuilder(@NonNull Material material, @Nullable String title) {
        this(material, 1, title);
    }

    /**
     * Creates a new {@link ItemBuilder} from the given material and amount of a new item being created, with a default
     * material title.
     *
     * @param material Type of item
     * @param amount   Amount of items in a stack
     */
    public ItemBuilder(@NonNull Material material, int amount) {
        this(material, amount, null);
    }

    /**
     * Creates a new {@link ItemBuilder} from the given material, with an amount of 1 and default material title.
     *
     * @param material Type of item
     */
    public ItemBuilder(@NonNull Material material) {
        this(material, 1, null);
    }

    /**
     * Creates a new {@link ItemBuilder} from a given {@link ItemStack}.
     *
     * @param itemStack {@link ItemStack} that's being wrapped by this class for further modification
     * @throws IllegalArgumentException If the ItemStack argument is null or of Material.AIR type
     */
    public ItemBuilder(@NonNull ItemStack itemStack) {
        this.itemStack = itemStack;
        this.itemMeta = itemStack.getItemMeta();
    }

    /**
     * Modifies the {@link ItemStack}'s subtype of {@link ItemMeta} with given operations, possibly specific for that
     * subclass.
     *
     * @param metaConsumer Consumer or anonymous function that'll take this instance's item's specific {@link ItemMeta}
     *                     as an argument
     * @param metaClass    Class that belongs to the {@link ItemMeta} subtype
     * @param <T>          {@link ItemMeta} subclass type
     * @return This instance, useful for chaining
     * @throws IllegalArgumentException If the {@link Class}&lt;T&gt; or {@link Consumer}&lt;T&gt; argument is null
     */
    @NonNull
    public <T extends ItemMeta> ItemBuilder withMeta(@NonNull Consumer<T> metaConsumer, @NonNull Class<T> metaClass) {
        this.getItemMeta().filter(metaClass::isInstance).ifPresent(meta -> {
            metaConsumer.accept(metaClass.cast(meta));
            this.itemStack.setItemMeta(meta);
        });

        return this;
    }

    /**
     * Removes all enchantments that are listed in a given list from the {@link ItemStack}.
     *
     * @param enchantments Enchantment(s) that will be removed from the {@link ItemStack}
     * @return This instance, useful for chaining
     * @throws IllegalArgumentException If the {@link Collection}&lt;{@link Enchantment}&gt; argument is null
     */
    @NonNull
    public ItemBuilder removeEnchantments(@NonNull Collection<Enchantment> enchantments) {
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
    @NonNull
    public ItemBuilder removeEnchantments(@NonNull Enchantment @NonNull ... enchantments) {
        Arrays.stream(enchantments).forEach(this.itemStack::removeEnchantment);
        return this;
    }

    /**
     * Adds an enchantment glint (glow) to the {@link ItemStack} without applying any particular enchantment.
     *
     * @return This instance, useful for chaining
     */
    @NonNull
    public ItemBuilder glint() {
        return this.withMeta(meta -> meta.setEnchantmentGlintOverride(true));
    }

    /**
     * Appends new lines of lore at the given index of the {@link ItemStack}'s lore from a given list.
     *
     * @param index Index at which the specified new lines of lore will be added
     * @param lines List of lore lines that'll be added
     * @return This instance, useful for chaining
     * @throws IllegalArgumentException If the {@link Collection}&lt;{@link String}&gt; argument is null
     */
    @NonNull
    public ItemBuilder addLoreAt(int index, @NonNull Collection<String> lines) {
        final List<String> lore = this.getLore();
        lore.addAll(index, lines);
        return this.lore(lore);
    }

    /**
     * Sets/repeats a new given line of lore at given <strong>existing</strong> indices of the {@link ItemStack}'s
     * lore.
     * <p>
     * If there is no existing lore, this method will create a new list with the initial size of the biggest given index
     * plus 1 and proceed to set the line at given indices.
     *
     * @param indices Indexes at which the specified lore line will be set
     * @param line    New line of lore
     * @return This instance, useful for chaining
     * @throws IllegalArgumentException If the integer array of indices is null
     */
    @NonNull
    public ItemBuilder loreAt(@Nullable String line, final int @NonNull ... indices) {
        final int size = Arrays.stream(indices)
                                 .max()
                                 .orElseThrow(() -> new IllegalArgumentException("Array of indices is null or empty")) +
                         1;
        final List<String> lore = this.getLore();

        if (lore.size() < size) {
            lore.addAll(Collections.nCopies(lore.size() - size, null));
        }

        for (int index : indices) {
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
    @NonNull
    public ItemBuilder with(@NonNull Consumer<ItemStack> itemConsumer) {
        this.itemStack.setItemMeta(this.itemMeta);
        itemConsumer.accept(this.itemStack);
        this.itemMeta = this.itemStack.getItemMeta();
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
    @NonNull
    public ItemBuilder withMeta(@NonNull Consumer<ItemMeta> metaConsumer) {
        this.getItemMeta().ifPresent(meta -> {
            metaConsumer.accept(meta);
            this.itemStack.setItemMeta(meta);
        });

        return this;
    }

    /**
     * Appends new lines of lore at the given index of the {@link ItemStack}'s lore from a given array.
     *
     * @param index Index at which the specified new lines of lore will be added
     * @param lines Array of lore lines that'll be added
     * @return This instance, useful for chaining
     * @throws IllegalArgumentException If the {@link String} array of lore lines is null
     */
    @NonNull
    public ItemBuilder addLoreAt(int index, String @NonNull ... lines) {
        return this.addLoreAt(index, Arrays.asList(lines));
    }

    /**
     * Appends new lines of lore at the end of the {@link ItemStack}'s lore from a given list.
     *
     * @param lines List of lore lines that'll be added
     * @return This instance, useful for chaining
     * @throws IllegalArgumentException If the {@link Collection}&lt;{@link String}&gt; argument is null
     */
    @NonNull
    public ItemBuilder addLore(@NonNull Collection<String> lines) {
        final List<String> lore = this.getLore();
        lore.addAll(lines);

        return this.lore(lore);
    }

    /**
     * Removes given ItemFlags from the {@link ItemStack}.
     *
     * @param flags ItemFlags that'll be removed from the {@link ItemStack}
     * @return This instance, useful for chaining
     * @throws IllegalArgumentException If the {@link ItemFlag} array is null
     */
    @NonNull
    public ItemBuilder removeFlags(@NonNull ItemFlag @NonNull ... flags) {
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
    @NonNull
    public ItemBuilder removeLoreAt(int @NonNull ... indices) {
        final List<String> lore = this.getLore();

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
    @NonNull
    public ItemBuilder material(@NonNull Material material) {
        return this.with(item -> item.setType(material));
    }

    /**
     * Alias of {@link #material(Material)}.
     *
     * @param material New type of material that the {@link ItemStack} will be
     * @return This instance, useful for chaining
     * @throws IllegalArgumentException If the {@link Material} argument is null
     * @see #material(Material)
     */
    @NonNull
    public ItemBuilder type(@NonNull Material material) {
        return this.material(material);
    }

    /**
     * Adds new ItemFlags to the {@link ItemStack}.
     *
     * @param flags Array of ItemFlag enums
     * @return This instance, useful for chaining
     * @throws IllegalArgumentException If the {@link ItemFlag} array is null
     */
    @NonNull
    public ItemBuilder addFlags(@NonNull ItemFlag @NonNull ... flags) {
        return this.withMeta(meta -> meta.addItemFlags(flags));
    }

    /**
     * Sets new lines of lore of the {@link ItemStack}'s lore from a given list.
     *
     * @param lines List of lore lines, lore will be removed if it's null
     * @return This instance, useful for chaining
     */
    @NonNull
    public ItemBuilder lore(@Nullable List<String> lines) {
        return this.withMeta(meta -> meta.setLore(lines));
    }

    /**
     * Sets new lines of lore of the {@link ItemStack}'s lore from a given array.
     *
     * @param lines Array of lore lines, lore will be removed if it's null
     * @return This instance, useful for chaining
     */
    @NonNull
    public ItemBuilder lore(@Nullable String @Nullable ... lines) {
        return this.lore(lines == null ? null : Arrays.asList(lines));
    }

    /**
     * Appends new lines of lore at the end of the {@link ItemStack}'s lore from a given array.
     *
     * @param lines Array of lore lines that'll be added
     * @return This instance, useful for chaining
     * @throws IllegalArgumentException If the {@link String} array of lore lines is null
     */
    @NonNull
    public ItemBuilder addLore(String @NonNull ... lines) {
        return this.addLore(Arrays.asList(lines));
    }

    /**
     * Sets a new {@link ItemMeta} for the {@link ItemStack}.
     *
     * @param meta New {@link ItemMeta}, will be removed if it's null
     * @return This instance, useful for chaining
     */
    @NonNull
    public ItemBuilder itemMeta(@Nullable ItemMeta meta) {
        return this.with(item -> item.setItemMeta(meta));
    }

    /**
     * Sets a new given display name/title for the {@link ItemStack}.
     *
     * @param title New display name/title of this {@link ItemStack}
     * @return This instance, useful for chaining
     */
    @NonNull
    public ItemBuilder title(@Nullable String title) {
        return this.withMeta(meta -> meta.setDisplayName(title));
    }

    /**
     * Alias of {@link #title(String)}.
     *
     * @param name New display name/title of this {@link ItemStack}
     * @return This instance, useful for chaining
     * @see #title(String)
     */
    @NonNull
    public ItemBuilder name(@Nullable String name) {
        return this.title(name);
    }

    /**
     * Sets whether the {@link ItemStack} can lose its durability through use.
     *
     * @param breakable Whether the {@link ItemStack} can lose its durability through use
     * @return This instance, useful for chaining
     */
    @NonNull
    public ItemBuilder breakable(boolean breakable) {
        return this.withMeta(meta -> meta.setUnbreakable(!breakable));
    }

    /**
     * Sets a given amount of items of the {@link ItemStack}.
     *
     * @param amount Amount of items in an {@link ItemStack}
     * @return This instance, useful for chaining
     */
    @NonNull
    public ItemBuilder amount(int amount) {
        return this.with(item -> item.setAmount(amount));
    }

    /**
     * Removes all lines of lore from the {@link ItemStack}'s lore.
     *
     * @return This instance, useful for chaining
     */
    @NonNull
    public ItemBuilder clearLore() {
        return this.lore((ArrayList<String>) null);
    }

    /**
     * Returns the {@link ItemMeta} of the {@link ItemStack}.
     *
     * @return {@link ItemMeta} of the {@link ItemStack}
     */
    @NonNull
    public Optional<ItemMeta> getItemMeta() {
        return Optional.ofNullable(this.itemMeta);
    }

    /**
     * Returns a {@link List} of lore lines of the {@link ItemStack}.
     *
     * @return Not null list of lore lines of the {@link ItemStack}, possibly empty
     */
    @NonNull
    public List<String> getLore() {
        return this.getItemMeta().map(ItemMeta::getLore).orElseGet(ArrayList::new);
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
    @NonNull
    public ItemStack build() {
        this.itemStack.setItemMeta(this.itemMeta);
        return this.itemStack;
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public ItemBuilder clone() {
        return new ItemBuilder(this.itemStack.clone());
    }
}