package net.itsthesky.terrawars.util;

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

}
