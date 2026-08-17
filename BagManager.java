package fr.elementa.mine;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BagManager {

    private final ElementaMine plugin;
    private final Map<UUID, PlayerBag> bags = new HashMap<>();
    private final Map<Material, Double> prices = new HashMap<>();

    private Economy economy;
    private File bagsFile;

    // Couts de base des ameliorations (en Eclats)
    private int capacityCostBase = 100;
    private int capacityCostPerLevel = 50;
    private int autosellCost = 500;
    private int multiplierCostBase = 200;
    private int multiplierCostPerLevel = 100;

    public BagManager(ElementaMine plugin) {
        this.plugin = plugin;
        setupEconomy();
        loadPrices();
        loadBags();
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            plugin.getLogger().warning("Aucun plugin d'economie (Vault/VaultUnlocked) detecte ! La vente sera desactivee.");
            return false;
        }
        this.economy = rsp.getProvider();
        return true;
    }

    public boolean isEconomyReady() {
        return economy != null;
    }

    public Economy getEconomy() {
        return economy;
    }

    public PlayerBag getBag(UUID uuid) {
        return bags.computeIfAbsent(uuid, PlayerBag::new);
    }

    public double getPrice(Material material) {
        return prices.getOrDefault(material, 0.0);
    }

    public void setPrice(Material material, double price) {
        prices.put(material, price);
    }

    /**
     * Vend tout le contenu du sac d'un joueur. Retourne le montant total gagne.
     */
    public double sellAll(Player player) {
        if (economy == null) return 0;

        PlayerBag bag = getBag(player.getUniqueId());
        if (bag.getContents().isEmpty()) return 0;

        double total = 0;
        for (Map.Entry<Material, Integer> entry : bag.getContents().entrySet()) {
            double unitPrice = getPrice(entry.getKey());
            total += unitPrice * entry.getValue();
        }
        total *= bag.getMultiplier();

        if (total <= 0) {
            return 0; // aucun prix configure pour ces items, on ne vide pas le sac
        }

        bag.clear();
        economy.depositPlayer(player, total);
        return total;
    }

    // ---------- Couts des ameliorations ----------

    public int getCapacityUpgradeCost(PlayerBag bag) {
        return capacityCostBase + (bag.getCapacityLevel() * capacityCostPerLevel);
    }

    public int getAutosellUnlockCost() {
        return autosellCost;
    }

    public int getMultiplierUpgradeCost(PlayerBag bag) {
        return multiplierCostBase + (bag.getMultiplierLevel() * multiplierCostPerLevel);
    }

    // ---------- Persistance ----------

    private void loadPrices() {
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection pricesSection = config.getConfigurationSection("prices");
        if (pricesSection == null) return;

        for (String matName : pricesSection.getKeys(false)) {
            Material mat = Material.matchMaterial(matName);
            if (mat != null) {
                prices.put(mat, pricesSection.getDouble(matName));
            }
        }
    }

    public void savePrices() {
        FileConfiguration config = plugin.getConfig();
        for (Map.Entry<Material, Double> entry : prices.entrySet()) {
            config.set("prices." + entry.getKey().name(), entry.getValue());
        }
        plugin.saveConfig();
    }

    private void loadBags() {
        bagsFile = new File(plugin.getDataFolder(), "bags.yml");
        if (!bagsFile.exists()) return;

        YamlConfiguration data = YamlConfiguration.loadConfiguration(bagsFile);
        ConfigurationSection playersSection = data.getConfigurationSection("players");
        if (playersSection == null) return;

        for (String uuidStr : playersSection.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                ConfigurationSection sec = playersSection.getConfigurationSection(uuidStr);
                if (sec == null) continue;

                PlayerBag bag = new PlayerBag(uuid);
                bag.setCapacityLevel(sec.getInt("capacityLevel", 0));
                bag.setMultiplierLevel(sec.getInt("multiplierLevel", 0));
                bag.setAutosellUnlocked(sec.getBoolean("autosellUnlocked", false));
                bag.setAutosellEnabled(sec.getBoolean("autosellEnabled", false));

                ConfigurationSection contentsSec = sec.getConfigurationSection("contents");
                if (contentsSec != null) {
                    for (String matName : contentsSec.getKeys(false)) {
                        Material mat = Material.matchMaterial(matName);
                        if (mat != null) {
                            bag.addItem(mat, contentsSec.getInt(matName));
                        }
                    }
                }

                bags.put(uuid, bag);
            } catch (IllegalArgumentException ignored) {
                // UUID invalide, on saute cette entree
            }
        }
    }

    public void saveBags() {
        if (bagsFile == null) {
            bagsFile = new File(plugin.getDataFolder(), "bags.yml");
        }
        YamlConfiguration data = new YamlConfiguration();

        for (Map.Entry<UUID, PlayerBag> entry : bags.entrySet()) {
            String base = "players." + entry.getKey().toString() + ".";
            PlayerBag bag = entry.getValue();

            data.set(base + "capacityLevel", bag.getCapacityLevel());
            data.set(base + "multiplierLevel", bag.getMultiplierLevel());
            data.set(base + "autosellUnlocked", bag.isAutosellUnlocked());
            data.set(base + "autosellEnabled", bag.isAutosellEnabled());

            for (Map.Entry<Material, Integer> content : bag.getContents().entrySet()) {
                data.set(base + "contents." + content.getKey().name(), content.getValue());
            }
        }

        try {
            data.save(bagsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Impossible de sauvegarder bags.yml : " + e.getMessage());
        }
    }
}
