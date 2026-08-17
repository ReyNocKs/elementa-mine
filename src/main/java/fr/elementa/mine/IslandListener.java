package fr.elementa.mine;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import world.bentobox.bentobox.api.events.island.IslandEvent;
import world.bentobox.bentobox.database.objects.Island;

/** Creates the default mine when BentoBox reports that an island was created. */
public final class IslandListener implements Listener {
    private static final int MINE_MIN_Y = 5;
    private static final int MINE_MAX_Y = 40;

    private final ElementaMine plugin;
    private final MineManager mineManager;

    public IslandListener(ElementaMine plugin, MineManager mineManager) {
        this.plugin = plugin;
        this.mineManager = mineManager;
    }

    @EventHandler
    public void onIslandEvent(IslandEvent event) {
        if (event == null || event.getReason() != IslandEvent.Reason.CREATED) {
            return;
        }
        try {
            Island island = event.getIsland();
            if (island != null) {
                createMineForIsland(island);
            }
        } catch (RuntimeException ex) {
            plugin.getLogger().warning("Impossible de creer la mine BentoBox: " + ex.getMessage());
        }
    }

    public static String regionNameFor(Island island) {
        return "team_" + island.getUniqueId();
    }

    private void createMineForIsland(Island island) {
        try {
            Location center = island.getCenter();
            World world = island.getWorld();
            if (center == null || world == null) {
                return;
            }

            String regionName = regionNameFor(island);
            int range = island.getProtectionRange();
            Location pos1 = new Location(world, center.getBlockX() - range, MINE_MIN_Y,
                    center.getBlockZ() - range);
            Location pos2 = new Location(world, center.getBlockX() + range, MINE_MAX_Y,
                    center.getBlockZ() + range);

            MineRegion region = mineManager.getOrCreate(regionName);
            region.setPos1(pos1);
            region.setPos2(pos2);
            region.setTier(1);

            MineTiers.TierData tierData = MineTiers.get(1);
            region.setResetThreshold(tierData.resetThreshold);
            region.setComposition(tierData.composition);
            mineManager.generate(region);
            mineManager.saveAll();

            plugin.getLogger().info("Mine T1 (" + tierData.name
                    + ") generee automatiquement pour l'ile " + island.getUniqueId());
        } catch (RuntimeException ex) {
            plugin.getLogger().warning("Erreur pendant la generation de la mine BentoBox: "
                    + ex.getMessage());
        }
    }
}
