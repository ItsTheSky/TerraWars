package net.itsthesky.terrawars;

import net.itsthesky.terrawars.api.services.base.IServiceProvider;
import net.itsthesky.terrawars.util.ItemBuilder;
import org.bukkit.plugin.java.JavaPlugin;

public final class TerraWars extends JavaPlugin {

    private static TerraWars plugin;
    private static IServiceProvider serviceProvider;

    @Override
    public void onEnable() {
        plugin = this;

        getLogger().info("TerraWars is starting...");

        serviceProvider = new ServiceProvider(this);
        try {
            serviceProvider.injectServices("net.itsthesky.terrawars.core.services");
        } catch (Exception e) {
            getLogger().severe("Failed to inject services: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getServer().getPluginManager().registerEvents(new ItemBuilder.ItemListener(), this);

        getLogger().info("TerraWars is ready to go! Registered " + serviceProvider.getRegisteredServices().size() + " services.");
    }

    @Override
    public void onDisable() {
        getLogger().info("TerraWars is stopping...");

        if (serviceProvider != null)
            serviceProvider.disableServices();
    }

    public static TerraWars instance() {
        return plugin;
    }

    public IServiceProvider serviceProvider() {
        return serviceProvider;
    }
}
