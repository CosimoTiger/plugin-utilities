package com.cosimo.utilities.file;

import lombok.NonNull;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Represents a YAML file that belongs to a plugin with its loaded {@link FileConfiguration}. When creating a new
 * instance or using the superclass constructor as a subclass, reloadFile() should be called to initialise the
 * {@link FileConfiguration} variable. This isn't done by default because calling superclass constructors and waiting on
 * them can be a problem when defining default class variables.
 */
public class YamlFile extends PluginFile<FileConfiguration> {

    /**
     * Data loaded from a YAML file
     */
    protected FileConfiguration fileConfig;

    public YamlFile(@NonNull Plugin plugin, @NonNull String resourcePath, @NonNull String destinationFile) {
        super(plugin, resourcePath, destinationFile);
    }

    public YamlFile(@NonNull Plugin plugin, @NonNull String resourcePath) {
        super(plugin, resourcePath);
    }

    /**
     * Copies this {@link PluginFile} from the resources folder to the destination first if it doesn't exist, then
     * proceeds to load the {@link FileConfiguration} which can be accessed through the {@link #getMemory()} getter
     * method.
     *
     * @return This instance, useful for chaining
     */
    @Override
    public YamlFile reloadFile() {
        this.createFile();
        this.fileConfig = YamlConfiguration.loadConfiguration(this);

        try (final var inputStream = new FileInputStream(this)) {
            this.fileConfig.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(inputStream)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return this;
    }

    @Override
    public YamlFile saveFile() {
        if (this.fileConfig != null) {
            try {
                this.fileConfig.save(this);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return this;
    }

    @Override
    public FileConfiguration getMemory() {
        if (this.fileConfig == null) {
            this.reloadFile();
        }

        return this.fileConfig;
    }

    @Nonnull
    @Override
    public PluginFile<FileConfiguration> setMemory(@NonNull FileConfiguration newMemory) {
        this.fileConfig = newMemory;
        return this;
    }
}