package memecat.fatcat.utilities.reflection;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

public class Resolver {

    private Class<?> clazz;

    public Resolver(@NotNull Class<?> object) {
        this.clazz = object;
    }

    // Could use an Optional<Field> here..
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