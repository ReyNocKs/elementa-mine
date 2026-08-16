package fr.elementa.mine;

import org.bukkit.Material;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerBag {

    private final UUID owner;
    private final Map<Material, Integer> contents = new LinkedHashMap<>();

    private int capacityLevel = 0;   // 0 = capacite de base
    private int multiplierLevel = 0; // 0 = pas de bonus
    private boolean autosellUnlocked = false;
    private boolean autosellEnabled = false;

    private static final int BASE_CAPACITY = 64;
    private static final int CAPACITY_PER_LEVEL = 32;

    public PlayerBag(UUID owner) {
        this.owner = owner;
    }

    public UUID getOwner() {
        return owner;
    }

    public Map<Material, Integer> getContents() {
        return contents;
    }

    public int getMaxCapacity() {
        return BASE_CAPACITY + (capacityLevel * CAPACITY_PER_LEVEL);
    }

    public int getCurrentAmount() {
        return contents.values().stream().mapToInt(Integer::intValue).sum();
    }

    public int getFreeSpace() {
        return Math.max(0, getMaxCapacity() - getCurrentAmount());
    }

    /**
     * Ajoute des items au sac, dans la limite de la place disponible.
     * @return la quantite qui n'a pas pu etre ajoutee (0 si tout est rentre)
     */
    public int addItem(Material material, int amount) {
        int free = getFreeSpace();
        int toAdd = Math.min(free, amount);
        if (toAdd > 0) {
            contents.merge(material, toAdd, Integer::sum);
        }
        return amount - toAdd;
    }

    public void clear() {
        contents.clear();
    }

    public int getCapacityLevel() {
        return capacityLevel;
    }

    public void setCapacityLevel(int capacityLevel) {
        this.capacityLevel = capacityLevel;
    }

    public int getMultiplierLevel() {
        return multiplierLevel;
    }

    public void setMultiplierLevel(int multiplierLevel) {
        this.multiplierLevel = multiplierLevel;
    }

    public double getMultiplier() {
        return 1.0 + (multiplierLevel * 0.10); // +10% par niveau
    }

    public boolean isAutosellUnlocked() {
        return autosellUnlocked;
    }

    public void setAutosellUnlocked(boolean autosellUnlocked) {
        this.autosellUnlocked = autosellUnlocked;
    }

    public boolean isAutosellEnabled() {
        return autosellEnabled;
    }

    public void setAutosellEnabled(boolean autosellEnabled) {
        this.autosellEnabled = autosellEnabled;
    }
}
