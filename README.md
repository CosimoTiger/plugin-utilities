# plugin-utilities

An optimized and lightweight toolkit of flexible common utilities for developing Spigot API plugins.

---

# Features

- flexible menu package for easily making custom menus with clickable `ItemStack`s managed through Spigot events
- `ItemBuilder` for less headache with `ItemMeta` and `ItemStack` modifications
- cooldowns package for tracking and renewing of expireable durations
- plugin file package with methods for managing custom configuration files since only the default `config.yml` is
  handled

---

## The menu package

Many menu libraries and frameworks exist, though they introduce an excess of assumed features with an already
unoptimized and bloated core. This simple menu package integrates with the event-driven Spigot API, lets the developer 
wrap any existing or new `Inventory` with the `IMenu` subclasses, take control of all its incoming `InventoryEvent`s and
edit its contents and viewers at any moment. The menus offer a fluent and flexible builder design pattern that
doesn't require subclassing for simpler use-cases.

The design principle of this package is the following:

- menus don't need to store states or properties, only actions
- states can be stored in action closures or external variables
- any extra features are usually unnecessary and can be done with existing code
    - e.g. pagination can be done using lambdas, caching through array lists or linked lists, and at any point in the
      code; it's a matter of use-case

> [!IMPORTANT]
> To let the menus work, simply do the following in your plugin's initialization: 
`MenuManager.setInstance(new MenuManager(plugin));`

### Slot utilities

Convert row-column coordinate pair indexing to slot indices by choosing zero-based or one-based indexing of rows and
columns:

```java
// For default chest inventories:
int zeroBasedSlotIndex = Slot.Zero.from(2, 3);
int oneBasedSlotIndex = Slot.One.from(3, 4);

// Given an inventory context for calculating with columns:
var menu = new Menu(Bukkit.createInventory(null, InventoryType.DISPENSER, "Example menu"));

int slot = Slot.One.from(1, 3, menu.getInventory());

menu.set(new ItemStack(Material.APPLE), slot);
menu.set(new ItemStack(Material.COOKIE), menu -> IntStream.range(0, menu.getInventory().getSize()).toArray());
```

## The `ItemBuilder`

The extensive `ItemBuilder` class simplifies the creation of `ItemStack` objects with a fluent and expressive design:

```java
final var playerHead = new ItemBuilder(Material.PLAYER_HEAD)
        .name(ChatColor.AQUA + player.getName())
        .withMeta(meta -> meta.setOwningPlayer(player), SkullMeta.class)
        .lore(ChatColor.GRAY + "Get your own player head",
              ChatColor.translateAlternateColorCodes('&', "&7using the &e/head&7 command."))
        .amount(7)
        .glint()
        .build();
final var customArmor = new ItemBuilder(Material.LEATHER_CHESTPLATE, ChatColor.BLUE + "Blue team suit")
        .withMeta(meta -> {
            meta.setColor(Color.fromRGB(52, 82, 235));
            meta.addEnchant(Enchantment.FIRE_PROTECTION, 3, false);

            if (meta.getAttributeModifiers() == null) {
                meta.setAttributeModifiers(HashMultimap.create(2, 2));
            }
        }, LeatherArmorMeta.class)
        .addLore(ChatColor.translateAlternateColorCodes('&', "&7You're on the &9Blue team&7!"),
                 "",
                 ChatColor.translateAlternateColorCodes('&', "&7Type &e/team <color> &7to switch teams."))
        .addFlags(ItemFlag.HIDE_DYE, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP)
        .build();

player.getInventory().addItem(playerHead, customArmor);
```

---

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
       // Replace the "VERSION" with the
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
    
          final var config = new YamlFile(this, "config.yml").reloadFile().getMemory();
    
          getCommand("example").setExecutor(new ExampleCommand(config.getConfigurationSection("commands.example")));
       }
    }
    ```

---

## Contributing

We welcome contributions from the community, through forking or opening an issue. They should be in line with the
project's main goals: optimized code that benefits everyone.

---

## License

This project is licensed under the [MIT License](./LICENSE).

---

## Support

For questions, issues or feature requests, please use the 
[GitHub Issues](https://github.com/CosimoTiger/plugin-utilities/issues) section.

---
