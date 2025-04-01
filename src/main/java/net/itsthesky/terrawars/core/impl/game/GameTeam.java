package net.itsthesky.terrawars.core.impl.game;

import net.itsthesky.terrawars.api.model.biome.IBiome;
import net.itsthesky.terrawars.api.model.game.IGame;
import net.itsthesky.terrawars.api.model.game.IGameNexus;
import net.itsthesky.terrawars.api.model.game.IGamePlayer;
import net.itsthesky.terrawars.api.model.game.IGameTeam;
import net.itsthesky.terrawars.util.Colors;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class GameTeam implements IGameTeam {

    @Override
    public @NotNull Set<IGamePlayer> getPlayers() {
        return Set.of();
    }

    @Override
    public @NotNull IBiome getBiome() {
        return null;
    }

    @Override
    public @NotNull IGameNexus getNexus() {
        return null;
    }

    @Override
    public @NotNull List<TextColor> getColorScheme() {
        return Colors.AMBER;
    }

    @Override
    public IGame getGame() {
        return null;
    }
}
