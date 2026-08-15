package fr.elementa.mine;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Random;

public class MineManager {

    private final ElementaMine plugin;
    private final Map<String, MineRegion> regions = new HashMap<>();
    private final Random random = new Random();

    // Nombre de blocs poses par tick pendant une generation, pour eviter de lag le serveur
    private static final int BLOCKS_PER_TICK = 2000;

    public MineManager(ElementaMine plugin) {
        this.plugin = plugin;
        loadAll();
    }

    public int getRegionCount() {
        return regions.size();
    }

    public MineRegion getOrCreate(String name) {
        return regions.computeIfAbsent(name, MineRegion::new);
    }

    public MineRegion get(String name) {
        return regions.get(name);
    }

    public Map<String, MineRegion> getAll() {
        return regions;
    }

    /**
     * Trouve la region (s'il y en a une) qui contient cette location.
     */
    public MineRegion findRegionAt(Location loc) {
        for (MineRegion region : regions.values()) {
            if (region.contains(loc)) {
                return region;
            }
        }
        return null;
    }

    /**
     * Remplit entierement la zone selon sa composition de blocs, etalé sur plusieurs ticks.
     */
    public void generate(MineRegion region) {
        if (!region.isFullyDefined()) return;
        if (region.getComposition().isEmpty()) return;

        World world = region.getWorld();
        Location p1 = region.getPos1();
        Location p2 = region.getPos2();

        int minX = Math.min(p1.getBlockX(), p2.getBlockX());
        int maxX = Math.max(p1.getBlockX(), p2.getBlockX());
        int minY = Math.min(p1.getBlockY(), p2.getBlockY());
        int maxY = Math.max(p1.getBlockY(), p2.getBlockY());
        int minZ = Math.min(p1.getBlockZ(), p2.getBlockZ());
        int maxZ = Math.max(p1.getBlockZ(), p2.getBlockZ());

        Queue<int[]> coords = new ArrayDeque<>();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    coords.add(new int[]{x, y, z});
                }
            }
        }

        Material[] weighted = buildWeightedPool(region.getComposition());
        if (weighted.length == 0) return;

        region.resetBrokenCount();

        new BukkitRunnable() {
            @Override
            public void run() {
                int placed = 0;
                while (placed < BLOCKS_PER_TICK && !coords.isEmpty()) {
                    int[] c = coords.poll();
                    Material mat = weighted[random.nextInt(weighted.length)];
                    world.getBlockAt(c[0], c[1], c[2]).setType(mat, false);
                    placed++;
                }
                if (coords.isEmpty()) {
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /**
     * Construit un pool pondere : chaque materiau apparait "pourcentage" fois dans un tableau de 100 entrees.
     */
    private Material[] buildWeightedPool(Map<Material, Integer> composition) {
        Material[] pool = new Material[100];
        int index = 0;
        for (Map.Entry<Material, Integer> entry : composition.entrySet()) {
            int count = entry.getValue();
            for (int i = 0; i < count && index < 100; i++) {
                pool[index++] = entry.getKey();
            }
        }
        if (index == 0) return new Material[0];
        // Si le total ne fait pas exactement 100, on tronque le tableau a ce qui a ete rempli
        if (index < 100) {
            Material[] trimmed = new Material[index];
            System.arraycopy(pool, 0, trimmed, 0, index);
            return trimmed;
        }
        return pool;
    }

    public Material pickRandomBlock(MineRegion region) {
        Material[] weighted = buildWeightedPool(region.getComposition());
        if (weighted.length == 0) return Material.STONE;
        return weighted[random.nextInt(weighted.length)];
    }

    // ---------- Persistance (config.yml) ----------

    public void loadAll() {
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection regionsSection = config.getConfigurationSection("regions");
        if (regionsSection == null) return;

        for (String name : regionsSection.getKeys(false)) {
            ConfigurationSection sec = regionsSection.getConfigurationSection(name);
            if (sec == null) continue;

            MineRegion region = new MineRegion(name);

            String worldName = sec.getString("world");
            if (worldName != null) {
                World world = Bukkit.getWorld(worldName);
                if (world != null) {
                    if (sec.contains("pos1")) {
                        region.setPos1(new Location(world,
                                sec.getDouble("pos1.x"), sec.getDouble("pos1.y"), sec.getDouble("pos1.z")));
                    }
                    if (sec.contains("pos2")) {
                        region.setPos2(new Location(world,
                                sec.getDouble("pos2.x"), sec.getDouble("pos2.y"), sec.getDouble("pos2.z")));
                    }
                }
            }

            region.setResetThreshold(sec.getInt("reset-threshold", 500));
            region.resetBrokenCount();

            Map<Material, Integer> composition = new HashMap<>();
            ConfigurationSection compSection = sec.getConfigurationSection("composition");
            if (compSection != null) {
                for (String matName : compSection.getKeys(false)) {
                    Material mat = Material.matchMaterial(matName);
                    if (mat != null) {
                        composition.put(mat, compSection.getInt(matName));
                    }
                }
            }
            region.setComposition(composition);

            regions.put(name, region);
        }
    }

    public void saveAll() {
        FileConfiguration config = plugin.getConfig();

        for (Map.Entry<String, MineRegion> entry : regions.entrySet()) {
            String name = entry.getKey();
            MineRegion region = entry.getValue();
            String base = "regions." + name + ".";

            if (region.getWorld() != null) {
                config.set(base + "world", region.getWorld().getName());
            }
            if (region.getPos1() != null) {
                config.set(base + "pos1.x", region.getPos1().getX());
                config.set(base + "pos1.y", region.getPos1().getY());
                config.set(base + "pos1.z", region.getPos1().getZ());
            }
            if (region.getPos2() != null) {
                config.set(base + "pos2.x", region.getPos2().getX());
                config.set(base + "pos2.y", region.getPos2().getY());
                config.set(base + "pos2.z", region.getPos2().getZ());
            }
            config.set(base + "reset-threshold", region.getResetThreshold());

            for (Map.Entry<Material, Integer> comp : region.getComposition().entrySet()) {
                config.set(base + "composition." + comp.getKey().name(), comp.getValue());
            }
        }

        plugin.saveConfig();
    }
}
