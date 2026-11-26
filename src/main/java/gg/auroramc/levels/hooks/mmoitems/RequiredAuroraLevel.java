package gg.auroramc.levels.hooks.mmoitems;

import gg.auroramc.levels.api.AuroraLevelsProvider;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.stat.type.ItemRestriction;
import org.bukkit.Material;

public class RequiredAuroraLevel extends DoubleStat implements ItemRestriction {
    public RequiredAuroraLevel() {
        super("REQUIRED_AURORA_LEVEL",
                Material.EXPERIENCE_BOTTLE,
                "Required Star Level",
                new String[]{"Minimum Star Level", "to use this item."},
                new String[]{"!block", "all"});
    }

    @Override
    public boolean canUse(RPGPlayer player, NBTItem item, boolean message) {
        if (!item.hasTag(getNBTPath())) return true;

        if (player.getPlayer().hasPermission("mmoitems.bypass.requirement")) return true;

        double required = item.getStat(getId());
        int current = AuroraLevelsProvider.getLeveler().getUserData(player.getPlayer()).getLevel();

        if (current < required) {
            if (message) {
                player.getPlayer().sendMessage("§cStar level requirement not met! Required: " + (int) required + ".");
            }
            return false;
        }
        return true;
    }
}
