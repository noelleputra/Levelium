package gg.auroramc.levels.hooks.mmoitems;

import gg.auroramc.levels.hooks.Hook;
import gg.auroramc.levels.AuroraLevels;
import net.Indyuce.mmoitems.MMOItems;

public class MMOItemsHook implements Hook<AuroraLevels> {
    @Override
    public void hook(AuroraLevels plugin) {
        try {
            MMOItems.plugin.getStats().register(new RequiredAuroraLevel());
            AuroraLevels.logger().info("Registered the MMOItems REQUIRED_AURORA_LEVEL stat.");
        } catch (Throwable t) {
            // Never let a MMOItems API change/timing issue take the whole plugin down
            AuroraLevels.logger().severe("Failed to register the MMOItems REQUIRED_AURORA_LEVEL stat: " + t);
            t.printStackTrace();
        }
    }
}
