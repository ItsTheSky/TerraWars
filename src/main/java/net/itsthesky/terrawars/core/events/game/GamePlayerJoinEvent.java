package net.itsthesky.terrawars.core.events.game;

import lombok.Getter;
import net.itsthesky.terrawars.api.model.game.IGamePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class GamePlayerJoinEvent extends GameEvent {

    private final IGamePlayer joinedPlayer;

    public GamePlayerJoinEvent(IGamePlayer joinedPlayer) {
        super(joinedPlayer.getGame());

        this.joinedPlayer = joinedPlayer;
    }


    //region Handlers
    private final static HandlerList handlers = new HandlerList();

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
    //endregion
}
