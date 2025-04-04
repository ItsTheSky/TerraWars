package net.itsthesky.terrawars.util;

import net.itsthesky.terrawars.TerraWars;
import net.itsthesky.terrawars.core.impl.game.Game;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

import java.util.function.Consumer;

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

    public static BukkitTask runTaskTimer(Runnable runnable,
                                          long delay, long period) {
        if (runnable == null)
            throw new IllegalArgumentException("Runnable cannot be null");

        return Bukkit.getScheduler().runTaskTimer(TerraWars.instance(), runnable, delay, period);
    }

    public static void runTaskTimer(Consumer<? super BukkitTask> runnable,
                                          long delay, long period) {
        if (runnable == null)
            throw new IllegalArgumentException("Runnable cannot be null");

        Bukkit.getScheduler().runTaskTimer(TerraWars.instance(), runnable, delay, period);
    }

    public static BukkitTask runTaskLater(Runnable runnable,
                                          long delay) {
        if (runnable == null)
            throw new IllegalArgumentException("Runnable cannot be null");

        return Bukkit.getScheduler().runTaskLater(TerraWars.instance(), runnable, delay);
    }

    public static void registerListener(Listener listener) {
        if (listener == null)
            throw new IllegalArgumentException("Listener cannot be null");

        Bukkit.getPluginManager().registerEvents(listener, TerraWars.instance());
    }
}
