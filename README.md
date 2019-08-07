# plugin-utilities
A small library with useful features and classes for Spigot and Bukkit plugin projects.
## Possible issues
- The class name "AbstractSlotProperty" is kind of lengthy, especially when it's an Optional<AbstractSlotProperty>
- The menu system might fail when it comes to server reloads (through the /reload command)
- Is the PluginDisableEvent for menus needed at all or should it be handled better?
- The library is barely tested for any bugs
## Planned features
- Updating to Java 9 would bring the ifPresentOrElse(Consumer<T>, Runnable) method
- A glow manager that can show glow and color selectively (packets are sent only to selected players), including entities
