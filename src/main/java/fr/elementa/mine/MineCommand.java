package fr.elementa.mine;

import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import world.bentobox.bentobox.database.objects.Island;

import java.util.List;

public final class MineCommand implements CommandExecutor, TabCompleter {
    private final ElementaMine plugin;
    private final MineManager mines;
    public MineCommand(ElementaMine plugin, MineManager mines, BagManager ignored) { this.plugin = plugin; this.mines = mines; }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;
        Island island = IslandResolver.forPlayer(player);
        MineRegion mine = island == null ? null : mines.get(island.getUniqueId().toString());
        if (mine == null) { player.sendMessage(plugin.msg("no-island")); return true; }
        if (args.length > 0 && args[0].equalsIgnoreCase("upgrade")) {
            player.openInventory(MineUpgradeGui.build(plugin, mine));
            return true;
        }
        Location target = mine.center();
        target.setY(mine.getPos2().getY() + 1);
        target.setX(target.getBlockX() + .5);
        target.setZ(target.getBlockZ() + .5);
        player.teleport(target);
        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return args.length == 1 ? List.of("upgrade") : List.of();
    }
}
