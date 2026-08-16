package fr.elementa.mine;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class UpgradeGui {

    public static final String TITLE = ChatColor.AQUA + "Ameliorations du sac";

    public static final int SLOT_CAPACITY = 20;
    public static final int SLOT_MULTIPLIER = 22;
    public static final int SLOT_AUTOSELL = 24;
    public static final int SLOT_BACK = 49;

    public static Inventory build(BagManager bagManager, PlayerBag bag) {
        UpgradeInventoryHolder holder = new UpgradeInventoryHolder();
        Inventory inv = org.bukkit.Bukkit.createInventory(holder, 54, TITLE);
        holder.setInventory(inv);

        // Capacite
        ItemStack capacityItem = new ItemStack(Material.CHEST);
        ItemMeta capMeta = capacityItem.getItemMeta();
        if (capMeta != null) {
            capMeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Capacite du sac");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Niveau actuel: " + ChatColor.WHITE + bag.getCapacityLevel());
            lore.add(ChatColor.GRAY + "Capacite actuelle: " + ChatColor.WHITE + bag.getMaxCapacity());
            lore.add(ChatColor.GRAY + "Cout du prochain niveau: " + ChatColor.GREEN
                    + bagManager.getCapacityUpgradeCost(bag) + " Eclats");
            lore.add("");
            lore.add(ChatColor.YELLOW + "Clique pour ameliorer");
            capMeta.setLore(lore);
            capacityItem.setItemMeta(capMeta);
        }
        inv.setItem(SLOT_CAPACITY, capacityItem);

        // Multiplicateur
        ItemStack multItem = new ItemStack(Material.EMERALD);
        ItemMeta multMeta = multItem.getItemMeta();
        if (multMeta != null) {
            multMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Multiplicateur de vente");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Niveau actuel: " + ChatColor.WHITE + bag.getMultiplierLevel());
            lore.add(ChatColor.GRAY + "Multiplicateur actuel: " + ChatColor.WHITE + "x" + String.format("%.2f", bag.getMultiplier()));
            lore.add(ChatColor.GRAY + "Cout du prochain niveau: " + ChatColor.GREEN
                    + bagManager.getMultiplierUpgradeCost(bag) + " Eclats");
            lore.add("");
            lore.add(ChatColor.YELLOW + "Clique pour ameliorer");
            multMeta.setLore(lore);
            multItem.setItemMeta(multMeta);
        }
        inv.setItem(SLOT_MULTIPLIER, multItem);

        // Vente auto
        ItemStack autosellItem = new ItemStack(bag.isAutosellUnlocked() ? Material.HOPPER : Material.REDSTONE);
        ItemMeta autoMeta = autosellItem.getItemMeta();
        if (autoMeta != null) {
            autoMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Vente automatique");
            List<String> lore = new ArrayList<>();
            if (bag.isAutosellUnlocked()) {
                lore.add(ChatColor.GRAY + "Statut: " + (bag.isAutosellEnabled()
                        ? ChatColor.GREEN + "ACTIVEE" : ChatColor.RED + "DESACTIVEE"));
                lore.add("");
                lore.add(ChatColor.YELLOW + "Clique pour activer/desactiver");
            } else {
                lore.add(ChatColor.GRAY + "Non debloquee");
                lore.add(ChatColor.GRAY + "Cout: " + ChatColor.GREEN + bagManager.getAutosellUnlockCost() + " Eclats");
                lore.add("");
                lore.add(ChatColor.YELLOW + "Clique pour debloquer");
            }
            autoMeta.setLore(lore);
            autosellItem.setItemMeta(autoMeta);
        }
        inv.setItem(SLOT_AUTOSELL, autosellItem);

        // Retour
        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Retour au sac");
            backItem.setItemMeta(backMeta);
        }
        inv.setItem(SLOT_BACK, backItem);

        return inv;
    }
}
