package gg.auroramc.levels.hooks;

/**
 * Local replacement for Aurora 3's Hook so the plugin also builds against Aurora 2.x.
 */
public interface Hook<T> {
    /** Called on onEnable, only when the hooked plugin is enabled. */
    void hook(T plugin);

    /** Called on onLoad, only when the hooked plugin is present. */
    default void hookAtStartUp(T plugin) {
    }
}
