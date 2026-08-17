package fr.elementa.mine;

import org.bukkit.World;
import org.bukkit.entity.Player;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.database.objects.Island;

import java.util.Optional;

public class IslandResolver {

    public static Island forPlayer(Player player) {
        World world = player.getWorld();
        
        // Cherche l'île à la position actuelle du joueur
        Optional<Island> island = BentoBox.getInstance()
                .getIslands()
                .getIslandAt(player.getLocation());

        if (island.isPresent()) {
            return island.get();
        }

        // Fallback : recherche par propriétaire si le joueur n'est pas sur son île
        island = BentoBox.getInstance()
                .getIslands()
                .getIsland(world, player.getUniqueId());

        return island.orElse(null);
    }

    public static Island getIslandOf(Player player, World world) {
        return forPlayer(player);
    }
}
