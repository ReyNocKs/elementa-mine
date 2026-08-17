package fr.elementa.mine;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
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

    // ---------- Persistance : un fichier YAML par UUID d'île ----------

    private File islandDataDir() {
        File dir = new File(plugin.getDataFolder(), "data/islands");
        if (!dir.exists() && !dir.mkdirs()) plugin.getLogger().warning("Impossible de creer " + dir);
        return dir;
    }

    public void loadAll() {
        plugin.reloadConfig();
        File dir = islandDataDir();
        File[] files = dir.listFiles((d, n) -> n.endsWith(".yml"));
        if (files != null) for (File file : files) loadFile(file);

        // Migration unique des anciennes donnees config.yml.regions vers data/islands/UUID.yml.
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection legacy = config.getConfigurationSection("regions");
        if (legacy != null) {
            for (String id : legacy.getKeys(false)) {
                if (!regions.containsKey(id)) {
                    ConfigurationSection sec = legacy.getConfigurationSection(id);
                    if (sec != null) {
                        MineRegion region = readSection(id, sec);
                        regions.put(id, region);
                        saveRegion(region);
                    }
                }
            }
            config.set("regions", null);
            plugin.saveConfig();
            plugin.getLogger().info("Anciennes donnees de mines migrees vers data/islands.");
        }
    }

    private void loadFile(File file) {
        String id = file.getName().substring(0, file.getName().length() - 4);
        try { regions.put(id, readSection(id, YamlConfiguration.loadConfiguration(file))); }
        catch (IllegalArgumentException ex) { plugin.getLogger().warning("UUID d'ile invalide: " + file.getName()); }
    }

    private MineRegion readSection(String id, ConfigurationSection sec) {
        MineRegion region = new MineRegion(id);
        String worldName = sec.getString("world");
        World world = worldName == null ? null : Bukkit.getWorld(worldName);
        if (world != null) {
            if (sec.contains("pos1")) region.setPos1(new Location(world, sec.getDouble("pos1.x"), sec.getDouble("pos1.y"), sec.getDouble("pos1.z")));
            if (sec.contains("pos2")) region.setPos2(new Location(world, sec.getDouble("pos2.x"), sec.getDouble("pos2.y"), sec.getDouble("pos2.z")));
        }
        region.setTier(sec.getInt("tier", 1));
        region.setResetThreshold(plugin.getConfig().getInt("mine.reset-threshold", 500));
        // La composition est un parametre general de config.yml, pas une donnee d'ile.
        region.setComposition(MineTiers.composition(plugin));
        region.resetBrokenCount();
        return region;
    }

    public void saveAll() { for (MineRegion region : regions.values()) saveRegion(region); }

    private void saveRegion(MineRegion region) {
        File file = new File(islandDataDir(), region.getName() + ".yml");
        YamlConfiguration data = new YamlConfiguration();
        if (region.getWorld() != null) data.set("world", region.getWorld().getName());
        if (region.getPos1() != null) {
            data.set("pos1.x", region.getPos1().getX()); data.set("pos1.y", region.getPos1().getY()); data.set("pos1.z", region.getPos1().getZ());
        }
        if (region.getPos2() != null) {
            data.set("pos2.x", region.getPos2().getX()); data.set("pos2.y", region.getPos2().getY()); data.set("pos2.z", region.getPos2().getZ());
        }
        data.set("tier", region.getTier());

        try { data.save(file); } catch (IOException ex) { plugin.getLogger().severe("Impossible de sauvegarder " + file + ": " + ex.getMessage()); }
    }

}
