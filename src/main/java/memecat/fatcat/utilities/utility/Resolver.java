package memecat.fatcat.utilities.utility;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.Optional;

/**
 * Reflection utility.
 */
public class Resolver {

    private final Class<?> clazz;

    public Resolver(@Nonnull Class<?> clazz) {
        this.clazz = clazz;
    }

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

    @Nonnull
    public Class<?> getClazz() {
        return this.clazz;
    }
}