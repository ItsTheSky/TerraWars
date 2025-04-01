package net.itsthesky.terrawars.core.impl.game;

import net.itsthesky.terrawars.api.model.game.IGame;
import net.itsthesky.terrawars.api.model.game.IGamePlayer;
import net.itsthesky.terrawars.api.model.game.IGameTeam;
import net.itsthesky.terrawars.util.Checks;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class GamePlayer implements IGamePlayer {

    private final OfflinePlayer player;
    private final IGame game;

    private GamePlayerState state;
    private @Nullable IGameTeam team;

    public GamePlayer(OfflinePlayer player, IGame game) {
        this.player = player;
        this.game = game;

        this.state = GamePlayerState.WAITING;
        this.team = null;
    }

    @Override
    public @NotNull OfflinePlayer getOfflinePlayer() {
        return player;
    }

    @Override
    public @NotNull IGameTeam getTeam() throws IllegalStateException {
        if (team == null)
            throw new IllegalStateException("Game has not started yet, player is not in a team.");

        return team;
    }

    @Override
    public void setTeam(@Nullable IGameTeam team) throws IllegalStateException {
        Checks.notNull(team, "Team cannot be null");

        this.team = team;
    }

    @Override
    public @NotNull GamePlayerState getState() {
        return state;
    }

    @Override
    public void setState(@NotNull GamePlayerState state) {
        Checks.notNull(state, "State cannot be null");

        this.state = state;
    }

    @Override
    public IGame getGame() {
        return game;
    }

}
