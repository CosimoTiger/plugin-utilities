package com.cosimo.utilities.file;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

/**
 * Represents a YAML file that belongs to a plugin by holding the FileConfiguration object variable. When creating a new
 * instance or using the superclass constructor as a subclass, reloadFile() should be called to initialise the
 * {@link FileConfiguration} variable. This isn't done by default because calling superclass constructors and waiting on
 * them can be a problem when defining default class variables.
 */
public class ConfigFile extends PluginFile {

    /**
     * This file, in the form of a Spigot/Bukkit YAML file object.
     */
    protected FileConfiguration fileConfig;

    /**
     * Creates a new {@link ConfigFile} from a given file name in its given file path.
     * <p>
     * The File path parameter can be a plugin's data folder which is accessible through the method
     * Plugin#getDataFolder().
     *
     * @param plugin Plugin that this file belongs to
     * @param path   Folder file or a directory in which this file should be, also called a "parent" file
     * @param name   File name or path to file, also called a "child" file, ending with a file extension
     */
    public ConfigFile(@NotNull Plugin plugin, @Nullable File path, @NotNull String name) {
        super(plugin, path, name);
    }

    /**
     * Creates a new {@link ConfigFile} from a given file name in its given file path.
     * <p>
     * The File path parameter can be a plugin's data folder which is accessible through the method
     * Plugin#getDataFolder().
     *
     * @param plugin Plugin that this file belongs to
     * @param path   Folder file or a directory in which this file should be, also called a "parent" file
     * @param name   File name or path to file, also called a "child" file, ending with a file extension
     */
    public ConfigFile(@NotNull Plugin plugin, @Nullable String path, @NotNull String name) {
        super(plugin, path, name);
    }

    /**
     * Creates a new {@link ConfigFile} from a given file path, ending with its name and extension.
     *
     * @param plugin Plugin that this file belongs to
     * @param path   File path to this file, ending with its name and an extension
     */
    public ConfigFile(@NotNull Plugin plugin, @NotNull String path) {
        super(plugin, path);
    }

    /**
     * Creates a new {@link ConfigFile} from a given URI path pointing to it.
     *
     * @param plugin Plugin that this file belongs to
     * @param uri    URI parameter that is used in the creation of {@link java.io.File}
     */
    public ConfigFile(@NotNull Plugin plugin, @NotNull URI uri) {
        super(plugin, uri);
    }

    /**
     * Uses {@link #createFile()} first to make sure this file exists, then proceeds to load the
     * {@link FileConfiguration} which can be accessed through the {@link #getConfig()} getter method or the protected
     * variable.
     */
    @Override
    public void reloadFile() {
        this.createFile();
        this.fileConfig = YamlConfiguration.loadConfiguration(this);

        try (InputStream inputStream = new FileInputStream(this)) {
            this.fileConfig.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(inputStream)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveFile() {
        if (this.fileConfig == null) {
            return;
        }

        try {
            this.fileConfig.save(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the {@link FileConfiguration} that this class holds.
     *
     * @return {@link FileConfiguration} object
     */
    @NotNull
    public FileConfiguration getConfig() {
        if (this.fileConfig == null) {
            this.reloadFile();
        }

        return this.fileConfig;
    }
}