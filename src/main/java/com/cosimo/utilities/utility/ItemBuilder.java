package com.cosimo.utilities.utility;

import com.google.common.base.Preconditions;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

/**
 * A class that wraps around a given or created {@link ItemStack}, allowing easier and chained modifications of the
 * item.
 */
public class ItemBuilder implements Cloneable {

    /**
     * Mutable {@link ItemStack}.
     */
    private final ItemStack itemStack;

    /**
     * Creates a new {@link ItemBuilder} from an item stored in a given configuration file's path or a certain item in
     * case of non-existence of path or object at path.
     *
     * @param configuration YAML configuration file in which the item is stored
     * @param path          Path at which the item is stored at
     * @param failure       {@link ItemStack} object that will be used instead if the configuration value is
     *                      non-existent
     */
    public ItemBuilder(@Nonnull FileConfiguration configuration, @Nonnull String path, @Nonnull ItemStack failure) {
        this(Objects.requireNonNull(configuration.getItemStack(path, failure), "FileConfiguration or the resulting ItemStack can't be null"));
    }

    /**
     * Creates a new {@link ItemBuilder} from an item stored in a given configuration file's path.
     * <p>
     * If an item at the given configuration path isn't found, then the item will be air.
     *
     * @param configuration YAML configuration file in which the item is stored
     * @param path          Path at which the item is stored at
     */
    public ItemBuilder(@Nonnull FileConfiguration configuration, @Nonnull String path) {
        this(configuration, path, new ItemStack(Material.STONE));
    }

    /**
     * Creates a new {@link ItemBuilder} from the given material, amount and title of a new item being created.
     *
     * @param material Type of an item
     * @param amount   Amount of items in a stack
     * @param title    Display name or visible title of an item
     */
    public ItemBuilder(@Nonnull Material material, int amount, @Nullable String title) {
        this(new ItemStack(material, amount), title);
    }

    /**
     * Creates a new {@link ItemBuilder} from the given item, renaming it to a given title.
     *
     * @param itemStack {@link ItemStack} that's being wrapped by this class for further modification
     * @param title     Display name or visible title of an item
     * @throws IllegalArgumentException If the ItemStack argument is null
     */
    public ItemBuilder(@Nonnull ItemStack itemStack, @Nullable String title) {
        this(itemStack);
        this.title(title);
    }

    /**
     * Creates a new {@link ItemBuilder} from the given material and title of a new item being created, with an amount
     * of 1.
     *
     * @param material Type of an item
     * @param title    Display name or visible title of an item
     */
    public ItemBuilder(@Nonnull Material material, @Nullable String title) {
        this(material, 1, title);
    }

    /**
     * Creates a new {@link ItemBuilder} from the given material and amount of a new item being created, with a default
     * material title.
     *
     * @param material Type of an item
     * @param amount   Amount of items in a stack
     */
    public ItemBuilder(@Nonnull Material material, int amount) {
        this(material, amount, null);
    }

    /**
     * Creates a new {@link ItemBuilder} from the given material, with an amount of 1 and default material title.
     *
     * @param material Type of an item
     */
    public ItemBuilder(@Nonnull Material material) {
        this(material, 1, null);
    }

    /**
     * Creates a new {@link ItemBuilder} from a given {@link ItemStack}.
     *
     * @param itemStack {@link ItemStack} that's being wrapped by this class for further modification
     * @throws IllegalArgumentException If the ItemStack argument is null or of Material.AIR type
     */
    public ItemBuilder(@Nonnull ItemStack itemStack) {
        Preconditions.checkArgument(itemStack != null, "ItemStack argument can't be null");

        this.itemStack = itemStack;
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
    @Nonnull
    public <T extends ItemMeta> ItemBuilder changeMeta(@Nonnull Consumer<T> metaConsumer, @Nonnull Class<T> metaClass) {
        Preconditions.checkArgument(metaClass != null, "Class<T extends ItemMeta> argument can't be null");
        Preconditions.checkArgument(metaConsumer != null, "Consumer<T extends ItemMeta> argument can't be null");

        this.getItemMeta().ifPresent(meta -> {
            if (metaClass.isInstance(meta)) {
                metaConsumer.accept(metaClass.cast(meta));
                this.itemStack.setItemMeta(meta);
            }
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
    @Nonnull
    public ItemBuilder removeEnchantments(@Nonnull Collection<Enchantment> enchantments) {
        Preconditions.checkArgument(enchantments != null, "Enchantments list argument can't be null");
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
    @Nonnull
    public ItemBuilder removeEnchantments(@Nonnull Enchantment... enchantments) {
        Preconditions.checkArgument(enchantments != null, "Enchantments array argument can't be null");

        for (Enchantment enchantment : enchantments) {
            this.itemStack.removeEnchantment(enchantment);
        }

        return this;
    }

    /**
     * Adds an enchantment glint (glow) to the {@link ItemStack} without applying any particular enchantment.
     *
     * @return This instance, useful for chaining
     */
    @Nonnull
    public ItemBuilder glint() {
        return this.changeMeta(meta -> meta.setEnchantmentGlintOverride(true));
    }

    /**
     * Appends new lines of lore at the given index of the {@link ItemStack}'s lore from a given list.
     *
     * @param index Index at which the specified new lines of lore will be added
     * @param lines List of lore lines that'll be added
     * @return This instance, useful for chaining
     * @throws IllegalArgumentException If the {@link Collection}&lt;{@link String}&gt; argument is null
     */
    @Nonnull
    public ItemBuilder addLoreAt(int index, @Nonnull Collection<String> lines) {
        Preconditions.checkArgument(lines != null, "Collection<String> of lore lines argument can't be null");

        List<String> lore = this.getLore();
        lore.addAll(index, lines);

        return this.lore(lore);
    }

    /**
     * Sets/repeats a new given line of lore at given <strong>existing</strong> indexes of the {@link ItemStack}'s
     * lore.
     * <p>
     * If there is no existing lore, this method will create a new list with the initial size of the biggest given index
     * plus 1 and proceed to set the line at given indexes.
     *
     * @param indexes Indexes at which the specified lore line will be set
     * @param line    New line of lore
     * @return This instance, useful for chaining
     * @throws IllegalArgumentException If the integer array of indexes is null
     */
    @Nonnull
    public ItemBuilder loreAt(@Nullable String line, int... indexes) {
        if (indexes == null || indexes.length == 0) {
            indexes = new int[]{0};
        }

        List<String> lore = this.getItemMeta().map(ItemMeta::getLore).orElse(null);
        lore = lore == null || lore.isEmpty() ? new ArrayList<>(NumberUtil.max(indexes) + 1) : lore;

        for (int index : indexes) {
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
    @Nonnull
    public ItemBuilder changeItem(@Nonnull Consumer<ItemStack> itemConsumer) {
        Preconditions.checkArgument(itemConsumer != null, "Consumer<ItemStack> argument can't be null");
        itemConsumer.accept(this.itemStack);
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
    @Nonnull
    public ItemBuilder changeMeta(@Nonnull Consumer<ItemMeta> metaConsumer) {
        Preconditions.checkArgument(metaConsumer != null, "Consumer<T extends ItemMeta> argument can't be null");

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
    @Nonnull
    public ItemBuilder addLoreAt(int index, @Nonnull String... lines) {
        Preconditions.checkArgument(lines != null, "String array of lore lines argument can't be null");
        return this.addLoreAt(index, Arrays.asList(lines));
    }

    /**
     * Appends new lines of lore at the end of the {@link ItemStack}'s lore from a given list.
     *
     * @param lines List of lore lines that'll be added
     * @return This instance, useful for chaining
     * @throws IllegalArgumentException If the {@link Collection}&lt;{@link String}&gt; argument is null
     */
    @Nonnull
    public ItemBuilder addLore(@Nonnull Collection<String> lines) {
        Preconditions.checkArgument(lines != null, "Collection<String> of lore lines argument can't be null");

        List<String> lore = this.getLore();
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
    @Nonnull
    public ItemBuilder removeFlags(@Nonnull ItemFlag... flags) {
        Preconditions.checkArgument(flags != null, "ItemFlag array of enums argument can't be null");
        return this.changeMeta(meta -> meta.removeItemFlags(flags));
    }

    /**
     * Removes a line of lore at each given list index of the {@link ItemStack}'s lore.
     * <p>
     * This method loops through the given list of indexes and removes a line at each given index. Removing an element
     * at an index will cause the {@link ArrayList} to shift in size and move it's elements towards the removed element
     * each time. Repeat the same index to remove a line and lines after it.
     *
     * @param indexes Array of indexes at which each lore line should be removed
     * @return This instance, useful for chaining
     * @throws IllegalArgumentException If the integer array of indexes is null
     */
    @Nonnull
    public ItemBuilder removeLoreAt(int... indexes) {
        if (indexes == null || indexes.length == 0) {
            indexes = new int[]{0};
        }

        List<String> lore = this.getLore();

        for (int index : indexes) {
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
    @Nonnull
    public ItemBuilder material(@Nonnull Material material) {
        Preconditions.checkArgument(material != null, "Material argument can't be null");
        return this.changeItem(item -> item.setType(material));
    }

    /**
     * Adds new ItemFlags to the {@link ItemStack}.
     *
     * @param flags Array of ItemFlag enums
     * @return This instance, useful for chaining
     * @throws IllegalArgumentException If the {@link ItemFlag} array is null
     */
    @Nonnull
    public ItemBuilder addFlags(@Nonnull ItemFlag... flags) {
        Preconditions.checkArgument(flags != null, "ItemFlag array of enums argument can't be null");
        return this.changeMeta(meta -> meta.addItemFlags(flags));
    }

    /**
     * Sets new lines of lore of the {@link ItemStack}'s lore from a given list.
     *
     * @param lines List of lore lines, lore will be removed if it's NULL
     * @return This instance, useful for chaining
     */
    @Nonnull
    public ItemBuilder lore(@Nullable List<String> lines) {
        return this.changeMeta(meta -> meta.setLore(lines));
    }

    /**
     * Appends new lines of lore at the end of the {@link ItemStack}'s lore from a given array.
     *
     * @param lines Array of lore lines that'll be added
     * @return This instance, useful for chaining
     * @throws IllegalArgumentException If the {@link String} array of lore lines is null
     */
    @Nonnull
    public ItemBuilder addLore(@Nonnull String... lines) {
        Preconditions.checkArgument(lines != null, "String array of lore lines argument can't be null");
        return this.addLore(Arrays.asList(lines));
    }

    /**
     * Sets a new {@link ItemMeta} for the {@link ItemStack}.
     *
     * @param meta New {@link ItemMeta}, will be removed if it's NULL
     * @return This instance, useful for chaining
     */
    @Nonnull
    public ItemBuilder itemMeta(@Nullable ItemMeta meta) {
        return this.changeItem(item -> item.setItemMeta(meta));
    }

    /**
     * Sets new lines of lore of the {@link ItemStack}'s lore from a given array.
     *
     * @param lines Array of lore lines, lore will be removed if it's NULL
     * @return This instance, useful for chaining
     */
    @Nonnull
    public ItemBuilder lore(@Nullable String... lines) {
        return this.lore(lines == null ? null : Arrays.asList(lines));
    }

    /**
     * Sets a new given display name/title for the {@link ItemStack}.
     *
     * @param title New display name/title of an {@link ItemStack}
     * @return This instance, useful for chaining
     */
    @Nonnull
    public ItemBuilder title(@Nullable String title) {
        return this.changeMeta(meta -> meta.setDisplayName(title));
    }

    /**
     * Sets whether the {@link ItemStack} can lose it's durability through use.
     *
     * @param breakable Whether the {@link ItemStack} can lose it's durability through use
     * @return This instance, useful for chaining
     */
    @Nonnull
    public ItemBuilder breakable(boolean breakable) {
        return this.changeMeta(meta -> meta.setUnbreakable(!breakable));
    }

    /**
     * Sets a given amount of items of the {@link ItemStack}.
     *
     * @param amount Amount of items in an {@link ItemStack}
     * @return This instance, useful for chaining
     */
    @Nonnull
    public ItemBuilder amount(int amount) {
        return this.changeItem(item -> item.setAmount(amount));
    }

    /**
     * Removes all lines of lore from the {@link ItemStack}'s lore.
     *
     * @return This instance, useful for chaining
     */
    @Nonnull
    public ItemBuilder clearLore() {
        return this.lore((ArrayList<String>) null);
    }

    /**
     * Returns the {@link ItemMeta} of the {@link ItemStack}.
     *
     * @return {@link ItemMeta} of the {@link ItemStack}
     */
    @Nonnull
    public Optional<ItemMeta> getItemMeta() {
        return Optional.ofNullable(this.itemStack.getItemMeta());
    }

    /**
     * Returns a {@link List} of lore lines of the {@link ItemStack}.
     *
     * @return Not null list of lore lines of the {@link ItemStack}, possibly empty
     */
    @Nonnull
    public List<String> getLore() {
        return this.getItemMeta().map(ItemMeta::getLore).orElse(new ArrayList<>());
    }

    /**
     * Returns whether the {@link ItemStack} can lose it's durability through use.
     *
     * @return Whether the {@link ItemStack} can lose it's durability through use
     */
    public boolean isBreakable() {
        return this.getItemMeta().map(meta -> !meta.isUnbreakable()).orElse(true);
    }

    /**
     * Simply returns the {@link ItemStack} that's being modified by this {@link ItemBuilder}.
     *
     * @return {@link ItemStack} that's being modified
     */
    @Nonnull
    public ItemStack get() {
        return this.itemStack;
    }

    @Override
    public ItemBuilder clone() {
        return new ItemBuilder(this.itemStack.clone());
    }
}