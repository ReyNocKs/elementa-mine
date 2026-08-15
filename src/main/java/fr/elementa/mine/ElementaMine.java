package fr.elementa.mine;

import org.bukkit.plugin.java.JavaPlugin;

public class ElementaMine extends JavaPlugin {

    private MineManager mineManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.mineManager = new MineManager(this);

        getServer().getPluginManager().registerEvents(new MineListener(this, mineManager), this);

        MineCommand mineCommand = new MineCommand(this, mineManager);
        getCommand("mine").setExecutor(mineCommand);
        getCommand("mine").setTabCompleter(mineCommand);

        getLogger().info("ElementaMine active - Zone(s) chargee(s): " + mineManager.getRegionCount());
    }

    @Override
    public void onDisable() {
        if (mineManager != null) {
            mineManager.saveAll();
        }
        getLogger().info("ElementaMine desactive.");
    }
}
