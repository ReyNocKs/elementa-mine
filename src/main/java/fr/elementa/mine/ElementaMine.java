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

        MineCommand mineCommand = new MineCommand(this, mineManager);
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
