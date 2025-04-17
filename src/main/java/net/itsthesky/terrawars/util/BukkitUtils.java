package net.itsthesky.terrawars.util;

import io.papermc.paper.adventure.PaperAdventure;
import net.itsthesky.terrawars.TerraWars;
import net.itsthesky.terrawars.api.services.IChatService;
import net.itsthesky.terrawars.core.impl.game.Game;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
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

    public static void unregisterListener(Listener listener) {
        if (listener == null)
            throw new IllegalArgumentException("Listener cannot be null");

        HandlerList.unregisterAll(listener);
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

    public static IChatService chat() {
        return TerraWars.instance().serviceProvider().getService(IChatService.class);
    }

    public static void playSound(@NotNull Audience audience, @NotNull Sound sound, float volume, float pitch) {
        Checks.notNull(audience, "Audience cannot be null");
        Checks.notNull(sound, "Sound cannot be null");

        final var advSound = net.kyori.adventure.sound.Sound.sound()
                .pitch(pitch)
                .source(net.kyori.adventure.sound.Sound.Source.MASTER)
                .volume(volume)
                .type(sound);

        audience.playSound(advSound.build());
    }

    private static final JSONComponentSerializer jsonSerializer = JSONComponentSerializer.builder()
            .build();

    public static Component deserializeJsonText(String rawJson) {
        if (rawJson == null || rawJson.isEmpty())
            return Component.empty();
        try {
            return jsonSerializer.deserialize(rawJson);
        } catch (Exception e) {
            return Component.text(rawJson);
        }
    }

    public static void updateTitle(Player player, Component title) {
        final Inventory inventory = player.getOpenInventory().getTopInventory();
        final ItemStack[] contents = inventory.getContents();
        final AbstractContainerMenu menu = ((CraftPlayer) player).getHandle().containerMenu;

        ((CraftPlayer) player).getHandle().connection.send(new ClientboundOpenScreenPacket(
                menu.containerId,
                menu.getType(),
                PaperAdventure.asVanilla(title)
        ));
        player.updateInventory();
        inventory.setContents(contents);
    }
}
