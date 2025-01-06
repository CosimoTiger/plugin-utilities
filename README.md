# plugin-utilities

![Java](https://img.shields.io/badge/Language-Java-orange.svg)
![License](https://img.shields.io/badge/License-MIT-blue.svg)

## Overview

An optimized and lightweight toolkit of flexible common utilities for developing Spigot API plugins.

## Features

- menu package for easily making custom flexible menus with clickable `ItemStack`s managed in the background through Spigot events,
- `ItemBuilder` for less headache with `ItemMeta` and `ItemStack` modifications,
- cooldowns package for tracking and renewing of expirable durations,
- plugin file package with methods for managing multiple or custom configuration files

## Installation

1. Add the library as a JitPack dependency via a build automation tool that you're using, such as Maven, Gradle, sbt,
   etc. See all releases, snapshots and guides at
   the [official JitPack website of this project](https://jitpack.io/#CosimoTiger/plugin-utilities).

2. Shade the dependency library using JAR minimization to exclude unused features from being compiled with your plugin, 
   which will decrease the file size impact of the dependency while keeping your plugin lightweight. Even though the 
   dependency is small, this process ensures efficient packaging by eliminating unnecessary code.

   Here's how you can configure it using Maven Shade Plugin, by adding inside Maven `<plugins>...</plugins>`:

    ```xml
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-shade-plugin</artifactId>
        <version>VERSION</version> <!-- Replace VERSION with the latest plugin version -->
        <executions>
            <execution>
                <phase>package</phase>
                <goals>
                    <goal>shade</goal>
                </goals>
                <configuration>
                    <minimizeJar>true</minimizeJar>
                    <artifactSet>
                        <!-- Replace the "Tag" with the latest release version -->
                        <includes>com.github.CosimoTiger:plugin-utilities:Tag</includes>
                    </artifactSet>
                </configuration>
            </execution>
        </executions>
    </plugin>
    ```
   
    Here's how you can configure it using the Gradle Shadow plugin (Groovy):

    ```groovy
   plugins {
       id 'java'
       // Replace the "VERSION" with the latest release available at https://github.com/GradleUp/shadow
       id 'com.gradleup.shadow' version 'VERSION'
   }
   
   shadowJar {
       archiveClassifier.set('all')
   
       manifest {
           attributes 'Main-Class': 'path.to.your.PluginMain'
       }
   
       dependencies {
           // Replace the "Tag" with the latest release version
           include dependency('com.github.CosimoTiger:plugin-utilities:Tag')
       }
   }
   ```

3. Start writing your code. Here's the typical plugin initialization code example:

    ```java
    import com.cosimo.utilities.file.YamlFile;
    import com.cosimo.utilities.menu.manager.MenuManager;
    import org.bukkit.plugin.java.JavaPlugin;
    
    public class ExamplePlugin extends JavaPlugin {
    
       @Override
       public void onEnable() {
          MenuManager.setInstance(new MenuManager(this));
    
          final var config = new YamlFile(this, "command-config.yml").reloadFile().getMemory();
    
          getCommand("example").setExecutor(new ExampleCommand(config.getConfigurationSection("commands.example")));
       }
    }
    ```

## Contributing

We welcome contributions from the community, through forking or opening an issue. They should be in line with the
project's main goals: optimized code that benefits everyone.

## License

This project is licensed under the [MIT License](./LICENSE).

## Support

For questions, issues or feature requests, please use the 
[GitHub Issues](https://github.com/CosimoTiger/plugin-utilities/issues) section.
