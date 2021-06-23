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

    @NotNull
    public Optional<Field> resolveField(@NotNull String name) {
        try {
            Field field = this.clazz.getDeclaredField(name);
            field.setAccessible(true);

            return Optional.of(field);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @NotNull
    public Class<?> getClazz() {
        return this.clazz;
    }
}