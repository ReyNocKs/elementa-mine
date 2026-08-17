package fr.elementa.mine;
import org.bukkit.Material; import java.util.LinkedHashMap; import java.util.Map;
public final class MineTiers {
 public static final int MAX_TIER=4;
 public static int size(ElementaMine p,int t){return p.getConfig().getInt("mine.tiers."+t+".size",9);}
 public static int price(ElementaMine p,int t){return p.getConfig().getInt("mine.tiers."+t+".price",0);}
 public static Map<Material,Integer> composition(ElementaMine p){Map<Material,Integer> m=new LinkedHashMap<>(); var s=p.getConfig().getConfigurationSection("mine.composition"); if(s!=null) for(String k:s.getKeys(false)){Material x=Material.matchMaterial(k); if(x!=null)m.put(x,s.getInt(k));} return m;}
 private MineTiers(){}
}
