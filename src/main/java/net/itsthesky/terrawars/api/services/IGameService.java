package net.itsthesky.terrawars.api.services;

import net.itsthesky.terrawars.api.model.biome.IBiome;
import net.itsthesky.terrawars.api.model.game.IGame;
import net.itsthesky.terrawars.core.config.GameConfig;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public interface IGameService {

    @NotNull Set<IGame> getGames();

    default @NotNull Set<IGame> getRunningGames() {
        return getGames().stream()
                .filter(IGame::isRunning)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Nullable IGame getGame(@NotNull UUID uuid);

    @NotNull IGame createGame(@NotNull GameConfig config);

    @Nullable IGame getPlayerGame(@NotNull UUID playerId);
}
