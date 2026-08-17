package fr.elementa.mine;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BagGui {

    public static final String TITLE = ChatColor.GOLD + "Ton sac";

    // Emplacements fixes des boutons du bas
    public static final int SLOT_SELL = 45;
    public static final int SLOT_UPGRADES = 49;

    public static Inventory build(BagManager bagManager, PlayerBag bag) {
        BagInventoryHolder holder = new BagInventoryHolder();
        Inventory inv = org.bukkit.Bukkit.createInventory(holder, 54, TITLE);
        holder.setInventory(inv);

        int slot = 0;
        double totalValue = 0;

        for (Map.Entry<Material, Integer> entry : bag.getContents().entrySet()) {
            if (slot >= 45) break; // on garde la derniere rangee pour les boutons

            Material mat = entry.getKey();
            int amount = entry.getValue();
            double unitPrice = bagManager.getPrice(mat);
            double lineValue = unitPrice * amount * bag.getMultiplier();
            totalValue += lineValue;

            ItemStack item = new ItemStack(mat, Math.min(amount, 64));
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.YELLOW + mat.name());
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Quantite: " + ChatColor.WHITE + amount);
                lore.add(ChatColor.GRAY + "Prix unitaire: " + ChatColor.GREEN + String.format("%.2f", unitPrice) + " Eclats");
                lore.add(ChatColor.GRAY + "Valeur totale: " + ChatColor.GREEN + String.format("%.2f", lineValue) + " Eclats");
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            inv.setItem(slot, item);
            slot++;
        }

        // Bouton Vendre
        ItemStack sellButton = new ItemStack(Material.EMERALD);
        ItemMeta sellMeta = sellButton.getItemMeta();
        if (sellMeta != null) {
            sellMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Vendre tout");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Valeur totale du sac:");
            lore.add(ChatColor.GREEN + String.format("%.2f", totalValue) + " Eclats");
            lore.add("");
            lore.add(ChatColor.YELLOW + "Clique pour vendre");
            sellMeta.setLore(lore);
            sellButton.setItemMeta(sellMeta);
        }
        inv.setItem(SLOT_SELL, sellButton);

        // Bouton Ameliorations
        ItemStack upgradeButton = new ItemStack(Material.ANVIL);
        ItemMeta upgradeMeta = upgradeButton.getItemMeta();
        if (upgradeMeta != null) {
            upgradeMeta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "Ameliorations");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Capacite: " + ChatColor.WHITE + bag.getCurrentAmount() + "/" + bag.getMaxCapacity());
            lore.add(ChatColor.GRAY + "Multiplicateur: " + ChatColor.WHITE + "x" + String.format("%.2f", bag.getMultiplier()));
            lore.add(ChatColor.GRAY + "Vente auto: " + ChatColor.WHITE
                    + (bag.isAutosellUnlocked() ? (bag.isAutosellEnabled() ? "ON" : "OFF") : "non debloquee"));
            lore.add("");
            lore.add(ChatColor.YELLOW + "Clique pour ameliorer");
            upgradeMeta.setLore(lore);
            upgradeButton.setItemMeta(upgradeMeta);
        }
        inv.setItem(SLOT_UPGRADES, upgradeButton);

        return inv;
    }
}
