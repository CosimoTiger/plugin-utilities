# plugin-utilities

A library with some common utilities for ease of developing Spigot plugins.

# Features

- menu library for custom menus and handling of all their events
- `ItemBuilder` for less headache with `ItemMeta` and `ItemStack` modifications
- cooldowns that manage themselves
- plugin file handling
- miscellaneous utilities

## The menu library

The menu library lets the developer wrap any `Inventory` with the menu classes, take control of all its common
incoming `InventoryEvents` and edit its contents and viewers. The library tries to not assume too much for the
developer, reinvent inventories or interfere with them, but disables most menu interactions, which can be easily
overriden by the developer; it also comes with some pre-made classes that can be extended.

To use the library, a `MenuManager` needs to be instantiated, which will manage all the menus that are passed to it.

## How to use `plugin-utilities` (WIP)

1. Add the library as a JitPack dependency via a build automation that you're using, such as Maven, Gradle, Ant, etc.
2. Shade the dependency, possibly with the JAR minimization option to exclude the utilities you're not using, which will
   lower the file size impact of the dependency even though it's currently rather small.
3. Write your code. The menu library, for example, requires you to instantiate a `MenuManager` with your `Plugin`
   instance to use it.
