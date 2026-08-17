package fr.elementa.mine;
import org.bukkit.plugin.java.JavaPlugin;
public class ElementaMine extends JavaPlugin { private MineManager mineManager; private BagManager bagManager;
 public void onEnable(){saveDefaultConfig();mineManager=new MineManager(this);bagManager=new BagManager(this);getServer().getPluginManager().registerEvents(new MineListener(this,mineManager,bagManager),this);getServer().getPluginManager().registerEvents(new GuiListener(this,bagManager),this);if(getServer().getPluginManager().getPlugin("BentoBox")!=null)getServer().getPluginManager().registerEvents(new IslandListener(this,mineManager),this);MineCommand c=new MineCommand(this,mineManager,bagManager);getCommand("mine").setExecutor(c);getCommand("mine").setTabCompleter(c);BagCommand b=new BagCommand(this,bagManager);getCommand("sac").setExecutor(b);getCommand("sac").setTabCompleter(b);long min=getConfig().getLong("mine.reset-period-minutes",0);if(min>0)getServer().getScheduler().runTaskTimer(this,()->mineManager.getAll().values().forEach(mineManager::generate),min*1200L,min*1200L);}
 public void onDisable(){if(mineManager!=null)mineManager.saveAll();if(bagManager!=null){bagManager.savePrices();bagManager.saveBags();}}
 public MineManager mines(){return mineManager;} public String msg(String k){return org.bukkit.ChatColor.translateAlternateColorCodes('&',getConfig().getString("messages."+k,k));}
}
