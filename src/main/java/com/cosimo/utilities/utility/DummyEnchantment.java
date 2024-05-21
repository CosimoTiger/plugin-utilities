package com.cosimo.utilities.utility;

import com.google.common.base.Preconditions;
import com.cosimo.utilities.UtilitiesPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.Optional;

public class DummyEnchantment extends Enchantment {

    public DummyEnchantment() {
        super(new NamespacedKey(getPlugin(), "dummy"));
    }

    @Override
    @Nonnull
    public String getName() {
        return "";
    }

    @Override
    public int getMaxLevel() {
        return 0;
    }

    @Override
    public int getStartLevel() {
        return 0;
    }

    @Override
    @Nonnull
    public EnchantmentTarget getItemTarget() {
        return null;
    }

    @Override
    public boolean isTreasure() {
        return false;
    }

    @Override
    public boolean isCursed() {
        return false;
    }

    @Override
    public boolean conflictsWith(@Nonnull Enchantment enchantment) {
        return false;
    }

    @Override
    public boolean canEnchantItem(@Nonnull ItemStack itemStack) {
        return true;
    }

    public static void register() {
        Resolver resolver = new Resolver(Enchantment.class);
        Optional<Field> optional = resolver.resolveField("acceptingNew");
        try {
            Preconditions.checkArgument(optional.isPresent(), "Cannot register new enchantment from invalid field.");
            Field field = optional.get();
            field.set(null, true);
            Enchantment.registerEnchantment(new DummyEnchantment());
        } catch (Exception ignore) {
        } finally {
            Enchantment.stopAcceptingRegistrations();
        }
    }

    private static UtilitiesPlugin getPlugin() {
        Optional<UtilitiesPlugin> instance = UtilitiesPlugin.getInstance();
        Preconditions.checkArgument(instance.isPresent(), "Cannot initialize enchantment with null plugin reference");
        return instance.get();
    }

}
