package net.itsthesky.terrawars.api.model.game;

public interface IGameHolder {

    /**
     * Get the game of this holder.
     * @return the game of this holder
     * @see IGame#getState()
     */
    IGame getGame();

}
