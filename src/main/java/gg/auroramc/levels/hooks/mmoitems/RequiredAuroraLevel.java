package gg.auroramc.levels.hooks.mmoitems;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.levels.api.data.LevelData;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.stat.type.ItemRestriction;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class RequiredAuroraLevel extends DoubleStat implements ItemRestriction {
    public RequiredAuroraLevel() {
        super("REQUIRED_AURORA_LEVEL",
                Material.EXPERIENCE_BOTTLE,
                "Required Star Level",
                new String[]{"Minimum Star Level", "to use this item."},
                new String[]{"!block", "all"});
    }

    @Override
    public boolean canUse(RPGPlayer rpgPlayer, NBTItem item, boolean message) {
        if (!item.hasTag(getNBTPath())) return true;

        Player player = rpgPlayer.getPlayer();
        if (player.hasPermission("mmoitems.bypass.requirement")) return true;

        double required = item.getStat(getId());
        if (required <= 0) return true;

        int current = getCurrentLevel(player);

        if (current < required) {
            if (message) {
                player.sendMessage("§cStar level requirement not met! Required: " + (int) required + ".");
            }
            return false;
        }
        return true;
    }

    // Player data may not be loaded yet right after join, treat that as level 0 instead of throwing.
    private int getCurrentLevel(Player player) {
        var user = AuroraAPI.getUser(player.getUniqueId());
        if (user == null) return 0;
        var data = user.getData(LevelData.class);
        return data == null ? 0 : data.getLevel();
    }
}
