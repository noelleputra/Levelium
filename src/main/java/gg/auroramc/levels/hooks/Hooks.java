package gg.auroramc.levels.hooks;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Local replacement for Aurora 3's Hooks: runs a hook only if its target plugin is present.
 */
public class Hooks<T extends JavaPlugin> {
    private record Entry<T>(Class<? extends Hook<T>> type, String pluginName) {
    }

    private final T plugin;
    private final List<Entry<T>> entries = new ArrayList<>();
    private final List<Hook<T>> loaded = new ArrayList<>();

    public Hooks(T plugin) {
        this.plugin = plugin;
    }

    public Hooks<T> add(Class<? extends Hook<T>> type, String pluginName) {
        entries.add(new Entry<>(type, pluginName));
        return this;
    }

    /** Call from onLoad. */
    public void load() {
        for (var entry : entries) {
            if (Bukkit.getPluginManager().getPlugin(entry.pluginName()) == null) continue;
            try {
                var hook = entry.type().getDeclaredConstructor().newInstance();
                hook.hookAtStartUp(plugin);
                loaded.add(hook);
            } catch (Throwable t) {
                plugin.getLogger().severe("Failed to load hook " + entry.type().getSimpleName() + ": " + t);
                t.printStackTrace();
            }
        }
    }

    /** Call from onEnable. */
    public void enable() {
        for (var hook : loaded) {
            try {
                hook.hook(plugin);
                if (hook instanceof Listener listener) {
                    Bukkit.getPluginManager().registerEvents(listener, plugin);
                }
            } catch (Throwable t) {
                plugin.getLogger().severe("Failed to enable hook " + hook.getClass().getSimpleName() + ": " + t);
                t.printStackTrace();
            }
        }
    }
}
