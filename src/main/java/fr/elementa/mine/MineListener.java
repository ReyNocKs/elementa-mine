package fr.elementa.mine;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class MineListener implements Listener {

    private final ElementaMine plugin;
    private final MineManager mineManager;

    public MineListener(ElementaMine plugin, MineManager mineManager) {
        this.plugin = plugin;
        this.mineManager = mineManager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        MineRegion region = mineManager.findRegionAt(event.getBlock().getLocation());
        if (region == null) return;

        region.incrementBroken();

        if (region.getBrokenCount() >= region.getResetThreshold()) {
            Player player = event.getPlayer();
            player.sendMessage(ChatColor.GOLD + "[Elementa] " + ChatColor.YELLOW
                    + "La mine \"" + region.getName() + "\" se regenere...");
            mineManager.generate(region);
        }
    }
}
