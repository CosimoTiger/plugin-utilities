package memecat.fatcat.utilities.utility;

import com.google.common.base.Preconditions;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
    public ItemBuilder(@NotNull FileConfiguration configuration, @NotNull String path, @NotNull ItemStack failure) {
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
    public ItemBuilder(@NotNull FileConfiguration configuration, @NotNull String path) {
        this(configuration, path, new ItemStack(Material.STONE));
    }

    /**
     * Creates a new {@link ItemBuilder} from the given material, amount and title of a new item being created.
     *
     * @param material Type of an item
     * @param amount   Amount of items in a stack
     * @param title    Display name or visible title of an item
     */
    public ItemBuilder(@NotNull Material material, int amount, @Nullable String title) {
        this(new ItemStack(material, amount), title);
    }

    /**
     * Creates a new {@link ItemBuilder} from the given item, renaming it to a given title.
     *
     * @param itemStack {@link ItemStack} that's being wrapped by this class for further modification
     * @param title     Display name or visible title of an item
     * @throws IllegalArgumentException If the ItemStack argument is null
     */
    public ItemBuilder(@NotNull ItemStack itemStack, @Nullable String title) {
        this(itemStack);
        title(title);
    }

    /**
     * Creates a new {@link ItemBuilder} from the given material and title of a new item being created, with an amount
     * of 1.
     *
     * @param material Type of an item
     * @param title    Display name or visible title of an item
     */
    public ItemBuilder(@NotNull Material material, @Nullable String title) {
        this(material, 1, title);
    }

    /**
     * Creates a new {@link ItemBuilder} from the given material and amount of a new item being created, with a default
     * material title.
     *
     * @param material Type of an item
     * @param amount   Amount of items in a stack
     */
    public ItemBuilder(@NotNull Material material, int amount) {
        this(material, amount, null);
    }

    /**
     * Creates a new {@link ItemBuilder} from the given material, with an amount of 1 and default material title.
     *
     * @param material Type of an item
     */
    public ItemBuilder(@NotNull Material material) {
        this(material, 1, null);
    }

    /**
     * Creates a new {@link ItemBuilder} from a given {@link ItemStack}.
     *
     * @param itemStack {@link ItemStack} that's being wrapped by this class for further modification
     * @throws IllegalArgumentException If the ItemStack argument is null or of Material.AIR type
     */
    public ItemBuilder(@NotNull ItemStack itemStack) {
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
    @NotNull
    public <T extends ItemMeta> ItemBuilder changeMeta(@NotNull Consumer<T> metaConsumer, @NotNull Class<T> metaClass) {
        Preconditions.checkArgument(metaClass != null, "Class<T extends ItemMeta> argument can't be null");
        Preconditions.checkArgument(metaConsumer != null, "Consumer<T extends ItemMeta> argument can't be null");

        getItemMeta().ifPresent(meta -> {
            if (metaClass.isInstance(meta)) {
                metaConsumer.accept(metaClass.cast(meta));
                itemStack.setItemMeta(meta);
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
    @NotNull
    public ItemBuilder removeEnchantments(@NotNull Collection<Enchantment> enchantments) {
        Preconditions.checkArgument(enchantments != null, "Enchantments list argument can't be null");
        enchantments.forEach(itemStack::removeEnchantment);
        return this;
    }

    /**
     * Removes all enchantments that are listed in a given array from the {@link ItemStack}.
     *
     * @param enchantments Enchantment(s) that will be removed from the {@link ItemStack}
     * @return This instance, useful for chaining
     * @throws IllegalArgumentException If the {@link Enchantment} array argument is null
     */
    @NotNull
    public ItemBuilder removeEnchantments(@NotNull Enchantment... enchantments) {
        Preconditions.checkArgument(enchantments != null, "Enchantments array argument can't be null");

        for (Enchantment enchantment : enchantments) {
            itemStack.removeEnchantment(enchantment);
        }

        return this;
    }

    /**
     * Appends new lines of lore at the given index of the {@link ItemStack}'s lore from a given list.
     *
     * @param index Index at which the specified new lines of lore will be added
     * @param lines List of lore lines that'll be added
     * @return This instance, useful for chaining
     * @throws IllegalArgumentException If the {@link Collection}&lt;{@link String}&gt; argument is null
     */
    @NotNull
    public ItemBuilder addLoreAt(int index, @NotNull Collection<String> lines) {
        Preconditions.checkArgument(lines != null, "Collection<String> of lore lines argument can't be null");

        List<String> lore = getLore();
        lore.addAll(index, lines);

        return lore(lore);
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
    @NotNull
    public ItemBuilder loreAt(@Nullable String line, @NotNull int... indexes) {
        Preconditions.checkArgument(indexes != null, "Array of lore line indexes argument can't be null");

        List<String> lore = getItemMeta().map(ItemMeta::getLore).orElse(null);
        lore = lore == null || lore.isEmpty() ? new ArrayList<>(Utility.max(indexes) + 1) : lore;

        for (int index : indexes) {
            lore.set(index, line);
        }

        return lore(lore);
    }

    /**
     * Modifies the {@link ItemStack} with given operations.
     *
     * @param itemConsumer Consumer or anonymous function that'll take this instance's item as an argument
     * @return This instance, useful for chaining
     * @throws IllegalArgumentException If the {@link Consumer}&lt;{@link ItemStack}&gt; argument is null
     */
    @NotNull
    public ItemBuilder changeItem(@NotNull Consumer<ItemStack> itemConsumer) {
        Preconditions.checkArgument(itemConsumer != null, "Consumer<ItemStack> argument can't be null");
        itemConsumer.accept(itemStack);
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
    @NotNull
    public ItemBuilder changeMeta(@NotNull Consumer<ItemMeta> metaConsumer) {
        Preconditions.checkArgument(metaConsumer != null, "Consumer<T extends ItemMeta> argument can't be null");

        getItemMeta().ifPresent(meta -> {
            metaConsumer.accept(meta);
            itemStack.setItemMeta(meta);
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
    @NotNull
    public ItemBuilder addLoreAt(int index, @NotNull String... lines) {
        Preconditions.checkArgument(lines != null, "String array of lore lines argument can't be null");
        return addLoreAt(index, Arrays.asList(lines));
    }

    /**
     * Appends new lines of lore at the end of the {@link ItemStack}'s lore from a given list.
     *
     * @param lines List of lore lines that'll be added
     * @return This instance, useful for chaining
     * @throws IllegalArgumentException If the {@link Collection}&lt;{@link String}&gt; argument is null
     */
    @NotNull
    public ItemBuilder addLore(@NotNull Collection<String> lines) {
        Preconditions.checkArgument(lines != null, "Collection<String> of lore lines argument can't be null");

        List<String> lore = getLore();
        lore.addAll(lines);

        return lore(lore);
    }

    /**
     * Removes given ItemFlags from the {@link ItemStack}.
     *
     * @param flags ItemFlags that'll be removed from the {@link ItemStack}
     * @return This instance, useful for chaining
     * @throws IllegalArgumentException If the {@link ItemFlag} array is null
     */
    @NotNull
    public ItemBuilder removeFlags(@NotNull ItemFlag... flags) {
        Preconditions.checkArgument(flags != null, "ItemFlag array of enums argument can't be null");
        return changeMeta(meta -> meta.removeItemFlags(flags));
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
    @NotNull
    public ItemBuilder removeLoreAt(@NotNull int... indexes) {
        Preconditions.checkArgument(indexes != null, "Array of lore line indexes argument can't be null");

        List<String> lore = getLore();

        for (int index : indexes) {
            lore.remove(index);
        }

        return lore(lore);
    }

    /**
     * Sets a new type of material of item for the {@link ItemStack}.
     *
     * @param material New type of material that the {@link ItemStack} will be
     * @return This instance, useful for chaining
     * @throws IllegalArgumentException If the {@link Material} argument is null
     */
    @NotNull
    public ItemBuilder material(@NotNull Material material) {
        Preconditions.checkArgument(material != null, "Material argument can't be null");
        return changeItem(item -> item.setType(material));
    }

    /**
     * Sets a newly given localized name for the {@link ItemStack}.
     *
     * @param name New localized name that the {@link ItemStack} will have
     * @return This instance, useful for chaining
     */
    @NotNull
    public ItemBuilder localizedName(@Nullable String name) {
        return changeMeta(meta -> meta.setLocalizedName(name));
    }

    /**
     * Adds new ItemFlags to the {@link ItemStack}.
     *
     * @param flags Array of ItemFlag enums
     * @return This instance, useful for chaining
     * @throws IllegalArgumentException If the {@link ItemFlag} array is null
     */
    @NotNull
    public ItemBuilder addFlags(@NotNull ItemFlag... flags) {
        Preconditions.checkArgument(flags != null, "ItemFlag array of enums argument can't be null");
        return changeMeta(meta -> meta.addItemFlags(flags));
    }

    /**
     * Sets new lines of lore of the {@link ItemStack}'s lore from a given list.
     *
     * @param lines List of lore lines, lore will be removed if it's NULL
     * @return This instance, useful for chaining
     */
    @NotNull
    public ItemBuilder lore(@Nullable List<String> lines) {
        return changeMeta(meta -> meta.setLore(lines));
    }

    /**
     * Appends new lines of lore at the end of the {@link ItemStack}'s lore from a given array.
     *
     * @param lines Array of lore lines that'll be added
     * @return This instance, useful for chaining
     * @throws IllegalArgumentException If the {@link String} array of lore lines is null
     */
    @NotNull
    public ItemBuilder addLore(@NotNull String... lines) {
        Preconditions.checkArgument(lines != null, "String array of lore lines argument can't be null");
        return addLore(Arrays.asList(lines));
    }

    /**
     * Sets a new {@link ItemMeta} for the {@link ItemStack}.
     *
     * @param meta New {@link ItemMeta}, will be removed if it's NULL
     * @return This instance, useful for chaining
     */
    @NotNull
    public ItemBuilder itemMeta(@Nullable ItemMeta meta) {
        return changeItem(item -> item.setItemMeta(meta));
    }

    /**
     * Sets new lines of lore of the {@link ItemStack}'s lore from a given array.
     *
     * @param lines Array of lore lines, lore will be removed if it's NULL
     * @return This instance, useful for chaining
     */
    @NotNull
    public ItemBuilder lore(@Nullable String... lines) {
        return lore(lines == null ? null : Arrays.asList(lines));
    }

    /**
     * Sets a new given display name/title for the {@link ItemStack}.
     *
     * @param title New display name/title of an {@link ItemStack}
     * @return This instance, useful for chaining
     */
    @NotNull
    public ItemBuilder title(@Nullable String title) {
        return changeMeta(meta -> meta.setDisplayName(title));
    }

    /**
     * Sets whether the {@link ItemStack} can lose it's durability through use.
     *
     * @param breakable Whether the {@link ItemStack} can lose it's durability through use
     * @return This instance, useful for chaining
     */
    @NotNull
    public ItemBuilder breakable(boolean breakable) {
        return changeMeta(meta -> meta.setUnbreakable(!breakable));
    }

    /**
     * Sets a given amount of items of the {@link ItemStack}.
     *
     * @param amount Amount of items in an {@link ItemStack}
     * @return This instance, useful for chaining
     */
    @NotNull
    public ItemBuilder amount(int amount) {
        return changeItem(item -> item.setAmount(amount));
    }

    /**
     * Removes all lines of lore from the {@link ItemStack}'s lore.
     *
     * @return This instance, useful for chaining
     */
    @NotNull
    public ItemBuilder clearLore() {
        return lore((ArrayList<String>) null);
    }

    /**
     * Returns the {@link ItemMeta} of the {@link ItemStack}.
     *
     * @return {@link ItemMeta} of the {@link ItemStack}
     */
    @NotNull
    public Optional<ItemMeta> getItemMeta() {
        return Optional.ofNullable(itemStack.getItemMeta());
    }

    /**
     * Returns a {@link List} of lore lines of the {@link ItemStack}.
     *
     * @return Not null list of lore lines of the {@link ItemStack}, possibly empty
     */
    @NotNull
    public List<String> getLore() {
        return getItemMeta().map(ItemMeta::getLore).orElse(new ArrayList<>());
    }

    /**
     * Returns whether the {@link ItemStack} can lose it's durability through use.
     *
     * @return Whether the {@link ItemStack} can lose it's durability through use
     */
    public boolean isBreakable() {
        return getItemMeta().map(meta -> !meta.isUnbreakable()).orElse(true);
    }

    /**
     * Simply returns the {@link ItemStack} that's being modified by this {@link ItemBuilder}.
     *
     * @return {@link ItemStack} that's being modified
     */
    @NotNull
    public ItemStack get() {
        return itemStack;
    }

    @Override
    public Object clone() {
        return new ItemBuilder(itemStack.clone());
    }
}