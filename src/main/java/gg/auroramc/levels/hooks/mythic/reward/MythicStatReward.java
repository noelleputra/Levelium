package gg.auroramc.levels.hooks.mythic.reward;

import gg.auroramc.aurora.api.message.Placeholder;
import gg.auroramc.aurora.api.reward.NumberReward;
import gg.auroramc.levels.AuroraLevels;
import gg.auroramc.levels.hooks.mythic.MythicHook;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.skills.stats.*;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.List;

public class MythicStatReward extends NumberReward {
    @Getter
    private static final StatSource source = MythicHook.SOURCE;
    private StatType statType;
    private StatModifierType modifierType;
    private boolean valid = true;

    public boolean isValid() {
        return valid;
    }

    public StatType getStatType() {
        return statType;
    }

    public StatModifierType getModifierType() {
        return modifierType;
    }

    @Override
    public void execute(Player player, long level, List<Placeholder<?>> placeholders) {
        if (!valid) return;

        StatRegistry registry = MythicBukkit.inst().getPlayerManager().getProfile(player).getStatRegistry();
        if (registry.getStatData(statType).isPresent()) {
            StatRegistry.StatMap data = registry.getStatData(statType).get();
            double current = switch (modifierType) {
                case ADDITIVE -> data.getAdditives().getOrDefault(source, 0.0);
                case ADDITIVE_MULTIPLIER -> data.getAdditiveMultipliers().getOrDefault(source, 0.0);
                case COMPOUND_MULTIPLIER -> data.getCompoundMultipliers().getOrDefault(source, 0.0);
                case SETTER -> data.getSetters().getOrDefault(source, 0.0);
            };
            registry.putValue(statType, source, modifierType, current + getValue(placeholders));
        } else {
            registry.putValue(statType, source, modifierType, getValue(placeholders));
        }
    }

    @Override
    public void init(ConfigurationSection args) {
        super.init(args);
        String stat = args.getString("stat");
        if (stat == null || stat.isBlank()) {
            valid = false;
            AuroraLevels.logger().warning("Stat is not defined in MythicStatReward");
            return;
        }

        statType = MythicBukkit.inst().getStatManager().getStats().get(stat);
        if (statType == null || !statType.isEnabled()) {
            valid = false;
            AuroraLevels.logger().warning("Invalid or disabled stat: " + stat + " in MythicStatReward");
            return;
        }

        String modifier = args.getString("modifier", "additive");
        try {
            modifierType = StatModifierType.valueOf(modifier.toUpperCase());
        } catch (IllegalArgumentException e) {
            valid = false;
            AuroraLevels.logger().warning("Invalid modifier type: " + modifier + " in MythicStatReward");
        }
    }

    @Override
    public String getDisplay(Player player, List<Placeholder<?>> placeholders) {
        String display = super.getDisplay(player, placeholders);
        if (!valid) return display;

        double value = getValue(placeholders);
        return Placeholder.execute(display,
                Placeholder.of("{stat}", statType.getDisplay()),
                Placeholder.of("{stat_display}", statType.getFormattedValue(modifierType, value)));
    }
}
