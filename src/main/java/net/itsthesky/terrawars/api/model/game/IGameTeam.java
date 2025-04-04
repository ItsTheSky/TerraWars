package net.itsthesky.terrawars.api.model.game;

import net.itsthesky.terrawars.api.model.biome.IBiome;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public interface IGameTeam extends IGameHolder {

    @NotNull Set<IGamePlayer> getPlayers();

    @NotNull IBiome getBiome();

    @NotNull IGameNexus getNexus();

    @NotNull List<TextColor> getColorScheme();

    boolean tryAddPlayer(@NotNull IGamePlayer player);
}
