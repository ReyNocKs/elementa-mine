package fr.elementa.mine;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.LinkedHashMap;
import java.util.Map;

public class MineRegion {

    private final String name;
    private World world;
    private Location pos1;
    private Location pos2;
    private int brokenCount = 0;
    private int resetThreshold = 500;
    private int tier = 1;
    // Materiau -> pourcentage (les pourcentages doivent totaliser 100)
    private final Map<Material, Integer> composition = new LinkedHashMap<>();

    public MineRegion(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public Location getPos1() {
        return pos1;
    }

    public void setPos1(Location pos1) {
        this.pos1 = pos1;
        this.world = pos1.getWorld();
    }

    public Location getPos2() {
        return pos2;
    }

    public void setPos2(Location pos2) {
        this.pos2 = pos2;
        this.world = pos2.getWorld();
    }

    public boolean isFullyDefined() {
        return pos1 != null && pos2 != null && world != null;
    }

    public int getBrokenCount() {
        return brokenCount;
    }

    public void incrementBroken() {
        brokenCount++;
    }

    public void resetBrokenCount() {
        brokenCount = 0;
    }

    public int getResetThreshold() {
        return resetThreshold;
    }

    public void setResetThreshold(int resetThreshold) {
        this.resetThreshold = resetThreshold;
    }

    public int getTier() {
        return tier;
    }

    public void setTier(int tier) {
        this.tier = tier;
    }

    public Map<Material, Integer> getComposition() {
        return composition;
    }

    public void setComposition(Map<Material, Integer> newComposition) {
        composition.clear();
        composition.putAll(newComposition);
    }

    /**
     * Verifie si une location donnee est a l'interieur de la zone (bornes incluses).
     */
    public boolean contains(Location loc) {
        if (!isFullyDefined() || loc.getWorld() == null) return false;
        if (!loc.getWorld().equals(world)) return false;

        double minX = Math.min(pos1.getX(), pos2.getX());
        double maxX = Math.max(pos1.getX(), pos2.getX());
        double minY = Math.min(pos1.getY(), pos2.getY());
        double maxY = Math.max(pos1.getY(), pos2.getY());
        double minZ = Math.min(pos1.getZ(), pos2.getZ());
        double maxZ = Math.max(pos1.getZ(), pos2.getZ());

        return loc.getX() >= minX && loc.getX() <= maxX
                && loc.getY() >= minY && loc.getY() <= maxY
                && loc.getZ() >= minZ && loc.getZ() <= maxZ;
    }
}
