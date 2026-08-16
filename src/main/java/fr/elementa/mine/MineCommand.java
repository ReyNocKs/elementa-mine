package fr.elementa.mine;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MineCommand implements CommandExecutor, TabCompleter {

    private final ElementaMine plugin;
    private final MineManager mineManager;
    private final BagManager bagManager;

    private static final List<String> SUBCOMMANDS = Arrays.asList(
            "setpos1", "setpos2", "addblock", "create", "reset", "info", "list", "save", "delete", "price", "tier"
    );

    public MineCommand(ElementaMine plugin, MineManager mineManager, BagManager bagManager) {
        this.plugin = plugin;
        this.mineManager = mineManager;
        this.bagManager = bagManager;
    }

    private void msg(CommandSender sender, String text) {
        sender.sendMessage(ChatColor.GOLD + "[Elementa] " + ChatColor.RESET + text);
    }

    private org.bukkit.Location blockAlignedLocation(Player player) {
        org.bukkit.Location loc = player.getLocation();
        return new org.bukkit.Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            msg(sender, "Usage: /mine <setpos1|setpos2|addblock|create|reset|info|list|save> <nom>");
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "setpos1":
            case "setpos2": {
                if (!(sender instanceof Player player)) {
                    msg(sender, "Seul un joueur en jeu peut utiliser cette commande.");
                    return true;
                }
                if (args.length < 2) {
                    msg(sender, "Usage: /mine " + sub + " <nom>");
                    return true;
                }
                MineRegion region = mineManager.getOrCreate(args[1]);
                if (sub.equals("setpos1")) {
                    region.setPos1(blockAlignedLocation(player));
                    msg(sender, "Position 1 de la zone \"" + args[1] + "\" definie a ta position.");
                } else {
                    region.setPos2(blockAlignedLocation(player));
                    msg(sender, "Position 2 de la zone \"" + args[1] + "\" definie a ta position.");
                }
                mineManager.saveAll();
                return true;
            }

            case "addblock": {
                if (args.length < 4) {
                    msg(sender, "Usage: /mine addblock <nom> <materiau> <pourcentage>");
                    return true;
                }
                MineRegion region = mineManager.get(args[1]);
                if (region == null) {
                    msg(sender, "Zone inconnue: " + args[1]);
                    return true;
                }
                Material mat = Material.matchMaterial(args[2]);
                if (mat == null) {
                    msg(sender, "Materiau inconnu: " + args[2] + " (utilise le nom Minecraft, ex: STONE, IRON_ORE)");
                    return true;
                }
                int percent;
                try {
                    percent = Integer.parseInt(args[3]);
                } catch (NumberFormatException e) {
                    msg(sender, "Le pourcentage doit etre un nombre entier.");
                    return true;
                }
                region.getComposition().put(mat, percent);
                int total = region.getComposition().values().stream().mapToInt(Integer::intValue).sum();
                msg(sender, mat.name() + " ajoute a " + percent + "%. Total actuel: " + total + "% (doit faire 100%)");
                mineManager.saveAll();
                return true;
            }

            case "create": {
                if (args.length < 2) {
                    msg(sender, "Usage: /mine create <nom>");
                    return true;
                }
                MineRegion region = mineManager.get(args[1]);
                if (region == null) {
                    msg(sender, "Zone inconnue: " + args[1]);
                    return true;
                }
                if (!region.isFullyDefined()) {
                    msg(sender, "Definis d'abord pos1 et pos2 avec /mine setpos1 et setpos2.");
                    return true;
                }
                int total = region.getComposition().values().stream().mapToInt(Integer::intValue).sum();
                if (total != 100) {
                    msg(sender, ChatColor.RED + "Attention: la composition totalise " + total
                            + "% au lieu de 100%. Ajoute/ajuste des blocs avec /mine addblock avant de creer.");
                    return true;
                }
                msg(sender, "Generation de la zone \"" + args[1] + "\" en cours...");
                mineManager.generate(region);
                return true;
            }

            case "reset": {
                if (args.length < 2) {
                    msg(sender, "Usage: /mine reset <nom>");
                    return true;
                }
                MineRegion region = mineManager.get(args[1]);
                if (region == null) {
                    msg(sender, "Zone inconnue: " + args[1]);
                    return true;
                }
                msg(sender, "Reset manuel de la zone \"" + args[1] + "\"...");
                mineManager.generate(region);
                return true;
            }

            case "info": {
                if (args.length < 2) {
                    msg(sender, "Usage: /mine info <nom>");
                    return true;
                }
                MineRegion region = mineManager.get(args[1]);
                if (region == null) {
                    msg(sender, "Zone inconnue: " + args[1]);
                    return true;
                }
                msg(sender, "Zone: " + region.getName());
                msg(sender, "Definie: " + region.isFullyDefined());
                msg(sender, "Palier: " + region.getTier() + "/" + MineTiers.MAX_TIER + " (" + MineTiers.get(region.getTier()).name + ")");
                msg(sender, "Blocs casses: " + region.getBrokenCount() + " / " + region.getResetThreshold());
                StringBuilder comp = new StringBuilder();
                for (Map.Entry<Material, Integer> e : region.getComposition().entrySet()) {
                    comp.append(e.getKey().name()).append("=").append(e.getValue()).append("% ");
                }
                msg(sender, "Composition: " + (comp.isEmpty() ? "(vide)" : comp.toString()));
                return true;
            }

            case "list": {
                if (mineManager.getAll().isEmpty()) {
                    msg(sender, "Aucune zone definie.");
                    return true;
                }
                msg(sender, "Zones: " + String.join(", ", mineManager.getAll().keySet()));
                return true;
            }

            case "save": {
                mineManager.saveAll();
                msg(sender, "Configuration sauvegardee.");
                return true;
            }

            case "delete": {
                if (args.length < 2) {
                    msg(sender, "Usage: /mine delete <nom>");
                    return true;
                }
                MineRegion region = mineManager.get(args[1]);
                if (region == null) {
                    msg(sender, "Zone inconnue: " + args[1]);
                    return true;
                }
                mineManager.getAll().remove(args[1]);
                mineManager.saveAll();
                msg(sender, ChatColor.GREEN + "Zone \"" + args[1] + "\" supprimee.");
                return true;
            }

            case "price": {
                if (args.length < 3) {
                    msg(sender, "Usage: /mine price <materiau> <valeur>");
                    return true;
                }
                Material mat = Material.matchMaterial(args[1]);
                if (mat == null) {
                    msg(sender, "Materiau inconnu: " + args[1]);
                    return true;
                }
                double price;
                try {
                    price = Double.parseDouble(args[2]);
                } catch (NumberFormatException e) {
                    msg(sender, "La valeur doit etre un nombre.");
                    return true;
                }
                bagManager.setPrice(mat, price);
                bagManager.savePrices();
                msg(sender, ChatColor.GREEN + "Prix de " + mat.name() + " defini a " + price + " Eclats.");
                return true;
            }

            case "tier": {
                if (args.length < 3) {
                    msg(sender, "Usage: /mine tier <nom> <niveau 1-" + MineTiers.MAX_TIER + ">");
                    return true;
                }
                MineRegion region = mineManager.get(args[1]);
                if (region == null) {
                    msg(sender, "Zone inconnue: " + args[1]);
                    return true;
                }
                int level;
                try {
                    level = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    msg(sender, "Le niveau doit etre un nombre.");
                    return true;
                }
                if (level < 1 || level > MineTiers.MAX_TIER) {
                    msg(sender, "Le niveau doit etre entre 1 et " + MineTiers.MAX_TIER + ".");
                    return true;
                }
                MineTiers.TierData tierData = MineTiers.get(level);
                region.setTier(level);
                region.setResetThreshold(tierData.resetThreshold);
                region.setComposition(tierData.composition);
                mineManager.generate(region);
                mineManager.saveAll();
                msg(sender, ChatColor.GREEN + "Zone \"" + args[1] + "\" passee au palier " + level + " (" + tierData.name + ").");
                return true;
            }

            default:
                msg(sender, "Sous-commande inconnue. Usage: /mine <setpos1|setpos2|addblock|create|reset|info|list|save> <nom>");
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> out = new ArrayList<>();
            String partial = args[0].toLowerCase();
            for (String s : SUBCOMMANDS) {
                if (s.startsWith(partial)) out.add(s);
            }
            return out;
        }
        if (args.length == 2) {
            List<String> out = new ArrayList<>();
            String partial = args[1].toLowerCase();
            for (String name : mineManager.getAll().keySet()) {
                if (name.toLowerCase().startsWith(partial)) out.add(name);
            }
            return out;
        }
        return new ArrayList<>();
    }
}
