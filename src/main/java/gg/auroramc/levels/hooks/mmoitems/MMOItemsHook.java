package gg.auroramc.levels.hooks.mmoitems;

import gg.auroramc.aurora.api.plugin.Hook;
import gg.auroramc.levels.AuroraLevels;
import net.Indyuce.mmoitems.MMOItems;

public class MMOItemsHook implements Hook<AuroraLevels> {
    @Override
    public void hook(AuroraLevels plugin) {
        MMOItems.plugin.getStats().register(new RequiredAuroraLevel());
        AuroraLevels.logger().info("Registered the MMOItems REQUIRED_AURORA_LEVEL stat.");
    }
}
