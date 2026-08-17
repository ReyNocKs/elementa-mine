package fr.elementa.mine;

import org.bukkit.World;
import org.bukkit.entity.Player;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.database.objects.Island;

import java.util.Optional;

public class IslandResolver {

    public static Island getIslandOf(Player player, World world) {
        Optional<Island> island = BentoBox.getInstance()
                .getIslands()
                .getIslandAt(player.getLocation());

        if (island.isPresent()) {
            return island.get();
        }

        return BentoBox.getInstance()
                .getIslands()
                .getIsland(world, player.getUniqueId());
    }
}
