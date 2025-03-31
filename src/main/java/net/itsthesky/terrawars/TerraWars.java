package net.itsthesky.terrawars;

import net.itsthesky.terrawars.api.services.base.IServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class TerraWars extends JavaPlugin {

    private static IServiceProvider serviceProvider;

    @Override
    public void onEnable() {
        getLogger().info("TerraWars is starting...");

        serviceProvider = new ServiceProvider(this);
        try {
            serviceProvider.injectServices("net.itsthesky.terrawars.core.services");
        } catch (Exception e) {
            getLogger().severe("Failed to inject services: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("TerraWars is ready to go! Registered " + serviceProvider.getRegisteredServices().size() + " services.");
    }

    @Override
    public void onDisable() {
        getLogger().info("TerraWars is stopping...");

        if (serviceProvider != null)
            serviceProvider.disableServices();
    }
}
