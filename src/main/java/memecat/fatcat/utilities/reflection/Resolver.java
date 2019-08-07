package memecat.fatcat.utilities.reflection;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

/**
 * A lack of documentation here as I, the developer, lack the knowledge on this.
 */
public class Resolver {

    private Class<?> clazz;

    public Resolver(@NotNull Class<?> object) {
        this.clazz = object;
    }

    public Field resolveField(@NotNull String name) {
        try {
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);

            return field;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Class<?> getClazz() {
        return clazz;
    }
}