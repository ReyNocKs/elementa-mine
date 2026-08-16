package fr.elementa.mine;

import net.milkbowl.vault.economy.Economy;
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

public class BagCommand implements CommandExecutor, TabCompleter {

    private final ElementaMine plugin;
    private final BagManager bagManager;

    private static final List<String> SUBCOMMANDS = Arrays.asList(
            "vendre", "autosell", "upgrade"
    );
    private static final List<String> UPGRADE_TYPES = Arrays.asList(
            "capacite", "autosell", "multiplicateur"
    );

    public BagCommand(ElementaMine plugin, BagManager bagManager) {
        this.plugin = plugin;
        this.bagManager = bagManager;
    }

    private void msg(CommandSender sender, String text) {
        sender.sendMessage(ChatColor.GOLD + "[Elementa] " + ChatColor.RESET + text);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            msg(sender, "Cette commande ne peut etre utilisee qu'en jeu.");
            return true;
        }

        if (args.length == 0) {
            player.openInventory(BagGui.build(bagManager, bagManager.getBag(player.getUniqueId())));
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "vendre": {
                if (!bagManager.isEconomyReady()) {
                    msg(player, ChatColor.RED + "Le systeme d'economie n'est pas disponible.");
                    return true;
                }
                double earned = bagManager.sellAll(player);
                if (earned <= 0) {
                    msg(player, "Ton sac est vide, rien a vendre.");
                } else {
                    Economy econ = bagManager.getEconomy();
                    msg(player, ChatColor.GREEN + "Vendu pour " + econ.format(earned) + " !");
                }
                return true;
            }

            case "autosell": {
                PlayerBag bag = bagManager.getBag(player.getUniqueId());
                if (!bag.isAutosellUnlocked()) {
                    msg(player, ChatColor.RED + "Tu n'as pas encore debloque la vente auto. Utilise /sac upgrade autosell.");
                    return true;
                }
                bag.setAutosellEnabled(!bag.isAutosellEnabled());
                msg(player, "Vente auto : " + (bag.isAutosellEnabled() ? ChatColor.GREEN + "ACTIVEE" : ChatColor.RED + "DESACTIVEE"));
                return true;
            }

            case "upgrade": {
                if (args.length < 2) {
                    msg(player, "Usage: /sac upgrade <capacite|autosell|multiplicateur>");
                    return true;
                }
                handleUpgrade(player, args[1].toLowerCase());
                return true;
            }

            default:
                msg(player, "Usage: /sac [vendre|autosell|upgrade]");
                return true;
        }
    }

    private void showBag(Player player) {
        PlayerBag bag = bagManager.getBag(player.getUniqueId());

        msg(player, ChatColor.YELLOW + "--- Ton sac (" + bag.getCurrentAmount() + "/" + bag.getMaxCapacity() + ") ---");
        if (bag.getContents().isEmpty()) {
            msg(player, "Vide pour le moment.");
        } else {
            for (Map.Entry<Material, Integer> entry : bag.getContents().entrySet()) {
                double unitPrice = bagManager.getPrice(entry.getKey());
                msg(player, "- " + entry.getKey().name() + " x" + entry.getValue()
                        + ChatColor.GRAY + " (" + unitPrice + " Eclats/u)");
            }
        }
        msg(player, ChatColor.AQUA + "Multiplicateur: x" + String.format("%.2f", bag.getMultiplier())
                + " | Vente auto: " + (bag.isAutosellUnlocked() ? (bag.isAutosellEnabled() ? "ON" : "OFF") : "non debloquee"));
    }

    private void handleUpgrade(Player player, String type) {
        if (!bagManager.isEconomyReady()) {
            msg(player, ChatColor.RED + "Le systeme d'economie n'est pas disponible.");
            return;
        }

        PlayerBag bag = bagManager.getBag(player.getUniqueId());
        Economy econ = bagManager.getEconomy();

        switch (type) {
            case "capacite": {
                int cost = bagManager.getCapacityUpgradeCost(bag);
                if (econ.getBalance(player) < cost) {
                    msg(player, ChatColor.RED + "Il te faut " + econ.format(cost) + " pour cette amelioration.");
                    return;
                }
                econ.withdrawPlayer(player, cost);
                bag.setCapacityLevel(bag.getCapacityLevel() + 1);
                msg(player, ChatColor.GREEN + "Capacite du sac augmentee ! Nouvelle capacite: " + bag.getMaxCapacity());
                return;
            }

            case "autosell": {
                if (bag.isAutosellUnlocked()) {
                    msg(player, "Tu as deja debloque la vente auto.");
                    return;
                }
                int cost = bagManager.getAutosellUnlockCost();
                if (econ.getBalance(player) < cost) {
                    msg(player, ChatColor.RED + "Il te faut " + econ.format(cost) + " pour debloquer la vente auto.");
                    return;
                }
                econ.withdrawPlayer(player, cost);
                bag.setAutosellUnlocked(true);
                msg(player, ChatColor.GREEN + "Vente auto debloquee ! Active-la avec /sac autosell.");
                return;
            }

            case "multiplicateur": {
                int cost = bagManager.getMultiplierUpgradeCost(bag);
                if (econ.getBalance(player) < cost) {
                    msg(player, ChatColor.RED + "Il te faut " + econ.format(cost) + " pour cette amelioration.");
                    return;
                }
                econ.withdrawPlayer(player, cost);
                bag.setMultiplierLevel(bag.getMultiplierLevel() + 1);
                msg(player, ChatColor.GREEN + "Multiplicateur augmente ! Nouveau multiplicateur: x"
                        + String.format("%.2f", bag.getMultiplier()));
                return;
            }

            default:
                msg(player, "Type d'amelioration inconnu. Usage: /sac upgrade <capacite|autosell|multiplicateur>");
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
        if (args.length == 2 && args[0].equalsIgnoreCase("upgrade")) {
            List<String> out = new ArrayList<>();
            String partial = args[1].toLowerCase();
            for (String s : UPGRADE_TYPES) {
                if (s.startsWith(partial)) out.add(s);
            }
            return out;
        }
        return new ArrayList<>();
    }
}
