package gg.auroramc.levels.hooks.mmolib;

import gg.auroramc.levels.hooks.Hook;
import gg.auroramc.aurora.api.util.NamespacedId;
import gg.auroramc.levels.AuroraLevels;

public class MMOLibHook implements Hook<AuroraLevels> {
    // Levels' stat modifiers in MythicLib, kept apart from other plugins'
    public static final String KEY = "aurora_levels";
    @Override
    public void hook(AuroraLevels plugin) {
        plugin.getLeveler().getRewardFactory()
                .registerRewardType(NamespacedId.fromDefault("mmo_stat"), MMOStatReward.class);

        plugin.getLeveler().getRewardAutoCorrector()
                .registerCorrector(NamespacedId.fromDefault("mmo_stat"), new MMOStatCorrector(plugin));

        AuroraLevels.logger().info("Hooked into MythicLib (MMOLib) for stat rewards with reward type: 'mmo_stat'. Auto reward corrector for stats is registered.");
    }
}
