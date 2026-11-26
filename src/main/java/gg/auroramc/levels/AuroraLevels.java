package gg.auroramc.levels;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.AuroraLogger;
import gg.auroramc.levels.hooks.Hooks;
import gg.auroramc.levels.api.AuroraLevelsProvider;
import gg.auroramc.levels.api.data.LevelData;
import gg.auroramc.levels.command.CommandManager;
import gg.auroramc.levels.config.ConfigManager;
import gg.auroramc.levels.hooks.auraskills.AuraSkillsHook;
import gg.auroramc.levels.hooks.ecoskills.EcoSkillsHook;
import gg.auroramc.levels.hooks.luckperms.LuckPermsHook;
import gg.auroramc.levels.hooks.mmoitems.MMOItemsHook;
import gg.auroramc.levels.hooks.mmolib.MMOLibHook;
import gg.auroramc.levels.hooks.mythic.MythicHook;
import gg.auroramc.levels.hooks.worldguard.WorldguardHook;
import gg.auroramc.levels.leveler.PlayerLeveler;
import gg.auroramc.levels.placeholder.LevelPlaceholderHandler;
import lombok.Getter;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class AuroraLevels extends JavaPlugin {
    @Getter
    private PlayerLeveler leveler;
    @Getter
    private ConfigManager configManager;

    private CommandManager commandManager;
    // Created in onLoad, after checking that Aurora is new enough (an older one doesn't have Hooks)
    @Getter
    private Hooks<AuroraLevels> hooks;

    private static AuroraLogger l;

    public static AuroraLogger logger() {
        return l;
    }

    // Set when Aurora is too old for this version, nothing else is done then
    private boolean incompatible = false;

    @Override
    public void onLoad() {
        if (!auroraIsCompatible()) {
            incompatible = true;
            return;
        }
        configManager = new ConfigManager(this);
        l = AuroraAPI.createLogger("AuroraLevels", () -> configManager.getLevelConfig().getDebug());
        hooks = new Hooks<>(this)
                .add(AuraSkillsHook.class, "AuraSkills")
                .add(EcoSkillsHook.class, "EcoSkills")
                .add(LuckPermsHook.class, "LuckPerms")
                .add(MMOItemsHook.class, "MMOItems")
                .add(MythicHook.class, "MythicMobs")
                .add(WorldguardHook.class, "WorldGuard")
                .add(MMOLibHook.class, "MythicLib");
        hooks.load();

        AuroraAPI.getLeaderboards().registerBoard(
                "levels",
                (user) -> leveler.getTotalXpForLevel(user.getData(LevelData.class).getLevel()) + user.getData(LevelData.class).getCurrentXP(),
                (entry) -> AuroraAPI.formatNumber(leveler.getLevelFromTotalXP(entry.getValue())),
                configManager.getLevelConfig().getLeaderboardCacheSize(),
                1
        );
    }

    @Override
    public void onEnable() {
        if (incompatible) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        AuroraAPI.getUserManager().registerUserDataHolder(LevelData.class);

        leveler = new PlayerLeveler(this);
        AuroraAPI.registerPlaceholderHandler(new LevelPlaceholderHandler(this));

        commandManager = new CommandManager(this);
        commandManager.reload();

        try {
            var field = AuroraLevelsProvider.class.getDeclaredField("plugin");
            field.setAccessible(true);
            field.set(null, this);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            l.severe("Failed to initialize api provider!");
            e.printStackTrace();
        }

        hooks.enable();

        Bukkit.getGlobalRegionScheduler().run(this, (task) -> {
            var config = configManager.getLevelConfig();
            leveler.getLevelMatcher().reload(config.getLevelMatchers(), config.getCustomLevels());
        });

        new Metrics(this, 23777);
    }

    public void reload() {
        configManager.reload();
        commandManager.reload();
        leveler.reload(false);

        var players = new ArrayList<>(Bukkit.getOnlinePlayers());

        CompletableFuture.runAsync(() -> {
            for (var player : players) {
                if (!player.isOnline()) continue;
                leveler.correctCurrentXP(player);
                leveler.getRewardAutoCorrector().correctRewards(player);
            }
        });
    }

    @Override
    public void onDisable() {
        if (commandManager != null) commandManager.unregisterCommands();
    }

    // Check the minimum version before loading code that depends on Aurora 2.5 APIs.
    private boolean auroraIsCompatible() {
        var aurora = Bukkit.getPluginManager().getPlugin("Aurora");
        var version = aurora == null ? "none" : aurora.getPluginMeta().getVersion();
        try {
            var parts = version.split("[.-]");
            int major = Integer.parseInt(parts[0]);
            int minor = Integer.parseInt(parts[1]);
            int patch = Integer.parseInt(parts[2]);
            if (major == 2 && (minor > 5 || minor == 5 && patch >= 3)) return true;
        } catch (NumberFormatException e) {
            // Report unparseable versions as incompatible below.
        } catch (ArrayIndexOutOfBoundsException e) {
            // Report incomplete versions as incompatible below.
        }
        getLogger().severe(getName() + " " + getPluginMeta().getVersion() + " requires Aurora 2.5.3 or newer in the 2.x series, but Aurora " + version + " is installed.");
        return false;
    }
}