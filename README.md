# plugin-utilities
A library with many utilities that help with developing Bukkit or Spigot plugins.
## Possible issues
- The class name "AbstractSlotProperty" is kind of lengthy, especially when it's an Optional<AbstractSlotProperty>
- The menu system might fail when it comes to server reloads (through the /reload command)
- Is the PluginDisableEvent for menus needed at all or should it be handled better?
- The library is barely tested for any bugs
## Planned features
- Updating to Java 9 would bring the ifPresentOrElse(Consumer<T>, Runnable) method
- A glow manager that can show glow and color selectively (packets are sent only to selected players), including entities
- Utility for making games, such as dividing a minigame into GamePhases
