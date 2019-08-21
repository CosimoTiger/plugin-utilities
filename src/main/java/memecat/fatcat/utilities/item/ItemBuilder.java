package memecat.fatcat.utilities.item;

import com.google.common.base.Preconditions;
import memecat.fatcat.utilities.Utility;
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
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * A class that wraps around a given or created {@link ItemStack}, allowing easier and chained modifications of the item.
 */
public class ItemBuilder {

    /**
     * {@link ItemStack} that is ready for modification.
     */
    private ItemStack itemStack;

    /**
     * Creates a new {@link ItemBuilder} from an item stored in a given configuration file's path or a certain item in
     * case of non-existence of path or object at path.
     *
     * @param configuration YAML configuration file in which the item is stored
     * @param path          Path at which the item is stored at
     * @param failure       {@link ItemStack} object that will be used instead if the configuration value is non-existent
     */
    public ItemBuilder(@NotNull FileConfiguration configuration, @NotNull String path, @NotNull ItemStack failure) {
        this(configuration.getItemStack(path, failure));
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
     * Creates a new {@link ItemBuilder} from the given item, renaming it to a given title.
     *
     * @param item  {@link ItemStack} that's being wrapped by this class for further modification
     * @param title Display name or visible title of an item
     * @throws IllegalArgumentException If the ItemStack argument is null or of Material.AIR type
     */
    public ItemBuilder(@NotNull ItemStack item, @Nullable String title) {
        Preconditions.checkArgument(item != null, "Item argument can't be null");
        Preconditions.checkArgument(!item.getType().equals(Material.AIR), "Item type can't be of Material.AIR type");

        this.itemStack = item;
        title(title);
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
     * @param item {@link ItemStack} that's being wrapped by this class for further modification
     * @throws IllegalArgumentException If the ItemStack argument is null or of Material.AIR type
     */
    public ItemBuilder(@NotNull ItemStack item) {
        this(item, null);
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
     */
    @NotNull
    public <T extends ItemMeta> ItemBuilder changeMeta(@NotNull Class<T> metaClass, @NotNull Consumer<T> metaConsumer) {
        Preconditions.checkArgument(metaClass != null, "Class<T extends ItemMeta> argument can't be null");
        Preconditions.checkArgument(metaConsumer != null, "Consumer<T extends ItemMeta> argument can't be null");

        ItemMeta meta = getItemMeta();

        if (metaClass.isInstance(meta)) {
            metaConsumer.accept(metaClass.cast(meta));
            itemMeta(meta);
        }

        return this;
    }

    /**
     * Removes all enchantments that are listed in a given list from the {@link ItemStack}.
     *
     * @param enchantments Enchantment(s) that will be removed from the {@link ItemStack}
     * @return This instance, useful for chaining
     */
    @NotNull
    public ItemBuilder removeEnchantments(@NotNull Collection<Enchantment> enchantments) {
        Preconditions.checkArgument(enchantments != null, "Enchantments list argument can't be null");
        enchantments.forEach(enchantment -> itemStack.removeEnchantment(enchantment));
        return this;
    }

    /**
     * Removes all enchantments that are listed in a given array from the {@link ItemStack}.
     *
     * @param enchantments Enchantment(s) that will be removed from the {@link ItemStack}
     * @return This instance, useful for chaining
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
     */
    @NotNull
    public ItemBuilder addLoreAt(int index, @NotNull Collection<String> lines) {
        Preconditions.checkArgument(lines != null, "Collection<String> of lore lines argument can't be null");

        List<String> lore = getLore();
        lore.addAll(index, lines);

        return lore(lore);
    }

    /**
     * Modifies the {@link ItemStack} with given operations.
     *
     * @param itemConsumer Consumer or anonymous function that'll take this instance's item as an argument
     * @return This instance, useful for chaining
     */
    @NotNull
    public ItemBuilder changeItem(@NotNull Consumer<ItemStack> itemConsumer) {
        Preconditions.checkArgument(itemConsumer != null, "Consumer<ItemStack> argument can't be null");
        itemConsumer.accept(itemStack);
        return this;
    }

    /**
     * Sets/repeats a new given line of lore at given <strong>existing</strong> indexes of the {@link ItemStack}'s lore.
     * <p>
     * If there is no existing lore, this method will create a new list with the initial size of the biggest given index
     * plus 1 and proceed to set the line at given indexes.
     *
     * @param indexes Indexes at which the specified lore line will be set
     * @param line    New line of lore
     * @return This instance, useful for chaining
     */
    @NotNull
    public ItemBuilder loreAt(@Nullable String line, @NotNull int... indexes) {
        Preconditions.checkArgument(indexes != null, "Array of lore line indexes argument can't be null");

        List<String> lore = getItemMeta().getLore();
        lore = lore == null || lore.isEmpty() ? new ArrayList<>(Utility.max(indexes) + 1) : lore;

        for (int index : indexes) {
            lore.set(index, line);
        }

        return lore(lore);
    }

    /**
     * Modifies the {@link ItemStack}'s {@link ItemMeta} with given operations.
     *
     * @param metaConsumer Consumer or anonymous function that'll take this instance's item's {@link ItemMeta} as an
     *                     argument
     * @return This instance, useful for chaining
     */
    @NotNull
    public ItemBuilder changeMeta(@NotNull Consumer<ItemMeta> metaConsumer) {
        return changeMeta(ItemMeta.class, metaConsumer);
    }

    /**
     * Appends new lines of lore at the given index of the {@link ItemStack}'s lore from a given array.
     *
     * @param index Index at which the specified new lines of lore will be added
     * @param lines Array of lore lines that'll be added
     * @return This instance, useful for chaining
     */
    @NotNull
    public ItemBuilder addLoreAt(int index, @NotNull String... lines) {
        Preconditions.checkArgument(lines != null, "String array of lore lines argument can't be null");
        return addLoreAt(index, Arrays.asList(lines));
    }

    /**
     * Removes given ItemFlags from the {@link ItemStack}.
     *
     * @param flags ItemFlags that'll be removed from the {@link ItemStack}
     * @return This instance, useful for chaining
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
     * each time. If you want to remove a line and next lines after it, you would have to repeat the same index then.
     *
     * @param indexes Array of indexes at which each lore line should be removed
     * @return This instance, useful for chaining
     */
    public ItemBuilder removeLoreAt(@NotNull int... indexes) {
        Preconditions.checkArgument(indexes != null, "Array of lore line indexes argument can't be null");

        List<String> lore = getLore();

        if (!lore.isEmpty()) {
            for (int index : indexes) {
                lore.remove(index);
            }
        }

        return lore(lore);
    }

    /**
     * Sets a new type of material of item for the {@link ItemStack}.
     *
     * @param material New type of material that the {@link ItemStack} will be
     * @return This instance, useful for chaining
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
     * Appends new lines of lore at the end of the {@link ItemStack}'s lore from a given list.
     *
     * @param lines List of lore lines that'll be added
     * @return This instance, useful for chaining
     */
    @NotNull
    public ItemBuilder addLore(@NotNull List<String> lines) {
        Preconditions.checkArgument(lines != null, "List<String> of lore lines argument can't be null");

        List<String> lore = getLore();
        lore.addAll(lines);

        return lore(lore);
    }

    /**
     * Adds new ItemFlags to the {@link ItemStack}.
     *
     * @param flags Array of ItemFlag enums
     * @return This instance, useful for chaining
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
     * Returns the enchantment level of a given enchantment of the {@link ItemStack}.
     *
     * @param enchantment Enchantment that the {@link ItemStack} possibly has
     * @return Enchantment level of a given enchantment of the {@link ItemStack}
     */
    public int getEnchantmentLevel(@NotNull Enchantment enchantment) {
        return itemStack.getEnchantmentLevel(enchantment);
    }

    /**
     * Returns a map of enchantments (keys) to their power levels (values) of the {@link ItemStack}.
     *
     * @return Map of enchantments to their power levels of the {@link ItemStack}
     */
    @NotNull
    public Map<Enchantment, Integer> getEnchantments() {
        return itemStack.getEnchantments();
    }

    /**
     * Returns whether the {@link ItemStack} has a given item flag.
     *
     * @param flag Item flag enum
     * @return Whether the {@link ItemStack} contains the given item flag
     */
    public boolean hasFlag(@NotNull ItemFlag flag) {
        Preconditions.checkArgument(flag != null, "ItemFlag argument can't be null");
        return getItemMeta().hasItemFlag(flag);
    }

    /**
     * Returns the localized name (this is most likely part of a language locale) of the {@link ItemStack}.
     *
     * @return Localized name of the {@link ItemStack}
     */
    @NotNull
    public String getLocalizedName() {
        return getItemMeta().getLocalizedName();
    }

    /**
     * Returns the {@link ItemMeta} of the {@link ItemStack}.
     *
     * @return {@link ItemMeta} of the {@link ItemStack}
     */
    @NotNull
    public ItemMeta getItemMeta() {
        return itemStack.getItemMeta();
    }

    /**
     * Returns a {@link List} of lore lines of the {@link ItemStack}.
     *
     * @return Not null list of lore lines of the {@link ItemStack}, possibly empty
     */
    @NotNull
    public List<String> getLore() {
        return Optional.ofNullable(getItemMeta().getLore()).orElse(new ArrayList<>());
    }

    /**
     * Returns whether the {@link ItemStack} has an existing {@link ItemMeta}.
     *
     * @return Whether the {@link ItemStack} has an existing {@link ItemMeta}
     */
    public boolean hasItemMeta() {
        return itemStack.hasItemMeta();
    }

    /**
     * Returns whether the {@link ItemStack} can lose it's durability through use.
     *
     * @return Whether the {@link ItemStack} can lose it's durability through use
     */
    public boolean isBreakable() {
        return getItemMeta().isUnbreakable();
    }

    /**
     * Returns the type (material) of the {@link ItemStack}.
     *
     * @return Type (material) of the {@link ItemStack}
     */
    @NotNull
    public Material getMaterial() {
        return itemStack.getType();
    }

    /**
     * Returns the display name (title) of the {@link ItemStack}.
     *
     * @return Display name (title) of the {@link ItemStack}
     */
    @NotNull
    public String getTitle() {
        return getItemMeta().getDisplayName();
    }

    /**
     * Simply returns the {@link ItemStack} that's being modified by this {@link ItemBuilder}.
     *
     * @return {@link ItemStack} that's being modified
     */
    @NotNull
    public ItemStack build() {
        return itemStack;
    }

    /**
     * Returns the amount of items in the {@link ItemStack}.
     *
     * @return Amount of items in the {@link ItemStack}
     */
    public int getAmount() {
        return itemStack.getAmount();
    }
}