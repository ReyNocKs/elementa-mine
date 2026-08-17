package fr.elementa.mine;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.Location;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

public class GuiListener implements Listener {

    private final ElementaMine plugin;
    private final BagManager bagManager;

    public GuiListener(ElementaMine plugin, BagManager bagManager) {
        this.plugin = plugin;
        this.bagManager = bagManager;
    }

    private void msg(Player player, String text) {
        player.sendMessage(ChatColor.GOLD + "[Elementa] " + ChatColor.RESET + text);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();

        if (holder instanceof MineUpgradeHolder) {
            event.setCancelled(true);
            if (!(event.getWhoClicked() instanceof Player player) || event.getRawSlot() != MineUpgradeGui.SLOT) return;
            world.bentobox.bentobox.database.objects.Island island = IslandResolver.forPlayer(player);
            MineRegion mine = island == null ? null : plugin.mines().get(island.getUniqueId().toString());
            if (mine == null || mine.getTier() >= MineTiers.MAX_TIER) { player.sendMessage(plugin.msg("max-tier")); return; }
            if (!bagManager.isEconomyReady()) { player.sendMessage("Economie indisponible."); return; }
            Economy economy = bagManager.getEconomy();
            int price = MineTiers.price(plugin, mine.getTier() + 1);
            if (economy.getBalance(player) < price) { player.sendMessage(plugin.msg("not-enough-money")); return; }
            economy.withdrawPlayer(player, price);
            mine.setTier(mine.getTier() + 1);
            int size = MineTiers.size(plugin, mine.getTier()), radius = size / 2;
            Location center = mine.center();
            mine.setPos1(new Location(center.getWorld(), center.getBlockX() - radius, center.getBlockY() - radius, center.getBlockZ() - radius));
            mine.setPos2(new Location(center.getWorld(), center.getBlockX() + radius, center.getBlockY() + radius, center.getBlockZ() + radius));
            mine.setComposition(MineTiers.composition(plugin));
            plugin.mines().generate(mine);
            plugin.mines().saveAll();
            player.sendMessage(plugin.msg("upgrade-success").replace("{tier}", "" + mine.getTier()));
            player.closeInventory();
            return;
        }
        if (holder instanceof BagInventoryHolder) {
            event.setCancelled(true);
            handleBagClick(event);
        } else if (holder instanceof UpgradeInventoryHolder) {
            event.setCancelled(true);
            handleUpgradeClick(event);
        }
    }

    private void handleBagClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        int slot = event.getRawSlot();

        if (slot == BagGui.SLOT_SELL) {
            if (!bagManager.isEconomyReady()) {
                msg(player, ChatColor.RED + "Le systeme d'economie n'est pas disponible.");
                return;
            }
            double earned = bagManager.sellAll(player);
            if (earned <= 0) {
                PlayerBag bag = bagManager.getBag(player.getUniqueId());
                if (bag.getContents().isEmpty()) {
                    msg(player, "Ton sac est vide, rien a vendre.");
                } else {
                    msg(player, ChatColor.RED + "Aucun prix n'est configure pour ces items. Un admin doit utiliser /mine price.");
                }
            } else {
                Economy econ = bagManager.getEconomy();
                msg(player, ChatColor.GREEN + "Vendu pour " + econ.format(earned) + " !");
            }
            player.openInventory(BagGui.build(bagManager, bagManager.getBag(player.getUniqueId())));
        } else if (slot == BagGui.SLOT_UPGRADES) {
            player.openInventory(UpgradeGui.build(bagManager, bagManager.getBag(player.getUniqueId())));
        }
    }

    private void handleUpgradeClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        int slot = event.getRawSlot();

        if (slot == UpgradeGui.SLOT_BACK) {
            player.openInventory(BagGui.build(bagManager, bagManager.getBag(player.getUniqueId())));
            return;
        }

        if (!bagManager.isEconomyReady()) {
            msg(player, ChatColor.RED + "Le systeme d'economie n'est pas disponible.");
            return;
        }

        PlayerBag bag = bagManager.getBag(player.getUniqueId());
        Economy econ = bagManager.getEconomy();

        if (slot == UpgradeGui.SLOT_CAPACITY) {
            int cost = bagManager.getCapacityUpgradeCost(bag);
            if (econ.getBalance(player) < cost) {
                msg(player, ChatColor.RED + "Il te faut " + econ.format(cost) + " pour cette amelioration.");
                return;
            }
            econ.withdrawPlayer(player, cost);
            bag.setCapacityLevel(bag.getCapacityLevel() + 1);
            msg(player, ChatColor.GREEN + "Capacite augmentee ! Nouvelle capacite: " + bag.getMaxCapacity());

        } else if (slot == UpgradeGui.SLOT_MULTIPLIER) {
            int cost = bagManager.getMultiplierUpgradeCost(bag);
            if (econ.getBalance(player) < cost) {
                msg(player, ChatColor.RED + "Il te faut " + econ.format(cost) + " pour cette amelioration.");
                return;
            }
            econ.withdrawPlayer(player, cost);
            bag.setMultiplierLevel(bag.getMultiplierLevel() + 1);
            msg(player, ChatColor.GREEN + "Multiplicateur augmente ! x" + String.format("%.2f", bag.getMultiplier()));

        } else if (slot == UpgradeGui.SLOT_AUTOSELL) {
            if (!bag.isAutosellUnlocked()) {
                int cost = bagManager.getAutosellUnlockCost();
                if (econ.getBalance(player) < cost) {
                    msg(player, ChatColor.RED + "Il te faut " + econ.format(cost) + " pour debloquer la vente auto.");
                    return;
                }
                econ.withdrawPlayer(player, cost);
                bag.setAutosellUnlocked(true);
                msg(player, ChatColor.GREEN + "Vente auto debloquee !");
            } else {
                bag.setAutosellEnabled(!bag.isAutosellEnabled());
                msg(player, "Vente auto : " + (bag.isAutosellEnabled() ? ChatColor.GREEN + "ACTIVEE" : ChatColor.RED + "DESACTIVEE"));
            }
        } else {
            return;
        }

        player.openInventory(UpgradeGui.build(bagManager, bag));
    }
}
