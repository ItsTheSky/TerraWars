package net.itsthesky.terrawars.core.events.game;

import lombok.Getter;
import net.itsthesky.terrawars.api.model.game.IGame;
import org.bukkit.event.Event;

@Getter
public abstract class GameEvent extends Event {

    private final IGame game;

    protected GameEvent(IGame game) {
        this.game = game;
    }

}
