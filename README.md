# plugin-utilities

A library with some common utilities for ease of developing Spigot plugins.

# Features

- central menu module for custom menus and handling of all their events
- `ItemBuilder` for less headache with `ItemMeta` and `ItemStack` modifications
- cooldowns that manage themselves
- plugin file handling

## The menu library

The menu library lets the developer wrap any `Inventory` with the menu classes, take control of all its common
incoming `InventoryEvents` and edit its contents and viewers. The library tries to not assume too much for the
developer, reinvent inventories or interfere with them, but disables most menu interactions, which can be easily
overriden by the developer; it also comes with some pre-made classes that can be extended.

To use the library, a `MenuManager` needs to be instantiated, which will manage all the menus that are passed to it.

## How to use `plugin-utilities`

1. Add the library as a JitPack dependency via a build automation tool that you're using, such as Maven, Gradle, Ant,
   etc.

    ```xml
   <project>
        <repositories>
            <repository>
                <id>jitpack.io</id>
                <url>https://jitpack.io</url>
            </repository>
        </repositories>
        
       <dependencies>
            <dependency>
               <groupId>com.github.CosimoTiger</groupId>
               <artifactId>plugin-utilities</artifactId>
               <version>1.0.0-alpha.2</version>
            </dependency>
       </dependencies>
   </project>
    ```

    ```groovy
    dependencyResolutionManagement {
        repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
        repositories {
            mavenCentral()
            maven { url 'https://jitpack.io' }
        }
    }
    
    dependencies {
        implementation 'com.github.CosimoTiger:plugin-utilities:1.0.0-alpha.2'
    }
    ```

2. **Optional step:** you can shade the dependency library using JAR minimization to exclude unused features from being
   compiled with your plugin, which will decrease the file size impact of the dependency even though it's currently very
   small and lightweight. Here's one way of doing it:

   Inside `<plugins>...</plugins>` add:

    ```xml
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-shade-plugin</artifactId>
        <version>3.6.0</version>
        <executions>
            <execution>
                <phase>package</phase>
                <goals>
                    <goal>shade</goal>
                </goals>
                <configuration>
                    <minimizeJar>true</minimizeJar>
                </configuration>
            </execution>
        </executions>
    </plugin>
    ```

   Beware of what these options do – if you're using Java Reflection, databases or possibly dependency injection, those
   dependencies may be affected, but don't fret because you
   can [exclude them from the process](https://maven.apache.org/plugins/maven-shade-plugin/examples/includes-excludes.html).

3. Start writing your code. Here's the typical plugin initialization code example:

    ```java
    import com.cosimo.utilities.file.YamlFile;
    import com.cosimo.utilities.menu.AbstractMenu;
    import com.cosimo.utilities.menu.manager.MenuManager;
    import org.bukkit.plugin.java.JavaPlugin;
    
    public class ExamplePlugin extends JavaPlugin {
    
       @Override
       public void onEnable() {
          new MenuManager(this);
    
          final var config = new YamlFile(this, "config.yml").reloadFile().getMemory();
    
          getCommand("example").setExecutor(new ExampleCommand(config.getConfigurationSection("commands.example")));
       }
    }
    ```
