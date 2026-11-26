package gg.auroramc.levels.hooks.mmolib;

import gg.auroramc.aurora.api.message.Placeholder;
import gg.auroramc.aurora.api.reward.NumberReward;
import gg.auroramc.aurora.api.util.NamespacedId;
import gg.auroramc.levels.AuroraLevels;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.stat.StatMap;
import io.lumine.mythic.lib.api.stat.modifier.StatModifier;
import io.lumine.mythic.lib.player.modifier.ModifierType;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MMOStatReward extends NumberReward {
    private static final String MMO_STAT = "aurora_levels";
    private String stat;
    private ModifierType modifierType;
    private boolean valid = true;

    public boolean isValid() {
        return valid;
    }

    public String getStat() {
        return stat;
    }

    public ModifierType getModifierType() {
        return modifierType;
    }

    @Override
    public void execute(Player player, long level, List<Placeholder<?>> placeholders) {
        if (!valid) return;

        MMOPlayerData playerData = MMOPlayerData.get(player);
        StatMap statMap = playerData.getStatMap();
        String key = NamespacedId.of(MMO_STAT, stat).toString();
        StatModifier currentModifier = getCurrentModifier(key, statMap);
        double value = getValue(placeholders);

        if (currentModifier != null) {
            value += currentModifier.getValue();
            currentModifier.unregister(playerData);
        }

        new StatModifier(key, stat, value, modifierType).register(playerData);
        Bukkit.getGlobalRegionScheduler().runDelayed(
                Bukkit.getPluginManager().getPlugin("AuroraLevels"),
                task -> statMap.getInstance(stat).update(),
                3);
    }

    @Override
    public void init(ConfigurationSection args) {
        super.init(args);
        stat = args.getString("stat");
        if (stat == null || stat.isBlank()) {
            valid = false;
            AuroraLevels.logger().warning("Stat is not defined in MMOStatReward");
            return;
        }
        try {
            modifierType = ModifierType.valueOf(args.getString("modifier", "FLAT").toUpperCase());
        } catch (IllegalArgumentException e) {
            valid = false;
            AuroraLevels.logger().warning("Invalid modifier type in MMOStatReward: " + args.getString("modifier"));
        }
    }

    @Nullable
    public StatModifier getCurrentModifier(String key, StatMap statMap) {
        for (StatModifier modifier : statMap.getInstance(stat).getModifiers()) {
            if (modifier.getKey().equals(key)) {
                return modifier;
            }
        }
        return null;
    }
}
