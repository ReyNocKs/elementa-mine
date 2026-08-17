package fr.elementa.mine;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.database.objects.Island;

import java.util.UUID;

/** Centralise la resolution exacte de l'ile BentoBox, sans recherche par monde/proximite. */
public final class IslandResolver {
    private IslandResolver() { }

    public static Island forPlayer(Player player) {
        if (player == null || player.getWorld() == null) return null;
        return BentoBox.getInstance().getIslands().getIsland(player.getWorld(), player.getUniqueId());
    }

    public static Island at(Location location) {
        if (location == null || location.getWorld() == null) return null;
        return BentoBox.getInstance().getIslands().getIslandAt(location);
    }

    public static UUID id(Island island) {
        return island == null ? null : island.getUniqueId();
    }
}
