package memecat.fatcat.utilities.file;

import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Optional;

/**
 * This class can be extended to create instances that represent a file that belongs to a specific plugin.
 */
public abstract class PluginFile extends File {

    /**
     * Plugin that this file belongs to.
     */
    private final Plugin plugin;

    /**
     * Creates a new {@link PluginFile} from a given file name in its given file path.
     * <p>
     * The File path parameter can be a plugin's data folder which is accessible through the method
     * Plugin#getDataFolder().
     *
     * @param plugin Plugin that this file belongs to
     * @param path   Folder file or a directory in which this file should be, also called a "parent" file
     * @param name   File name or path to file, also called a "child" file, ending with a file extension
     */
    public PluginFile(@Nonnull Plugin plugin, @Nullable File path, @Nonnull String name) {
        super(path, name);
        this.plugin = plugin;
    }

    /**
     * Creates a new {@link PluginFile} from a given file name in its given file path.
     * <p>
     * The File path parameter can be a plugin's data folder which is accessible through the method
     * Plugin#getDataFolder().
     *
     * @param plugin Plugin that this file belongs to
     * @param path   Folder file or a directory in which this file should be, also called a "parent" file
     * @param name   File name or path to file, also called a "child" file, ending with a file extension
     */
    public PluginFile(@Nonnull Plugin plugin, @Nullable String path, @Nonnull String name) {
        super(path, name);
        this.plugin = plugin;
    }

    /**
     * Creates a new {@link PluginFile} from a given file path, ending with it's name and extension.
     *
     * @param plugin Plugin that this file belongs to
     * @param path   File path to this file, ending with it's name and an extension
     */
    public PluginFile(@Nonnull Plugin plugin, @Nonnull String path) {
        super(path);
        this.plugin = plugin;
    }

    /**
     * Creates a new {@link PluginFile} from a given URI path pointing to it.
     *
     * @param plugin Plugin that this file belongs to
     * @param uri    URI parameter that is used in the creation of {@link java.io.File}
     */
    public PluginFile(@Nonnull Plugin plugin, @Nonnull URI uri) {
        super(uri);
        this.plugin = plugin;
    }

    /**
     * Creates a new {@link PluginFile} if it doesn't exist in its location in the file system by copying it from the
     * plugin's embedded resources folder.
     *
     * @return Whether this file already existed
     */
    public boolean createFile() {
        if (!this.exists()) {
            Optional.ofNullable(this.getParentFile()).ifPresent(File::mkdirs);

            try (InputStream inputStream = this.plugin.getResource(this.getName())) {
                Objects.requireNonNull(inputStream, "Unable to find plugin " + this.getPlugin().getDescription().getFullName()
                        + "'s file /resources/" + this.getName() + "!");
                Files.copy(inputStream, this.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }

            return false;
        }

        return true;
    }

    /**
     * Reads the data from the file again and loads it.
     */
    public abstract void reloadFile();

    /**
     * Saves the file to its file location.
     */
    public abstract void saveFile();

    /**
     * Returns the plugin that this file belongs to.
     *
     * @return Plugin that this file belongs to
     */
    @Nonnull
    public Plugin getPlugin() {
        return this.plugin;
    }
}