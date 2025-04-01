package net.itsthesky.terrawars.api.model.game;

import lombok.Getter;
import net.itsthesky.terrawars.api.services.IChatService;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a (running) game of TerraWars.
 */
public interface IGame {

    /**
     * Represent the size of the game.
     */
    @Getter
    enum GameSize {
        SOLO(1),
        DUO(2),
        SQUAD(4),
        ;

        private final int playerPerTeam;

        GameSize(int playerPerTeam) {
            this.playerPerTeam = playerPerTeam;
        }
    }

    /**
     * Represent the state of the game.
     */
    enum GameState {
        /**
         * The game is waiting for players to join.
         */
        WAITING,

        /**
         * The game has enough players and is starting.
         * No more players can join.
         */
        STARTING,

        /**
         * The game is in progress, meaning people are currently playing
         * on the map.
         */
        RUNNING,

        /**
         * The game is over and the players are being teleported back to the lobby.
         */
        ENDED
    }

    @NotNull UUID getId();

    @NotNull World getWorld();

    @NotNull Location getLobbyLocation();

    @NotNull GameState getState();

    @NotNull Set<IGameTeam> getTeams() throws IllegalStateException;

    @NotNull Set<IGamePlayer> getWaitingPlayers() throws IllegalStateException;

    int getMaxPlayers();

    /**
     * Get the size of the game.
     * @return the size of the game
     */
    @NotNull GameSize getSize();

    default boolean isRunning() {
        return getState() == GameState.RUNNING;
    }

    void setState(@NotNull GameState state);

    /**
     * Try adding a player to this game. This is usually
     * called once a player want to join the game during {@link GameState#WAITING}.
     * <br>
     * If the game is already in {@link GameState#STARTING} or {@link GameState#RUNNING},
     * this method will return false.
     * @param player the player to add
     * @return true if the player was added, false otherwise
     */
    boolean tryAddPlayer(@NotNull Player player);

    /**
     * Remove a player from the game. This method will
     * <b>completely</b> remove the player from the game! This
     * should only be used during {@link GameState#WAITING}!
     * @param player the player to remove
     */
    void removePlayer(@NotNull Player player);

    void broadcastMessage(@NotNull IChatService.MessageSeverity severity, @NotNull String message, @Nullable IGameTeam specificTeam,
                          @Nullable OfflinePlayer sender);

    default void broadcastMessage(@NotNull IChatService.MessageSeverity severity, @NotNull String message) {
        broadcastMessage(severity, message, null, null);
    }

    void broadcastTitle(@NotNull String title, @NotNull String subtitle,
                        Duration fadeIn, Duration stay, Duration fadeOut,
                        @NotNull List<TextColor> scheme,
                        @Nullable IGameTeam specificTeam, @Nullable OfflinePlayer sender);

    default void broadcastTitle(@NotNull String title, @NotNull String subtitle,
                                @NotNull List<TextColor> scheme,
                                Duration fadeIn, Duration stay, Duration fadeOut) {
        broadcastTitle(title, subtitle, fadeIn, stay, fadeOut, scheme, null, null);
    }

}
