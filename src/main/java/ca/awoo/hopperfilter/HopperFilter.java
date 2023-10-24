package ca.awoo.hopperfilter;

import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Logger;

public final class HopperFilter extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(new HopperListener(), this);
        loggerInstance = Bukkit.getServer().getLogger();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
