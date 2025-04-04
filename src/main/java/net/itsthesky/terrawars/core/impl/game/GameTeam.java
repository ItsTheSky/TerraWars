package net.itsthesky.terrawars.core.impl.game;

import lombok.Getter;
import net.itsthesky.terrawars.api.model.biome.IBiome;
import net.itsthesky.terrawars.api.model.game.IGame;
import net.itsthesky.terrawars.api.model.game.IGameNexus;
import net.itsthesky.terrawars.api.model.game.IGamePlayer;
import net.itsthesky.terrawars.api.model.game.IGameTeam;
import net.itsthesky.terrawars.util.Colors;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Getter
public class GameTeam implements IGameTeam {

    private final Map<UUID, GamePlayer> players;
    private final Game game;
    private final IBiome biome;
    private final IGameNexus nexus;

    public GameTeam(Game game, IBiome biome, IGameNexus nexus) {
        this.players = new HashMap<>(game.getSize().getPlayerPerTeam());
        this.game = game;
        this.biome = biome;
        this.nexus = nexus;
    }

    @Override
    public @NotNull Set<IGamePlayer> getPlayers() {
        return new HashSet<>(players.values());
    }

    @Override
    public @NotNull IBiome getBiome() {
        return biome;
    }

    @Override
    public @NotNull IGameNexus getNexus() {
        return nexus;
    }

    @Override
    public @NotNull List<TextColor> getColorScheme() {
        return Colors.AMBER;
    }

    @Override
    public boolean tryAddPlayer(@NotNull IGamePlayer player) {
        if (players.size() >= game.getSize().getPlayerPerTeam())
            return false;

        this.players.put(player.getPlayer().getUniqueId(), (GamePlayer) player);
        player.setTeam(this);
        return true;
    }
}
