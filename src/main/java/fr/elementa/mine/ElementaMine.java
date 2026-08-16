package fr.elementa.mine;

import org.bukkit.plugin.java.JavaPlugin;

public class ElementaMine extends JavaPlugin {

    private MineManager mineManager;
    private BagManager bagManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.mineManager = new MineManager(this);
        this.bagManager = new BagManager(this);

        getServer().getPluginManager().registerEvents(new MineListener(this, mineManager, bagManager), this);
        getServer().getPluginManager().registerEvents(new GuiListener(this, bagManager), this);

        if (getServer().getPluginManager().getPlugin("BentoBox") != null) {
            getServer().getPluginManager().registerEvents(new IslandListener(this, mineManager), this);
            getLogger().info("BentoBox detecte - generation automatique des mines d'ile activee.");
        } else {
            getLogger().warning("BentoBox non detecte - les mines devront etre creees manuellement via /mine.");
        }

        MineCommand mineCommand = new MineCommand(this, mineManager, bagManager);
        getCommand("mine").setExecutor(mineCommand);
        getCommand("mine").setTabCompleter(mineCommand);

        BagCommand bagCommand = new BagCommand(this, bagManager);
        getCommand("sac").setExecutor(bagCommand);
        getCommand("sac").setTabCompleter(bagCommand);

        getLogger().info("ElementaMine active - Zone(s) chargee(s): " + mineManager.getRegionCount());
    }

    @Override
    public void onDisable() {
        if (mineManager != null) {
            mineManager.saveAll();
        }
        if (bagManager != null) {
            bagManager.savePrices();
            bagManager.saveBags();
        }
        getLogger().info("ElementaMine desactive.");
    }
}
