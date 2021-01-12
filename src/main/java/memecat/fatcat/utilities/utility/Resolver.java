package memecat.fatcat.utilities.utility;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Optional;

/**
 * Reflection utility.
 */
public class Resolver {

    private final Class<?> clazz;

    public Resolver(@NotNull Class<?> clazz) {
        this.clazz = clazz;
    }

    public Optional<Field> resolveField(@NotNull String name) {
        try {
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);

            return Optional.of(field);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public Class<?> getClazz() {
        return clazz;
    }
}