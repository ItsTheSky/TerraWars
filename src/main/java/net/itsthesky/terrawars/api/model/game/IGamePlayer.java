package net.itsthesky.terrawars.api.model.game;

import net.itsthesky.terrawars.api.model.ability.IAbility;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Represent a player that is in a game of TerraWars.
 */
public interface IGamePlayer extends IGameHolder {

    enum GamePlayerState {

        /**
         * The player is waiting for the game to start.
         * All player have this state when they join the game.
         */
        WAITING,

        /**
         * The player is part of a {@link IGameTeam team} (that can be
         * accessed with {@link IGamePlayer#getTeam()}).
         */
        TEAM,

        /**
         * The player is spectating the game.
         * <br>
         * This can be used to allow players to join a game
         * and watch it without being part of a team.
         * <br>
         * It is also given to players that died with their nexus
         * destroyed: they're spectating the game until it ends.
         */
        SPECTATOR,
    }

    @NotNull OfflinePlayer getOfflinePlayer();

    default @NotNull Player getPlayer() {
        if (!getOfflinePlayer().isConnected())
            throw new IllegalStateException("Player is not connected");

        return Objects.requireNonNull(getOfflinePlayer().getPlayer());
    }

    default boolean isOnline() {
        return getOfflinePlayer().isOnline();
    }

    /**
     * Get the team of this player.
     * This method will throw an {@link IllegalStateException} if the player
     * is not in a running/started game.
     * @return the team of this player
     * @throws IllegalStateException if the player is not in a running game
     * @see IGame#getState()
     * @see IGamePlayer#getGame()
     * @see GamePlayerState#TEAM
     */
    @Nullable IGameTeam getTeam() throws IllegalStateException;

    void setTeam(@Nullable IGameTeam team) throws IllegalStateException;

    /**
     * Get the state of this player.
     * @return the state of this player
     */
    @NotNull GamePlayerState getState();

    /**
     * Set the state of this player.
     * @param state the new state of this player
     */
    void setState(@NotNull GamePlayerState state);

    /**
     * Check if this player is in a team.
     * @return true if the player is in a team, false otherwise
     */
    default boolean isInTeam() {
        return getState() == GamePlayerState.TEAM;
    }

    /**
     * Get the currently selected ability of this player.
     * This method should always return an ability that is
     * from {@link IGameTeam#getBiome() its biome!}
     * <br>
     * May return null, in case it does not have any selected yet.
     * @return the currently selected ability of this player
     * @see IGamePlayer#setSelectedAbility(IAbility)
     */
    @Nullable IAbility getSelectedAbility();

    /**
     * Set the currently selected ability of this player.
     * This method should always set an ability that is
     * from {@link IGameTeam#getBiome() its biome!}
     * @param ability the new selected ability of this player
     * @see IGamePlayer#getSelectedAbility()
     */
    void setSelectedAbility(@Nullable IAbility ability);

    /**
     * Set up the hotbar for that player, so the last item
     * is the ability item, selected by the player.
     */
    void setupHotbar(boolean clear);

    /**
     * Refresh the hotbar of this player. This will basically
     * refresh teh ability's slot in the hotbar.
     */
    default void refreshHotbar() {
        setupHotbar(false);
    }
}
