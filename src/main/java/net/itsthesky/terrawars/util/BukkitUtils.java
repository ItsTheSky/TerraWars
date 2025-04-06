package net.itsthesky.terrawars.util;

import net.itsthesky.terrawars.TerraWars;
import net.itsthesky.terrawars.core.impl.game.Game;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    public static Color convertColor(TextColor color) {
        if (color == null)
            throw new IllegalArgumentException("Color cannot be null");

        return Color.fromRGB(color.red(), color.green(), color.blue());
    }

    public static void registerListener(Listener listener) {
        if (listener == null)
            throw new IllegalArgumentException("Listener cannot be null");

        Bukkit.getPluginManager().registerEvents(listener, TerraWars.instance());
    }

    public static @Nullable PersistentDataContainer getBlockPdc(@NotNull Block block) {
        final var chunk = block.getChunk();
        final var key = new NamespacedKey(Keys.NAMESPACE, "block_pdc_" + block.getX() + "_" + block.getY() + "_" + block.getZ());
        return chunk.getPersistentDataContainer().get(key, PersistentDataType.TAG_CONTAINER);
    }

    public static void setBlockPdc(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        final var chunk = block.getChunk();
        final var key = new NamespacedKey(Keys.NAMESPACE, "block_pdc_" + block.getX() + "_" + block.getY() + "_" + block.getZ());
        chunk.getPersistentDataContainer().set(key, PersistentDataType.TAG_CONTAINER, pdc);
    }

    public static void editBlockPdc(@NotNull Block block,
                                    @NotNull Consumer<PersistentDataContainer> consumer) {
        final var chunk = block.getChunk();
        final var key = new NamespacedKey(Keys.NAMESPACE, "block_pdc_" + block.getX() + "_" + block.getY() + "_" + block.getZ());
        var pdc = chunk.getPersistentDataContainer().get(key, PersistentDataType.TAG_CONTAINER);
        if (pdc == null)
            pdc = chunk.getPersistentDataContainer().getAdapterContext().newPersistentDataContainer();

        consumer.accept(pdc);
        chunk.getPersistentDataContainer().set(key, PersistentDataType.TAG_CONTAINER, pdc);
    }
}
