package com.cosimo.utilities.utility;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Optional;

/**
 * Reflection utility.
 */
public record Resolver(@NotNull Class<?> clazz) {

    @NotNull
    public Optional<Field> resolveField(@NotNull String name) {
        try {
            final Field field = this.clazz.getDeclaredField(name);
            field.setAccessible(true);

            return Optional.of(field);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }
}