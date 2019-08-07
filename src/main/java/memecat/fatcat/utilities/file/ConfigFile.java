package memecat.fatcat.utilities.file;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

/**
 * Represents a YAML file that is stored inside of a plugin's data folder, holding the FileConfiguration object variable.
 */
public class ConfigFile extends PluginFile {

    /**
     * This file, in the form of a Spigot/Bukkit YAML file object.
     */
    private FileConfiguration fileConfig;

    /**
     * Creates a new {@link ConfigFile} from a given file name in it's given file path.
     * <p>
     * The File path parameter can be a plugin's data folder which is accessible through the method
     * Plugin#getDataFolder().
     *
     * @param plugin Plugin that this file belongs to
     * @param path   Folder file or a directory in which this file should be, also called a "parent" file
     * @param name   File name or path to file, also called a "child" file, ending with a file extension
     */
    public ConfigFile(Plugin plugin, File path, String name) {
        super(plugin, path, name);

        reloadFile();
    }

    /**
     * Creates a new {@link ConfigFile} from a given file name in it's given file path.
     * <p>
     * The File path parameter can be a plugin's data folder which is accessible through the method
     * Plugin#getDataFolder().
     *
     * @param plugin Plugin that this file belongs to
     * @param path   Folder file or a directory in which this file should be, also called a "parent" file
     * @param name   File name or path to file, also called a "child" file, ending with a file extension
     */
    public ConfigFile(Plugin plugin, String path, String name) {
        super(plugin, path, name);

        reloadFile();
    }

    /**
     * Creates a new {@link ConfigFile} from a given file path, ending with it's name and extension.
     *
     * @param plugin Plugin that this file belongs to
     * @param path   File path to this file, ending with it's name and an extension
     */
    public ConfigFile(Plugin plugin, String path) {
        super(plugin, path);

        reloadFile();
    }

    /**
     * Creates a new {@link ConfigFile} from a given URI path pointing to it.
     *
     * @param plugin Plugin that this file belongs to
     * @param uri    URI parameter that is used in the creation of {@link java.io.File}
     */
    public ConfigFile(Plugin plugin, URI uri) {
        super(plugin, uri);

        reloadFile();
    }

    @Override
    public void reloadFile() {
        createFile();

        this.fileConfig = YamlConfiguration.loadConfiguration(this);

        try (InputStream inputStream = new FileInputStream(this)) {
            this.fileConfig.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(inputStream)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveFile() {
        if (fileConfig == null) {
            reloadFile();
        }

        try {
            fileConfig.save(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the configuration file that this class holds.
     *
     * @return Spigot/Bukkit FileConfiguration object, possibly NULL
     */
    @NotNull
    public FileConfiguration getFile() {
        return fileConfig;
    }
}