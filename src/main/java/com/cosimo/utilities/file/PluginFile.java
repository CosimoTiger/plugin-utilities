package com.cosimo.utilities.file;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Optional;

/**
 * Representation and controller of a file owned by a certain plugin, can be subclassed for specific file types and
 * memory representations. The single goal of this controller is to copy a given original file into the destination
 * plugin data folder.
 *
 * @param <T> Memory representation type that's a result of loading this file
 */
@Getter
public abstract class PluginFile<T> extends File {

    /**
     * Plugin that this file belongs to, used for {@link Plugin#getResource(String)}
     */
    private final @NonNull Plugin plugin;
    /**
     * Relative file path of the source file in the resources directory to copy into destination path
     */
    private final @NonNull String resourcePath;

    /**
     * Creates a new instance from a given relative directory path inside the JAR resources folder of the file to copy
     * to the given relative destination path inside the {@link Plugin#getDataFolder()}.
     *
     * @param plugin          Plugin that this file belongs to, used for {@link Plugin#getResource(String)}
     * @param resourcePath    Relative file path of the source file in the resources directory to copy into destination
     *                        path
     * @param destinationPath Relative file path for the destination file in the {@link Plugin#getDataFolder()}
     */
    public PluginFile(@NonNull Plugin plugin, @NonNull String resourcePath, @NonNull String destinationPath) {
        super(plugin.getDataFolder(), destinationPath);
        this.resourcePath = resourcePath;
        this.plugin = plugin;
    }

    /**
     * Creates a new instance from a given relative directory path inside the JAR resources folder of the file to copy
     * to the same relative destination path inside the {@link Plugin#getDataFolder()}.
     *
     * <p>A more illustrative example would be to imagine that if the developer were to apply this class to each file
     * in their plugin's resources folder, they'd end up with the same directory structure in the destination directory
     * {@link Plugin#getDataFolder()}. The other constructor allows for customization of the destination directories and
     * file names, unlike this constructor.</p>
     *
     * @param plugin       Plugin that this file belongs to, used for {@link Plugin#getResource(String)}
     * @param resourcePath Relative file path of the source file in the resources directory to copy into destination
     *                     path
     */
    public PluginFile(@NonNull Plugin plugin, @NonNull String resourcePath) {
        this(plugin, resourcePath, resourcePath);
    }

    /**
     * Creates a new {@link PluginFile} if it doesn't exist in its location in the file system by copying it from the
     * plugin's embedded resources folder.
     *
     * @return Whether this file already existed
     */
    public boolean createFile() {
        final var existed = this.exists();

        if (!existed) {
            Optional.ofNullable(this.getParentFile()).ifPresent(File::mkdirs);

            try (final var inputStream = this.plugin.getResource(this.getResourcePath())) {
                Objects.requireNonNull(inputStream, () -> "Plugin %s's resource %s doesn't exist".formatted(
                        this.getPlugin().getDescription().getFullName(), this.getName()));
                Files.copy(inputStream, this.toPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return existed;
    }

    /**
     * Returns the data stored in this instance after reading the file usually after {@link #reloadFile()} and being
     * modified during runtime.
     *
     * @return Whichever object data type is specified by this instance
     */
    @Nullable
    public abstract T getMemory();

    /**
     * Overwrites the stored memory in this instance, should a custom memory be loaded.
     *
     * @param newMemory Not null object
     * @return This instance, useful for chaining
     */
    @NonNull
    public abstract PluginFile<T> setMemory(@NonNull T newMemory);

    /**
     * Reads the data from the file, parses it if needed and loads it for {@link #getMemory()}.
     *
     * @return This instance, useful for chaining
     */
    public abstract PluginFile<T> reloadFile();

    /**
     * Saves the possibly modified data of this file to its plugin file location.
     *
     * @return This instance, useful for chaining
     */
    public abstract PluginFile<T> saveFile();
}