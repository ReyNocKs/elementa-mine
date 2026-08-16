package fr.elementa.mine;

import org.bukkit.Location;
import org.bukkit.World;
import world.bentobox.bentobox.api.events.island.IslandEvent;
import world.bentobox.bentobox.database.objects.Island;

public class IslandListener implements org.bukkit.event.Listener {

    private final ElementaMine plugin;
    private final MineManager mineManager;

    // Bande de hauteur reservee a la mine (zone habituellement vide sous une ile BSkyBlock)
    private static final int MINE_MIN_Y = 5;
    private static final int MINE_MAX_Y = 40;

    public IslandListener(ElementaMine plugin, MineManager mineManager) {
        this.plugin = plugin;
        this.mineManager = mineManager;
    }

    @org.bukkit.event.EventHandler
    public void onIslandCreated(IslandEvent.IslandCreatedEvent event) {
        createMineForIsland(event.getIsland());
    }

    public static String regionNameFor(Island island) {
        return "team_" + island.getUniqueId();
    }

    private void createMineForIsland(Island island) {
        World world = island.getWorld();
        if (world == null || island.getCenter() == null) return;

        String regionName = regionNameFor(island);

        int centerX = island.getCenter().getBlockX();
        int centerZ = island.getCenter().getBlockZ();
        int range = island.getProtectionRange();

        Location pos1 = new Location(world, centerX - range, MINE_MIN_Y, centerZ - range);
        Location pos2 = new Location(world, centerX + range, MINE_MAX_Y, centerZ + range);

        MineRegion region = mineManager.getOrCreate(regionName);
        region.setPos1(pos1);
        region.setPos2(pos2);
        region.setTier(1);

        MineTiers.TierData tierData = MineTiers.get(1);
        region.setResetThreshold(tierData.resetThreshold);
        region.setComposition(tierData.composition);

        mineManager.generate(region);
        mineManager.saveAll();

        plugin.getLogger().info("Mine T1 (" + tierData.name + ") generee automatiquement pour l'ile " + island.getUniqueId());
    }
}
