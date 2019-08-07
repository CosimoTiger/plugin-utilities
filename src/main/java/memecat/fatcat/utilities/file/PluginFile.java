package memecat.fatcat.utilities.file;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;

/**
 * This class can be extended to create instances that represent a file that belongs to a specific plugin.
 */
public abstract class PluginFile extends File {

    /**
     * Plugin that this file belongs to.
     */
    private Plugin plugin;

    /**
     * Creates a new {@link PluginFile} from a given file name in it's given file path.
     * <p>
     * The File path parameter can be a plugin's data folder which is accessible through the method
     * Plugin#getDataFolder().
     *
     * @param plugin Plugin that this file belongs to
     * @param path   Folder file or a directory in which this file should be, also called a "parent" file
     * @param name   File name or path to file, also called a "child" file, ending with a file extension
     */
    public PluginFile(Plugin plugin, File path, String name) {
        super(path, name);

        this.plugin = plugin;
    }

    /**
     * Creates a new {@link PluginFile} from a given file name in it's given file path.
     * <p>
     * The File path parameter can be a plugin's data folder which is accessible through the method
     * Plugin#getDataFolder().
     *
     * @param plugin Plugin that this file belongs to
     * @param path   Folder file or a directory in which this file should be, also called a "parent" file
     * @param name   File name or path to file, also called a "child" file, ending with a file extension
     */
    public PluginFile(Plugin plugin, String path, String name) {
        super(path, name);

        this.plugin = plugin;
    }

    /**
     * Creates a new {@link PluginFile} from a given file path, ending with it's name and extension.
     *
     * @param plugin Plugin that this file belongs to
     * @param path   File path to this file, ending with it's name and an extension
     */
    public PluginFile(Plugin plugin, String path) {
        super(path);

        this.plugin = plugin;
    }

    /**
     * Creates a new {@link PluginFile} from a given URI path pointing to it.
     *
     * @param plugin Plugin that this file belongs to
     * @param uri    URI parameter that is used in the creation of {@link java.io.File}
     */
    public PluginFile(Plugin plugin, URI uri) {
        super(uri);

        this.plugin = plugin;
    }

    /**
     * Creates a new {@link PluginFile} if it doesn't exist in it's location in the file system by copying it from the
     * plugin's embedded resources folder.
     *
     * @return Whether this file already exists
     */
    public boolean createFile() {
        if (!exists()) {
            this.getParentFile().mkdirs();
            try (InputStream inputStream = plugin.getResource(getName())) {
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
     * Saves the file to it's file location.
     */
    public abstract void saveFile();

    /**
     * Returns the plugin that this file belongs to.
     *
     * @return Plugin that this file belongs to
     */
    @NotNull
    public Plugin getPlugin() {
        return plugin;
    }
}