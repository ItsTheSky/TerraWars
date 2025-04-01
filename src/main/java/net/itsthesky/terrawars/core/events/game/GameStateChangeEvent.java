package net.itsthesky.terrawars.core.events.game;

import lombok.Getter;
import net.itsthesky.terrawars.api.model.game.IGame;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This event is called when the state of a game is changed.
 */
@Getter
public class GameStateChangeEvent extends GameEvent {

    private final IGame.GameState oldState;
    private final IGame.GameState newState;

    public GameStateChangeEvent(IGame game, IGame.GameState oldState, IGame.GameState newState) {
        super(game);
        this.oldState = oldState;
        this.newState = newState;
    }

    private final static HandlerList handlers = new HandlerList();
    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
