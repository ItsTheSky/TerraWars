package net.itsthesky.terrawars.util;

import net.itsthesky.terrawars.TerraWars;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;

public final class BukkitUtils {

    private BukkitUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static void callEvent(Event event) {
        if (event == null)
            throw new IllegalArgumentException("Event cannot be null");

        Bukkit.getPluginManager().callEvent(event);
    }

    public static void async(Runnable runnable) {
        if (runnable == null)
            throw new IllegalArgumentException("Runnable cannot be null");

        Bukkit.getScheduler().runTaskAsynchronously(TerraWars.instance(), runnable);
    }

    public static void sync(Runnable runnable) {
        if (runnable == null)
            throw new IllegalArgumentException("Runnable cannot be null");

        Bukkit.getScheduler().runTask(TerraWars.instance(), runnable);
    }

}
