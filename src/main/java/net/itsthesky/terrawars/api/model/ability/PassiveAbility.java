package net.itsthesky.terrawars.api.model.ability;

import net.itsthesky.terrawars.TerraWars;
import net.itsthesky.terrawars.api.model.game.IGame;
import net.itsthesky.terrawars.api.model.game.IGamePlayer;
import net.itsthesky.terrawars.util.BukkitUtils;
import org.bukkit.Material;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class PassiveAbility extends AbstractAbility {

    private final Map<UUID, PassiveAbilityListener> activeListeners = new HashMap<>();

    protected PassiveAbility(String id, Material icon, String displayName, List<String> description, int cooldownSeconds) {
        super(id, icon, displayName, description, cooldownSeconds, AbilityType.PASSIVE);
    }

    /**
     * Registers a listener for this ability when a player selects it.
     * Each player gets their own listener instance.
     *
     * @param player The game player who selected this ability
     * @param game The game instance
     */
    public void registerListener(@NotNull IGamePlayer player, @NotNull IGame game) {
        // Remove any existing listener for this player
        unregisterListener(player);

        // Create a new listener for this player
        final PassiveAbilityListener listener = createListener(player, game);
        activeListeners.put(player.getPlayer().getUniqueId(), listener);

        // Register the listener with Bukkit
        BukkitUtils.registerListener(listener);
    }

    /**
     * Unregisters the listener for this ability when a player deselects it.
     *
     * @param player The game player who deselected this ability
     */
    public void unregisterListener(@NotNull IGamePlayer player) {
        final PassiveAbilityListener listener = activeListeners.remove(player.getPlayer().getUniqueId());
        if (listener != null) {
            HandlerList.unregisterAll(listener);
        }
    }

    /**
     * Unregisters all listeners for this ability.
     * Should be called when the game ends.
     */
    public void unregisterAllListeners() {
        for (PassiveAbilityListener listener : activeListeners.values()) {
            HandlerList.unregisterAll(listener);
        }
        activeListeners.clear();
    }

    /**
     * Creates a new listener for this ability.
     *
     * @param player The game player who selected this ability
     * @param game The game instance
     * @return A new listener instance
     */
    protected abstract PassiveAbilityListener createListener(@NotNull IGamePlayer player, @NotNull IGame game);

    /**
     * Base interface for passive ability listeners.
     * All passive abilities should implement their own listener class.
     */
    protected interface PassiveAbilityListener extends Listener {
        // Marker interface
    }
}