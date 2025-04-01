package net.itsthesky.terrawars.api.model.game;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public interface IGameNexus extends IGameHolder {

    @NotNull IGameTeam getTeam();

    @NotNull Location getLocation();

    int getLevel();

    void setLevel(int level);

    int getMaxLevel();

    @NotNull NexusStats getStats(int level);

    default @NotNull NexusStats getStats() {
        return getStats(getLevel());
    }

    record NexusStats(int maxHealth, int regenPerSec, int regenDelay) {}

}
