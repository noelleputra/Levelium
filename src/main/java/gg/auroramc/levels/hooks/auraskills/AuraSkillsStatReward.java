package gg.auroramc.levels.hooks.auraskills;

import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.registry.NamespacedId;
import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.api.stat.StatModifier;
import gg.auroramc.aurora.api.message.Placeholder;
import gg.auroramc.aurora.api.reward.NumberReward;
import gg.auroramc.levels.AuroraLevels;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.List;

public class AuraSkillsStatReward extends NumberReward {
    @Getter
    private static final String AURA_SKILLS_STAT = AuraSkillsHook.PREFIX;
    @Getter
    private Stat stat;

    @Override
    public void execute(Player player, long level, List<Placeholder<?>> formulaPlaceholders) {
        if (stat == null) return;

        var modId = AURA_SKILLS_STAT + stat.getId();
        double value = getValue(formulaPlaceholders);
        var user = AuraSkillsApi.get().getUser(player.getUniqueId());
        var currentModifier = user.getStatModifier(modId);
        if (currentModifier != null) {
            value += currentModifier.value();
        }
        user.addStatModifier(new StatModifier(modId, stat, value));
    }

    @Override
    public void init(ConfigurationSection args) {
        super.init(args);
        stat = AuraSkillsApi.get().getGlobalRegistry()
                .getStat(NamespacedId.fromDefault(args.getString("stat", "invalid_stat")));
        if (stat == null) {
            AuroraLevels.logger().warning("Invalid stat in AuraSkillsStatReward: " + args.getString("stat"));
        }
    }

    @Override
    public String getDisplay(Player player, List<Placeholder<?>> formulaPlaceholders) {
        var display = super.getDisplay(player, formulaPlaceholders);
        if (stat == null) return display;

        var user = AuraSkillsApi.get().getUser(player.getUniqueId());
        return Placeholder.execute(display,
                Placeholder.of("{symbol}", stat.getSymbol(user.getLocale())),
                Placeholder.of("{stat}", stat.getDisplayName(user.getLocale())));
    }
}
