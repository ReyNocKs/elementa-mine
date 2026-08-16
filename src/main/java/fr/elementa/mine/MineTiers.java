package fr.elementa.mine;

import org.bukkit.Material;

import java.util.LinkedHashMap;
import java.util.Map;

public class MineTiers {

    public static class TierData {
        public final String name;
        public final Map<Material, Integer> composition;
        public final int resetThreshold;

        public TierData(String name, Map<Material, Integer> composition, int resetThreshold) {
            this.name = name;
            this.composition = composition;
            this.resetThreshold = resetThreshold;
        }
    }

    public static final int MAX_TIER = 5;

    public static TierData get(int tier) {
        Map<Material, Integer> comp = new LinkedHashMap<>();

        switch (Math.max(1, Math.min(tier, MAX_TIER))) {
            case 1:
                comp.put(Material.STONE, 60);
                comp.put(Material.ANDESITE, 15);
                comp.put(Material.DIORITE, 15);
                comp.put(Material.COAL_ORE, 10);
                return new TierData("Pierre Brute", comp, 500);

            case 2:
                comp.put(Material.STONE, 45);
                comp.put(Material.IRON_ORE, 25);
                comp.put(Material.COAL_ORE, 20);
                comp.put(Material.COPPER_ORE, 10);
                return new TierData("Veine de Fer", comp, 600);

            case 3:
                comp.put(Material.IRON_ORE, 35);
                comp.put(Material.GOLD_ORE, 25);
                comp.put(Material.REDSTONE_ORE, 25);
                comp.put(Material.LAPIS_ORE, 15);
                return new TierData("Coeur Dore", comp, 700);

            case 4:
                comp.put(Material.GOLD_ORE, 30);
                comp.put(Material.DIAMOND_ORE, 20);
                comp.put(Material.EMERALD_ORE, 10);
                comp.put(Material.LAPIS_ORE, 40);
                return new TierData("Abime Precieux", comp, 800);

            case 5:
            default:
                comp.put(Material.DIAMOND_ORE, 40);
                comp.put(Material.EMERALD_ORE, 30);
                comp.put(Material.GOLD_ORE, 30);
                return new TierData("Noyau Elementaire", comp, 1000);
        }
    }
}
