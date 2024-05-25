package com.cosimo.utilities.utility;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.Optional;

/**
 * Reflection utility.
 */
public record Resolver(@Nonnull Class<?> clazz) {

    @Nonnull
    public Optional<Field> resolveField(@Nonnull String name) {
        try {
            Field field = this.clazz.getDeclaredField(name);
            field.setAccessible(true);

            return Optional.of(field);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }
}