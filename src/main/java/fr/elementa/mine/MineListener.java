package fr.elementa.mine;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class MineListener implements Listener {

    private final ElementaMine plugin;
    private final MineManager mineManager;
    private final BagManager bagManager;

    public MineListener(ElementaMine plugin, MineManager mineManager, BagManager bagManager) {
        this.plugin = plugin;
        this.mineManager = mineManager;
        this.bagManager = bagManager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        MineRegion region = mineManager.findRegionAt(event.getBlock().getLocation());
        if (region == null) return;

        Player player = event.getPlayer();
        Material broken = event.getBlock().getType();

        // On empeche le drop vanilla : le bloc va dans le sac du joueur a la place
        event.setDropItems(false); event.getBlock().setType(Material.AIR, false);

        PlayerBag bag = bagManager.getBag(player.getUniqueId());
        int leftover = bag.addItem(broken, 1);
        if (leftover > 0) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.bag-full", "&cSac plein")));
        }

        if (bag.isAutosellUnlocked() && bag.isAutosellEnabled() && bagManager.isEconomyReady()) {
            double earned = bagManager.sellAll(player);
            if (earned > 0) {
                Economy econ = bagManager.getEconomy();
                player.sendMessage(ChatColor.GOLD + "[Elementa] " + ChatColor.GREEN
                        + "+" + econ.format(earned) + " (vente auto)");
            }
        }

        region.incrementBroken();

        if (region.getBrokenCount() >= region.getResetThreshold()) {
            player.sendMessage(ChatColor.GOLD + "[Elementa] " + ChatColor.YELLOW
                    + "La mine \"" + region.getName() + "\" se regenere...");
            mineManager.generate(region);
        }
    }
}
