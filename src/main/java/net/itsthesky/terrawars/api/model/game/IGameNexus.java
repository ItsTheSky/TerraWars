package net.itsthesky.terrawars.api.model.game;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public interface IGameNexus extends IGameHolder {

    @NotNull IGameTeam getTeam();

    @NotNull Location getLocation();

    int getLevel();

    void setLevel(int level);

    int getMaxLevel();

    @NotNull NexusStats getStats();

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    final class NexusStats {
        private int health;
        private int maxHealth;
        private int regenPerSec;
        private int regenDelay;
    }

}
