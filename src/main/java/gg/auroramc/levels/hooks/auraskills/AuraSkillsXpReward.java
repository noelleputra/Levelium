package gg.auroramc.levels.hooks.auraskills;

import com.google.common.collect.Lists;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.registry.NamespacedId;
import dev.aurelium.auraskills.api.skill.Skill;
import gg.auroramc.aurora.api.message.Placeholder;
import gg.auroramc.aurora.api.reward.NumberReward;
import gg.auroramc.levels.AuroraLevels;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.List;

public class AuraSkillsXpReward extends NumberReward {
    private Skill skill;

    @Override
    public void execute(Player player, long level, List<Placeholder<?>> placeholders) {
        if (skill == null) return;

        var user = AuraSkillsApi.get().getUser(player.getUniqueId());
        List<Placeholder<?>> compiled = Lists.asList(
                Placeholder.of("{skill_level}", user.getSkillLevel(skill)),
                placeholders.toArray(new Placeholder[0]));
        user.addSkillXp(skill, getValue(compiled));
    }

    @Override
    public void init(ConfigurationSection args) {
        super.init(args);
        skill = AuraSkillsApi.get().getGlobalRegistry()
                .getSkill(NamespacedId.fromDefault(args.getString("skill", "invalid_skill")));
        if (skill == null) {
            AuroraLevels.logger().warning("Invalid skill in AuraSkillsXpReward: " + args.getString("skill"));
        }
    }

    @Override
    public String getDisplay(Player player, List<Placeholder<?>> placeholders) {
        var display = super.getDisplay(player, placeholders);
        if (skill == null) return display;

        var user = AuraSkillsApi.get().getUser(player.getUniqueId());
        display = Placeholder.execute(display,
                Placeholder.of("{skill_level}", user.getSkillLevel(skill)));
        return Placeholder.execute(display,
                Placeholder.of("{skill}", skill.getDisplayName(user.getLocale())));
    }
}
